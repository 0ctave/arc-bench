package fr.bloctave.arena.agent.trace

import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.eclipse.lmos.arc.agents.tracing.AgentTracer
import org.eclipse.lmos.arc.agents.tracing.Events
import org.eclipse.lmos.arc.agents.tracing.Tags
import org.eclipse.lmos.arc.agents.withLogContext
import java.time.Instant
import java.util.Collections
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

// --- 1. The Coroutine Context Elements ---

/**
 * Holds the ROOT of the trace for the current execution.
 * This ensures every coroutine in the scope writes to the same report.
 */
class TraceCollector(
    val traceId: String = UUID.randomUUID().toString(),
    val rootSpans: MutableList<AgentSpan> = Collections.synchronizedList(mutableListOf())
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<TraceCollector>
}

/**
 * Holds the POINTER to the 'current' active span in this coroutine scope.
 * This allows nesting (children knowing who their parent is).
 */
class ActiveSpanElement(
    val span: AgentSpan
) : ThreadContextElement<AgentSpan?> {
    // We use ThreadContextElement to ensure compatibility if libraries rely on ThreadLocals,
    // but primarily we use it as a standard ContextElement.

    companion object Key : CoroutineContext.Key<ActiveSpanElement>

    override val key: CoroutineContext.Key<*> = Key

    override fun updateThreadContext(context: CoroutineContext): AgentSpan? {
        return null // We don't rely on ThreadLocal storage, strictly CoroutineContext
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: AgentSpan?) {
        // No-op
    }
}

// --- 2. The Stateless Tracer ---

/**
 * A "Ghost" Tracer. It has no state itself.
 * It materializes traces only when run inside a context containing a TraceCollector.
 */
class InMemoryAgentTracer : AgentTracer {

    override suspend fun <T> withSpan(
        name: String,
        attributes: Map<String, String>,
        fn: suspend (Tags, Events) -> T
    ): T {
        // 1. Resolve Parent
        val currentContext = currentCoroutineContext()
        val activeParent = currentContext[ActiveSpanElement]?.span
        val collector = currentContext[TraceCollector]

        // If no collector is found, we are not recording. Just run the function.
        if (collector == null) {
            return fn(NoOpTags, NoOpEvents)
        }

        // 2. Create the new Span
        val newSpan = AgentSpan(name = name)
        newSpan.attributes.putAll(attributes)

        // 3. Attach to Tree
        if (activeParent != null) {
            synchronized(activeParent.children) {
                activeParent.children.add(newSpan)
            }
        } else {
            // No parent? This is a root span in this context.
            collector.rootSpans.add(newSpan)
        }

        // 4. Run Logic with new "Active Span" in context
        // We wrap the `fn` execution so `newSpan` becomes the `ActiveSpanElement` for all downstream calls.
        return withLogContext(attributes) {
            val newContext = currentContext + ActiveSpanElement(newSpan)

            try {
                withContext(newContext) {
                    fn(
                        // Adapter for Tags
                        object : Tags {
                            override fun tag(key: String, value: String) { newSpan.tags[key] = value }
                            override fun tag(key: String, value: Long) { newSpan.tags[key] = value.toString() }
                            override fun tag(key: String, value: Boolean) { newSpan.tags[key] = value.toString() }
                            override fun error(ex: Throwable) {
                                newSpan.error = ex
                                newSpan.tags["error"] = ex.message ?: "Unknown"
                            }
                        },
                        // Adapter for Events
                        Events { key, value ->
                            synchronized(newSpan.events) {
                                newSpan.events.add(TraceEvent(key, value))
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                newSpan.error = e
                newSpan.tags["error"] = e.message ?: "Unknown"
                throw e
            } finally {
                newSpan.endTimestamp = Instant.now()
            }
        }
    }

    override suspend fun addToSpan(key: String, value: String) {
        val span = currentCoroutineContext()[ActiveSpanElement]?.span
        span?.tags?.put(key, value)
    }
}

// Helpers
private object NoOpTags : Tags {
    override fun tag(key: String, value: String) {}
    override fun tag(key: String, value: Long) {}
    override fun tag(key: String, value: Boolean) {}
    override fun error(ex: Throwable) {}
}
private object NoOpEvents : Events {
    override fun event(key: String, value: String) {}
}
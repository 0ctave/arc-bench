package fr.bloctave.arena.agent.trace

import java.time.Instant

/**
 * The root container for a full execution trace.
 */
data class TraceData(
    val traceId: String,
    val startTimestamp: Instant,
    val rootSpans: List<AgentSpan>
)

/**
 * Represents a single operation (Agent execution, Tool call, LLM inference).
 */
data class AgentSpan(
    val name: String,
    val attributes: MutableMap<String, String> = mutableMapOf(),
    val tags: MutableMap<String, String> = mutableMapOf(),
    val events: MutableList<TraceEvent> = mutableListOf(),
    val children: MutableList<AgentSpan> = mutableListOf(),
    val startTimestamp: Instant = Instant.now(),
    var endTimestamp: Instant? = null,
    var error: Throwable? = null
) {
    val durationMs: Long
        get() = if (endTimestamp != null) {
            endTimestamp!!.toEpochMilli() - startTimestamp.toEpochMilli()
        } else 0
}

data class TraceEvent(
    val name: String,
    val value: String,
    val timestamp: Instant = Instant.now()
)

/**
 * The wrapper result containing both the value and the traces.
 */
data class AgentResult<T>(
    val result: T,
    val trace: TraceData
)
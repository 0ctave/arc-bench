package fr.bloctave.arena.agent

import fr.bloctave.arena.agent.trace.AgentResult
import fr.bloctave.arena.agent.trace.TraceCollector
import fr.bloctave.arena.agent.trace.TraceData
import kotlinx.coroutines.withContext
import org.eclipse.lmos.arc.agents.Agent
import org.eclipse.lmos.arc.agents.AgentProvider
import org.eclipse.lmos.arc.agents.conversation.Conversation
import org.eclipse.lmos.arc.core.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class AgentService(
    private val agentProvider: AgentProvider
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Calls an agent by name and captures its entire execution trace in memory.
     */
    suspend fun callAgent(
        name: String,
        input: Conversation
    ): AgentResult<Conversation> {

        // 1. Create a unique collector for THIS specific request
        val traceId = UUID.randomUUID().toString()
        val collector = TraceCollector(traceId = traceId)
        val start = Instant.now()

        // 2. Switch context to include the collector
        // ANY tracing that happens inside this block (even deep in the agent)
        // will find this specific 'collector' instance.
        return withContext(collector) {
            log.info("Starting agent execution: $name [$traceId]")

            val agent = agentProvider.getAgents().find { it.name == name }
                ?: throw IllegalArgumentException("Agent '$name' not found")

            // We cast to the expected type (Conversation -> Conversation)
            @Suppress("UNCHECKED_CAST")
            val chatAgent = agent as Agent<Conversation, Conversation>

            // 3. Execute
            val result = chatAgent.execute(input).getOrThrow()

            // 4. Snapshot the data
            val traceData = TraceData(
                traceId = traceId,
                startTimestamp = start,
                rootSpans = collector.rootSpans.toList() // Create a copy
            )

            AgentResult(result, traceData)
        }
    }
}
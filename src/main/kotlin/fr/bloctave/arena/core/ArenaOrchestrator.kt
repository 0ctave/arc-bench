package fr.bloctave.arena.core

/**
 * The ArenaOrchestrator manages the lifecycle of a competitive evaluation match.
 * It is responsible for:
 * 1. Generating the Match ID.
 * 2. Establishing the Trace Context.
 * 3. Forking execution to Agent Runners.
 * 4. Signaling the Judge Service upon completion.
 */
/*@Service
class ArenaOrchestrator(
    private val openTelemetry: OpenTelemetry,
    private val agentRunner: ArenaAgentRunner,
    private val agentService: AgentService
) {

    private val logger = LoggerFactory.getLogger(ArenaOrchestrator::class.java)
    private val tracer: Tracer = openTelemetry.getTracer("com.eclipse.lmos.arena.orchestrator")

    // Custom dispatcher could be injected here for better isolation
    private val executionDispatcher: CoroutineDispatcher = Dispatchers.Default

    /**
     * Conducts an asynchronous match between two agents for a given query.
     *
     * @param query The input query for the agents.
     * @return The unique Match ID generated for this session.
     */
    suspend fun conductMatch(query: ArenaQuery): String {
        val matchId = UUID.randomUUID().toString()

        // 1. Start the Parent Span (The "Container" for the entire match)
        val parentSpan = tracer.spanBuilder("arena-match-orchestration")
            .setAttribute(TelemetryKeys.MATCH_ID_ATTRIBUTE, matchId)
            .setAttribute("arena.query_id", query.queryId)
            .startSpan()

        try {
            // 2. Prepare the OpenTelemetry Context
            // We create a new Context containing the parentSpan.
            // This object is immutable and detached from the thread at this point.
            val otelContext = Context.current().with(parentSpan)

            logger.info("Initiating Match $matchId. Context prepared.")

            // 3. Enter the Coroutine Context with OTel Propagation
            // 'withContext' switches us to the execution dispatcher AND mounts the OTel context.
            // crucial: otelContext.asContextElement() is the bridge.
            withContext(executionDispatcher + otelContext) {

                // Initialize the buffer in the Judge Service BEFORE launching agents
                // This ensures that if agents are incredibly fast, the buffer exists.
                agentService.initializeMatch(matchId, query)

                // 4. Structured Concurrency: Launch Agents
                // coroutineScope ensures that if one fails, we can handle cancellation gracefully.
                // It also waits for children to finish before this block exits (though we use awaitAll explicitly).
                val results = coroutineScope {

                    // Launch Agent A
                    val agentAJob = async {
                        runAgentWithSafeContext("Agent-A", query, matchId)
                    }

                    // Launch Agent B
                    val agentBJob = async {
                        runAgentWithSafeContext("Agent-B", query, matchId)
                    }

                    // Asynchronously wait for both results
                    val completedResults = awaitAll(agentAJob, agentBJob)

                    logger.info("Match $matchId: All agents finished execution.")
                    completedResults
                }

                // 5. Trigger Judgment
                // At this point, traces should have been exported (or are in transit).
                // We signal the Judge Service to close the buffer and evaluate.
                agentService.triggerJudiciaryService(matchId)
            }
        } catch (e: Exception) {
            logger.error("Match $matchId failed due to orchestrator error", e)
            parentSpan.setStatus(StatusCode.ERROR, e.message)
            parentSpan.recordException(e)
            throw e
        } finally {
            // End the parent span. This marks the end of the orchestration trace.
            parentSpan.end()
        }

        return matchId
    }

    /**
     * Helper wrapper to ensure specific agent attributes are attached to the agent's root span.
     * This creates a clear visual hierarchy in tools like Jaeger/Zipkin:
     * Match-Span -> Agent-A-Span -> [Internal Agent Logic]
     */
    private suspend fun runAgentWithSafeContext(
        agentName: String,
        query: ArenaQuery,
        matchId: String
    ): AgentResult {
        // Create a named span for this specific agent runner
        val agentSpan = tracer.spanBuilder("agent-execution-$agentName")
            .setAttribute(TelemetryKeys.AGENT_NAME_ATTRIBUTE, agentName)
            .setAttribute(TelemetryKeys.MATCH_ID_ATTRIBUTE, matchId)
            .startSpan()

        // We must re-mount this new 'agentSpan' as the current context for the runner
        return try {
            withContext(Context.current().with(agentSpan).asContextElement()) {
                agentRunner.executeAgent(agentName, query)
            }
        } catch (e: Exception) {
            agentSpan.recordException(e)
            agentSpan.setStatus(StatusCode.ERROR)
            // Return a failure result rather than throwing, to allow the other agent to finish
            AgentResult(agentName, "", 0, StatusCode.ERROR.toString(), e.message)
            // Note: In real code, map OTel status to domain status
            throw e
        } finally {
            agentSpan.end()
        }
    }
}*/
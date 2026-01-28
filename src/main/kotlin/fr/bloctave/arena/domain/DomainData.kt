package fr.bloctave.arena.domain

import kotlinx.serialization.Serializable

@Serializable
data class AgentTrajectory(
    val agentId: String,
    val matchId: String,
    val timestamp: Long,
    val steps: List<TraceStep>, // The sequence of events
    val finalResult: String
)

@Serializable
sealed interface TraceStep {

    // The internal monologue (Reasoning)
    @Serializable
    data class Thought(
        val content: String,
        val timestamp: Long
    ) : TraceStep

    // The attempt to act (Tool Usage)
    @Serializable
    data class ToolCall(
        val toolName: String,
        val argumentsJson: String,
        val timestamp: Long
    ) : TraceStep

    // The reality check (Environment Feedback)
    @Serializable
    data class ToolOutput(
        val toolName: String,
        val result: String,
        val isError: Boolean
    ) : TraceStep
}

@Serializable
data class ArenaVerdict(
    val winnerAgentId: String?, // Null if Tie
    val confidenceScore: Double, // 0.0 to 1.0
    val reasoning: String,
    val violatingConstraints: List<String> // e.g.,
)
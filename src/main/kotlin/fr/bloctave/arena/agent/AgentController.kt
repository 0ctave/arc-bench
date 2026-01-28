package fr.bloctave.arena.agent

import fr.bloctave.arena.agent.trace.AgentResult
import org.eclipse.lmos.arc.agents.conversation.Conversation
import org.eclipse.lmos.arc.agents.conversation.UserMessage
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class AgentRequest(
    val agentName: String,
    val prompt: String
)

@RestController
class AgentController(
    private val executionService: AgentService
) {

    @PostMapping("/agent/run")
    suspend fun runAgent(@RequestBody request: AgentRequest): AgentResult<Conversation> {
        val conversation = Conversation(transcript = listOf(UserMessage(request.prompt)))

        return executionService.callAgent(
            name = request.agentName,
            input = conversation
        )
    }
}
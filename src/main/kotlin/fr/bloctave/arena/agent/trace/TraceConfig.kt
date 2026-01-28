package fr.bloctave.arena.agent.trace

import org.eclipse.lmos.arc.agents.tracing.AgentTracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class TraceConfig {

    /**
     * Registers our InMemory tracer as the primary tracer for the system.
     * ARC will automatically inject this into all Agents declared in the DSL.
     */
    @Bean
    @Primary
    fun inMemoryTracer(): AgentTracer {
        return InMemoryAgentTracer()
    }
}
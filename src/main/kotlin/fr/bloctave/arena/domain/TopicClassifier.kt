package fr.bloctave.arena.domain

interface TopicClassifier {
    // Returns "Banking", "Healthcare", "Telecom", etc.
    suspend fun classify(userPrompt: String): DomainContext
}

data class DomainContext(
    val domain: String,
    val criticalConstraints: List<String> // e.g., "Must verify ID first"
)
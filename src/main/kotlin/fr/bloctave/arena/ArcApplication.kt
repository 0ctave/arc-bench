// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0


package fr.bloctave.arena

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Simple Spring Boot application that demonstrates how to use the Arc Agents.
 */
@SpringBootApplication
class ArcApplication

fun main(args: Array<String>) {
    runApplication<ArcApplication>(*args)
}

/*
 * Copyright 2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.roboquant.common

import java.util.logging.*
import kotlin.reflect.KClass

/**
 * Simple Logging object that provides utility methods to create and update loggers. Where many loggers APIs are
 * focused on serverside applications, this API is also suitable for interactive environments like notebooks since
 * you can change logging levels at runtime.
 *
 * Features:
 *
 * - Smaller and more user-friendly log messages
 * - Can set log levels at runtime
 * - Use color syntax
 *
 * Please note this is a logger that can is used in Kotlin source code, not to be confused with a MetricsLogger
 * that can be used to log metrics during a run.
 */
object Logging {

    private var defaultLevel = Level.INFO

    /**
     * Use a simple output format (default) or a more detailed format better suited for debugging purposes
     */
    var useSimpleFormat = true

    // ANSI escape code
    private const val ANSI_RESET = "\u001B[0m"
    private const val ANSI_GREEN = "\u001B[32m"
    private const val ANSI_BLUE = "\u001B[34m"

    private class LoggingFormatter : SimpleFormatter() {

        @Suppress("MaxLineLength")
        override fun format(lr: LogRecord): String {
            return if (useSimpleFormat) {
                val shortLoggerName = lr.loggerName.split('.').last()
                "[$ANSI_BLUE${lr.level.localizedName}$ANSI_RESET] $ANSI_GREEN${shortLoggerName}:$ANSI_RESET ${lr.message}\n"
            } else {
                "[$ANSI_BLUE${lr.level.localizedName}$ANSI_RESET] $ANSI_GREEN${lr.loggerName} ${lr.sourceClassName}.${lr.sourceMethodName}:$ANSI_RESET ${lr.message}\n"
            }
        }
    }

    init {
        // Install a modified formatter
        val handler = ConsoleHandler()
        handler.level = Level.FINEST
        handler.formatter = LoggingFormatter()
        resetHandler(handler)
    }

    /**
     * Reset a [handler]
     */
    private fun resetHandler(handler: Handler) {
        LogManager.getLogManager().reset()
        val rootLogger = Logger.getLogger("")
        rootLogger.addHandler(handler)
    }

    /**
     * Get a logger based on the provided [clazz]
     */
    fun getLogger(clazz: KClass<*>): Logger {
        return getLogger(clazz.qualifiedName ?: "$clazz")
    }

    /**
     * Get a logger based on the provided [name]
     */
    internal fun getLogger(name: String): Logger {
        val mainLogger: Logger = Logger.getLogger(name)
        mainLogger.level = defaultLevel
        return mainLogger
    }

    /**
     * Set the logging level for all loggers to specified [level] and restrict the update to loggers whose name start
     * with provided [prefix], the default being "org.roboquant"
     */
    fun setLevel(level: Level, prefix: String = "org.roboquant", updateDefault: Boolean = true) {
        val manager = LogManager.getLogManager()
        if (updateDefault) defaultLevel = level
        LogManager.getLogManager().loggerNames.toList().forEach {
            if (it.startsWith(prefix)) {
                manager.getLogger(it)?.level = level
            }
        }
    }

    /**
     * Set the default logging [level] for new Loggers. This won't change logging level of already created loggers,
     * for that please use [setLevel].
     */
    fun setDefaultLevel(level: Level) {
        defaultLevel = level
    }

    /**
     * Return the registered logger names
     */
    fun getLoggerNames() = LogManager.getLogManager().loggerNames.toList()

}

/**
 * Log a severe error [msg] with the [throwable]
 */
fun Logger.severe(msg: String, throwable: Throwable) = log(Level.SEVERE, msg + throwable.message)

package io.spokestack.ciabata

import java.util.*

/**
 * Possible modes for the timer. A single preparation period precedes the first period of work,
 * after which work and rest periods alternate until the pre-set number of cycles has been
 * completed.
 */
enum class TimerMode {
    PREP, REST, WORK;

    fun text(): String {
        return name.toLowerCase(Locale.getDefault())
    }
}
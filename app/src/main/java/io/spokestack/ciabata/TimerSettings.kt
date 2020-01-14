package io.spokestack.ciabata

/**
 * Simple data class to express the timer's settings. Times are expressed in seconds.
 */
data class TimerSettings (
    val cycles: Int = 8,
    // prep time occurs once, when the timer is first started
    val prep: Int = 10,
    val work: Int = 20,
    val rest: Int = 10
)

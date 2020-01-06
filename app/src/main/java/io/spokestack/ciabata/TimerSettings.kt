package io.spokestack.ciabata

/*
Simple data class to express the timer's settings.
 */
data class TimerSettings (
    val cycles: Int = 8,
    val prep: Int = 10,
    val work: Int = 30,
    val rest: Int = 10
)

package io.spokestack.ciabata

import java.util.*

enum class TimerMode {
    PREP, REST, WORK;

    fun text(): String {
        return name.toLowerCase(Locale.getDefault())
    }
}
package io.spokestack.ciabata

/**
 * The intents recognized by this app.
 */
enum class Intent {
    START, STOP, RESET, UNKNOWN
}

/**
 * The singleton intent classifier responsible for making user utterances actionable.
 */
object IntentClassifier {
    private val resetRegex = Regex(
        """(?xi)
        ^(?: reset (\s (?: the|my) \s timer)? |
        start \s over |
        (?: let'?s) \s do \s that \s again)
        """
    )

    private val startRegex = Regex(
        """(?xi)
        ^(?: start (\s (?: the|my) \s timer)? |
        (?: let'?s \s)? go)
        """
    )

    private val stopRegex = Regex(
        """(?xi)
        ^(?: stop (\s (?: the|my) \s timer)? |
        (?: I'?ve \s had \s)? enough)
        """
    )

    private val intentMap = mapOf(
        Intent.RESET to resetRegex,
        Intent.START to startRegex,
        Intent.STOP to stopRegex
    )


    fun classify(utterance: String): Intent {
        for ((intent, regex) in intentMap) {
            regex.find(utterance)?.let {
                return intent
            }
        }
        return Intent.UNKNOWN
    }
}

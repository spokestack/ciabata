package io.spokestack.ciabata

/**
 * The limited set of intents recognized by this app.
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


    /**
     * Turn unstructured text into one of a limited number of intents for the app to act on.
     *
     * @param utterance The full text of the user's utterance.
     * @return an [Intent] for use by the timer.
     */
    fun classify(utterance: String): Intent {
        for ((intent, regex) in intentMap) {
            regex.find(utterance)?.let {
                return intent
            }
        }
        return Intent.UNKNOWN
    }
}

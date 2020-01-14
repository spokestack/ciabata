# Ciabata

Ciabata is a simple (yet customizable) fitness timer for iOS that follows the [Tabata method](https://en.wikipedia.org/wiki/High-intensity_interval_training#Tabata_regimen). It uses the [Spokestack](https://github.com/spokestack/spokestack-ios) libary to enable hands-free control via voice as well as user-supplied custom TTS feedback.


Ciabata is a simple Android app used to demonstrate a basic integration with the [Spokestack](https://github.com/spokestack/spokestack-android) voice control library. It follows the [Tabata method](https://en.wikipedia.org/wiki/High-intensity_interval_training#Tabata_regimen), alternating cycles of high-intensity work and rest over a period of two minutes (all times are configurable in the `TimerSettings` class) and allowing the user to control the timer with their voice.

## Development instructions

First, download the TensorFlow Lite wakeword models:
- [detect](https://d3dmqd7cy685il.cloudfront.net/model/wake/spokestack/detect.lite)
- [encode](https://d3dmqd7cy685il.cloudfront.net/model/wake/spokestack/encode.lite)
- [filter](https://d3dmqd7cy685il.cloudfront.net/model/wake/spokestack/filter.lite)
and put them in the `assets` directory.

By default, the app uses Android's built-in `SpeechRecognizer` for ASR, but the Android emulator doesn't play well with this component, so you'll either need to test on a real device or use a different ASR to test voice features.

To use Google ASR, you'll need a `google.properties` file (in the Java Property file format) in the root of the project with a single property, `CREDENTIALS`, that contains Google API credentials in a stringified version of the JSON format described [here](https://cloud.google.com/speech/docs/streaming-recognize). Including a JSON string in Java's property format is admittedly inelegant, so feel free to change the app's Gradle file to load these credentials some other way; it's done this way for pure expedience.

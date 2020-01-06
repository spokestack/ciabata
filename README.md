# Ciabata

Ciabata is a simple Android app used to demonstrate a basic integration with the [Spokestack](https://github.com/spokestack/spokestack-android) voice control library.

## Development instructions
First, download the TensorFlow Lite wakeword models: 
- [detect](https://d3dmqd7cy685il.cloudfront.net/model/wake/spokestack/detect.lite) 
- [encode](https://d3dmqd7cy685il.cloudfront.net/model/wake/spokestack/encode.lite) 
- [filter](https://d3dmqd7cy685il.cloudfront.net/model/wake/spokestack/filter.lite)
and put them in the `assets` directory.

To use Google ASR, you'll need a `google.properties` file in the root of the project with a single property, `CREDENTIALS`, that contains Google API credentials in the JSON format described [here](https://cloud.google.com/speech/docs/streaming-recognize). Including a JSON string in Java's property format is admittedly inelegant, so feel free to change the app's gradle file to load these credentials some other way; it's done this way for pure expedience.



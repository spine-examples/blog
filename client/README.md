# Flutter client for Blog

This is a simple web-client for the Blog app. The client uses the Flutter framework and the Spine 
Dart [library](https://github.com/SpineEventEngine/dart).

## Build

Dart and the `pub` tool must be installed in order for to build the project. Refer to the official
Dart documentation for the instructions. 

The `protoc_plugin` and the Spine `dart_code_gen` packages must be installed globally, as follows:

```bash
pub global activate protoc_plugin
pub global activate dart_code_gen
```

## Browsing the client

In order to use the client, build the project:

```bash
./gradlew build
```

Then, start the `web` server by running:

```bash
./gradlew :web:appStart
```

Then, in a separate terminal window, start the Flutter web server:

```bash
cd ./client
flutter run
```

This command might request you to choose the "device" to open the app on. Choose the appropriate
web browser.

Now the client is up and running.

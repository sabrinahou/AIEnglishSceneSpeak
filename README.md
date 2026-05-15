# AIEnglishSceneSpeak

AIEnglishSceneSpeak is a local-first Android app for learning practical English through real-life scenes.

Instead of starting with traditional word lists, the app turns a user-provided scenario, such as a first tennis lesson with an English-speaking coach, into a visual, audio-friendly learning pack with vocabulary, phrases, sentences, dialogues, scene images, translation practice, and roleplay.

The project is designed as a standalone Android app:

- No backend server
- No user account system
- No remote database
- No cloud sync
- User-provided API key
- Local JSON learning packs
- Local audio and image cache

## Current Status

This is an MVP Android project built for rapid iteration.

The app can run locally in Android Studio or on an Android emulator. The current generation flow prioritizes responsiveness: the first learning pack is created instantly from a rich local scenario pack, while AI-powered features such as high-quality speech, image generation, correction, and roleplay can be used on demand when an API key is configured.

## Features

### Scene-Based Learning Packs

- Enter a real-life scenario in Chinese or English.
- Choose a learner level.
- Generate a structured learning pack.
- Store the pack locally as JSON.
- Reopen saved packs after restarting the app.

### Vocabulary Learning

- Grouped vocabulary categories.
- Cartoon-style word cards.
- English word, phonetic hint, Chinese meaning, usage note, and example sentence.
- Tap-to-expand card behavior.
- Vocabulary review mode with learned and needs-review states.
- Local progress persistence.

### Phrases and Sentences

- Common phrases grouped by usage context.
- Practical spoken sentences.
- Chinese translations.
- Word-by-word explanations.
- Usage context explanations.
- Audio playback for words and example sentences.

### Dialogues

- Scenario-based dialogues.
- Role-based turns, such as Coach and Student.
- English and Chinese display.
- Audio playback for dialogue turns.

### Scene Images

- Scene image page with generated or cached images.
- Multiple scene groups when vocabulary can be split into different visual contexts.
- Clickable word labels over scene images.
- Labels can play pronunciation and show Chinese explanations.

### Translation Practice

- Chinese-to-English practice prompts.
- AI correction when an API key is available.
- Corrected answer, natural answer, and Chinese feedback.

### Roleplay Practice

- Scenario-based roleplay tasks.
- The app can play a realistic conversation partner, such as a tennis coach.
- User answers can be corrected by AI.
- The app continues with the next turn.

### Pronunciation Modes

The app supports two pronunciation modes:

- Local mode: uses Android system text-to-speech and does not consume API tokens.
- High-quality mode: uses OpenAI speech generation for more natural American English audio and caches the result locally.

### Local Data Management

- Local learning pack list.
- Delete local learning packs.
- Local JSON storage.
- Local image and audio cache.
- Secure API key storage.

## Tech Stack

- Kotlin
- Jetpack Compose
- Android MVVM
- Repository pattern
- kotlinx.serialization
- Android app-specific file storage
- EncryptedSharedPreferences / AndroidX Security
- Media3 ExoPlayer
- Android TextToSpeech
- OpenAI API integration through a local `AiClient` abstraction

## Project Structure

```text
app/
  src/main/java/com/example/sceneenglish/
    MainActivity.kt
    app/
      AppContainer.kt
      AppNavGraph.kt
      AppViewModel.kt
      SceneEnglishApp.kt
    data/
      ai/
        AiClient.kt
        OpenAiClient.kt
        MockAiClient.kt
        MockLearningPack.kt
        AiPrompts.kt
      local/
        LocalFileStore.kt
        SecureSettingsStore.kt
        AppSettingsStore.kt
      repository/
        LearningPackRepository.kt
        AudioRepository.kt
        ImageRepository.kt
        PracticeRepository.kt
    domain/
      model/
    ui/
      screens/
      components/
      theme/
    util/
```

## Local Storage Layout

Learning data is stored in Android app-specific storage.

```text
files/
  learning_packs/
    index.json
    pack_xxx/
      learning_pack.json
      local_state.json
      images/
      audio/
```

The app intentionally avoids Room, SQLite, backend databases, user accounts, and cloud sync for the first version.

## AI API Usage

The app is designed for client-side API usage with a user-provided API key.

Current AI-related integrations include:

- Text generation and correction: OpenAI Responses API
- High-quality speech: OpenAI Audio Speech API
- Speech-to-text support: OpenAI Audio Transcriptions API
- Image generation: OpenAI Images API

Important: do not hardcode an API key into the app before publishing. APK files can be reverse engineered.

## Requirements

- Android Studio
- Android SDK
- JDK compatible with the Android Gradle Plugin
- Android 8.0 or later device/emulator
- Optional: OpenAI API key for AI-powered features

## Setup

1. Clone the repository.

   ```bash
   git clone <your-repo-url>
   cd AIEnglishSceneSpeak
   ```

2. Open the project in Android Studio.

3. Let Android Studio sync Gradle.

4. Make sure `local.properties` points to your Android SDK.

   Example:

   ```properties
   sdk.dir=/Users/yourname/Library/Android/sdk
   ```

5. Run the app on an emulator or Android device.

6. Open Settings in the app and enter your API key if you want to use AI-powered features.

## Command-Line Build

The repository includes the Gradle Wrapper.

```bash
./gradlew test
./gradlew assembleDebug
```

If your environment cannot write to the default Gradle cache directory, use a temporary Gradle home:

```bash
GRADLE_USER_HOME=/private/tmp/aienglishscenespeak-gradle-home ./gradlew test assembleDebug
```

## Install Debug APK

After building:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Launch the app:

```bash
adb shell am start -n com.example.sceneenglish/.MainActivity
```

## Release Build

Create a release build:

```bash
./gradlew assembleRelease
```

For public distribution, configure your own signing key in Android Studio or Gradle. Do not ship test keys or hardcoded API keys.

## API Key Safety

AIEnglishSceneSpeak is a local-first app. The API key is entered by the user and stored locally with Android secure storage.

For personal use or local development, direct client-to-AI API calls are convenient. For public production distribution, consider adding a backend proxy with proper authentication, rate limits, quota control, abuse prevention, and key protection.

## Development Notes

- Package name currently remains `com.example.sceneenglish`.
- App display name is `AIEnglishSceneSpeak`.
- The default fast learning pack currently uses a rich local tennis lesson scenario to avoid slow first-generation waits.
- `OpenAiClient` contains the direct API implementation.
- `MockAiClient` and `MockLearningPack` are useful for local development and tests.

## Testing

Run unit tests:

```bash
./gradlew test
```

The current tests cover utility behavior and grouped learning-pack generation.

## Roadmap Ideas

- More built-in scenario templates
- AI lazy learning mode
- Daily review queue
- Better spaced repetition
- More visual scene groups
- Export and import learning packs
- Anki export
- More offline pronunciation options
- Tablet layout
- Full package/applicationId rename before public release

## License

No license has been selected yet. Add a license before publishing if you want others to reuse or contribute to the project.

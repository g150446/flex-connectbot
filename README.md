# FlexConnectBot

FlexConnectBot is a [Secure Shell](https://en.wikipedia.org/wiki/Secure_Shell)
client for Android that lets you connect to remote servers over a
cryptographically secure link.


## Google Play

[![Get it on Google Play][2]][1]

  [1]: https://play.google.com/store/apps/details?id=org.flexconnectbot
  [2]: https://developer.android.com/images/brand/en_generic_rgb_wo_60.png


## Compiling

### Android Studio

FlexConnectBot is most easily developed in [Android Studio](
https://developer.android.com/studio/). You can import this project
directly from its project creation screen by importing from the GitHub URL.

### Command line

To compile FlexConnectBot using `gradlew`, you must first specify where your
Android SDK is via the `ANDROID_SDK_HOME` environment variable. Then
you can invoke the Gradle wrapper to build:

```sh
./gradlew build
```

### Continuous Integration

FlexConnectBot uses [GitHub Actions](https://github.com/g150446/flex-connectbot/actions)
for continuous integration. The workflow is defined in
`.github/workflows/ci.yml`.

#### Running Workflows Locally with act

In general, simply running `./gradlew build` should cover all the
checks run in the GitHub Actions continuous integration workflow, but you can
run GitHub Actions workflows locally using [`nektos/act`](https://github.com/nektos/act).
This requires Docker to be installed and running.

To run the main CI workflow (`ci.yml`):

```sh
act -W .github/workflows/ci.yml
```


## Translations

If you'd like to correct or contribute new translations to FlexConnectBot,
then head on over to the translations project.

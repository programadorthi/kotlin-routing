# multiplatform sample

### Running iOS
- IPhone: `./gradlew :samples:multiplatform-voyager:iosDeployIPhone8Debug`
- IPad: `./gradlew :samples:multiplatform-voyager:iosDeployIPadDebug`

### Running MacOS Native app (Desktop using Kotlin Native)
```shell
./gradlew :samples:multiplatform-voyager:runDebugExecutableMacosArm64
```

### Running JVM Native app (Desktop)
```shell
./gradlew :samples:multiplatform-voyager:run
```

### Running Web Compose Canvas
```shell
./gradlew :samples:multiplatform-voyager:jsBrowserDevelopmentRun
```

### Building Android App
```shell
./gradlew :samples:multiplatform-voyager:assembleDebug
```

If you want to run Android sample in the emulator, you can open the project and run the application configuration `samples.multiplatform-voyager` on Android Studio.

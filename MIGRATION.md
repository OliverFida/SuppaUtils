1. Followed Migration Steps from [fabricmc.com](https://docs.fabricmc.net/develop/migrating-mappings/loom)
2. Deleted `.gradle` & `build` directories
3. Executed `./gradlew clean build --refresh-dependencies` and `./gradlew genSources`
4. If step 2 & 3 didn't help to resolve error with Mixins do these:
   1. Repeat step 2 but also delete `%USERPROFILE%\.gradle\caches` directory
   2. Repeat step 3
5. Still no success?
   1. Repeat step 4.1 but also delete `.idea` directory
   2. Repeat step 3
   3. Execute `.\gradlew idea`
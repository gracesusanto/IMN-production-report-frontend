# Jetpack-Compose-BarCode-Scanner

## [Watch it On YouTube](https://youtu.be/A8r_sY7zJgE)

## License
```
Copyright 2020 MakeItEasyDev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

### Download
1. Build > Build App Bundle(s) / APK(s) > Build APK(s)
2. cd /Users/grace.susanto/Documents/juliano/imn/IMN-production-report-frontend/app/build/outputs/apk/debug
3. adb install app-debug.apk

### Build Issue
Error: Cannot use @TaskAction annotation on method DataBindingGenBaseClassesTask.writeBaseClasses() because interface org.gradle.api.tasks.incremental.IncrementalTaskInputs is not a valid parameter to an action method.
https://stackoverflow.com/questions/74025711/could-not-create-task-appcompiledebugkotlin-could-not-create-task-app
Problem: gradle version incompatibility while moving up from 7.x to 8.x
Fix: gradle-wrapper.properties --> distributionUrl=https\://services.gradle.org/distributions/gradle-7.6-bin.zip


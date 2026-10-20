# Runnable local build

The downloadable project ZIP includes `student-management-system.jar` here. It bundles the runtime libraries and UI.

Run the project with `run.bat`, or `java -jar release/student-management-system.jar --seed` after starting MongoDB. JDK 17+ is required. No Maven download is needed to run this JAR.

The JAR is Git-ignored so the source repository stays clean. Build your own with `mvn clean package`; the result is in `target/`. Dependency licenses/notices are retained in the shaded JAR's metadata; the project's MIT license applies to its own source.

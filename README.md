# compilation

If you just want to compile the library, but dont want the tests to be executed:
```./gradlew assemble```
If you want the tests to be compiled and run as well
```./gradlew build```
If you want to use the simulator from luposdate3000 or any external program
```./gradlew publishToMavenLocal```


# binaries

1. JS
    include this file in your html:
    ./build/distributions/simora.js
    For debugging purposes, place this file directly next to the previous one in the same folder.
    This will fail, if you rename the file.
    ./build/distributions/simora.js.map
2. JVM
    The main jar is:
    ./build/libs/simora-jvm-0.0.1.jar
    include all dependencies from this file:
    ./build/external_jvm_dependencies
    run it with:
    java -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt
    on Windows you must replace ":" with ";"
3. Linux
    Look into the subfolders of
    ./build/bin/linuxX64/

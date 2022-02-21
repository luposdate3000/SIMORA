In low-power networks, sending data over the network is one of the largest energy consumers.
Therefore, any application running in such an environment must reduce the volume of communication caused by their presence.
All state of the art network simulators focus on specific parts of the network stack.
Due to this specialization, the application interface is simplified in a way, that often only fictive and abstract applications can be simulated.
However, we want to gain insights about the interoperability between routing and real application for the purpose of reducing communication costs.
Therefore we have created SIMORA.

In wireless sensor networks all devices have the ability to communicate with each other.
Also some devices may be connected to the internet as well.
This allows the integration of stronger computers into the network.
One possibility is to integrate cloud based approaches, where the network delivers all of its data to the cloud.
In this scenario the cloud provides a huge amount of processing power to the application.
This however introduces a huge latency for trivial queries, because the data needs to be sent over a huge distance, before any evaluation can start.
Therefore the concept of edge computing emerged, such that medium sized devices can be integrated into the network as well.

This makes it possible to move the processing of innovative applications from the cloud to the sensors.
The aim of this relocation of applications is to minimize latencies in the network by shortening transmission paths.
Depending on the application, it might be possible to reduce the network traffic within the network as well.
The primary reason for this effect is, that the application may be able to discard irrelevant messages much earlier.
Additional application side compression can be much better than any generic compression.

To make most out of the available hardware, its properties needs to be propagated to the final application as well.
Only the application is able to apply the maximum fine-tuning.
Generic approaches may cover many cases as well, but no generic approach is able to prune unimportant information.
For this kind of fine tuning, the application needs an always up to date view of its surrounding network.
This information is already available within the routing protocol algorithms running on each device.
Therefore we publish this information to the application.

The state of the art in network simulators includes only scenarios without resource-intensive applications as for example a database.
As a consequence, the software stack does not consider this scenario as well.
To be able to test, evaluate and compare visionary applications, we need a virtual environment.

Another argument for the need of a virtual environment is, the heterogeneous nature of wireless sensor networks.
It is difficult to carry out standardized experiments.
And even if experiments are run, it is hard to repeat them.
Ultimately, the repeatability is also problematic, since an identical network structure has to be reconstructed.
This makes it almost impossible for other researchers to confirm experimental results.
However, it can also be a problem for the same research group, because after a few years the hardware has to be replaced or removed due to defects.

Furthermore, this environment needs to be very flexible to support various different use cases.
It won't make any sense to build a fixed environment, because then it wont be reusable in the first place.
For debugging and analysis reasons, it does make sense to repeat the experiment with a changed topology or different device properties.
Using real hardware has the disadvantage that it is difficult and expensive to configure different scenarios with different network topologies.

In this sense, the existing network simulators have a strong focus on the low level communication layers.
What is missing from our point of view is a simulator, which can simulate the low level communication layers but with the focus on the application layer.
With this simulated environment we want to be able to develop applications, which can fully utilize heterogeneous hardware.

What makes this simulator different:
* A simulator, which allows to simulate the interoperability between routing and application.
* A simulator, with the focus on high level applications in fog, cloud, edge and IoT scenarios.
* Multi platform support to experiment with distributed applications for teaching purposes in the browser.
* Simulating different network topologies to showcase the effects of lower communication layers in the application.
* Dynamic content multi cast, which allows the application on each device on the multi cast tree to modify the package contents.


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
    Run with
    ./build/bin/linuxX64/debugExecutable/simora.kexe 
    or
    ./build/bin/linuxX64/releaseExecutable/simora.kexe

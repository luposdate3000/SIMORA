#!/bin/bash
./gradlew build > x 2>y
grep -rlw "public fun main" | xargs sed "s/public fun main/public fun main/g" -i
grep "Modifier 'internal' is not applicable inside 'interface'" y | sed "s/^e: //g" | sed "s/:.*//g" | sort | uniq | xargs sed "s/ internal fun/ public fun/g" -i

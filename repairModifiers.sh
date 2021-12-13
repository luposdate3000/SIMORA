#!/bin/bash

#within simora itself

#combination with luposdate3000
for f in \
  $(grep "'internal' parameter type " ../luposdate3000/y | sed "s/.*'internal' parameter type //g" | sort | uniq | grep -v " ") \
  $(grep "'internal' parameter type argument " ../luposdate3000/y | sed "s/.*'internal' parameter type //g" | sort | uniq)
do
grep -rlw "internal class $f" | xargs sed "s/internal class $f/public class $f/g" -i
grep -rlw "internal interface $f" | xargs sed "s/internal interface $f/public interface $f/g" -i
done


./gradlew publishToMavenLocal > x 2>y
grep -rlw "public fun main" | xargs sed "s/public fun main/public fun main/g" -i
grep "Modifier 'internal' is not applicable inside 'interface'" y | sed "s/^e: //g" | sed "s/:.*//g" | sort | uniq | xargs sed "s/ internal fun/ public fun/g" -i
for f in \
  $(grep "'public' sub-interface exposes its 'internal' supertype" y | sed "s/.*'public' sub-interface exposes its 'internal' supertype //g" | sort | uniq) \
  $(grep "'public' function exposes its 'internal' parameter type " y | sed "s/.*'public' function exposes its 'internal' parameter type //g" | sort | uniq)
do
grep -rlw "internal interface $f" | xargs sed "s/internal interface $f/public interface $f/g" -i
grep -rlw "internal class $f" | xargs sed "s/internal class $f/public class $f/g" -i
done
./gradlew publishToMavenLocal

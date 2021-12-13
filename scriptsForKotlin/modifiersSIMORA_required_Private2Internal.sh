#!/bin/bash
./gradlew publishToMavenLocal > x 2>y
grep -rlw "internal fun main" | xargs sed "s/internal fun main/internal fun main/g" -i
grep "Modifier 'private' is not applicable inside 'interface'" y | sed "s/^e: //g" | sed "s/:.*//g" | sort | uniq | xargs sed "s/ private fun/ internal fun/g" -i
for f in \
  $(grep "'internal' sub-interface exposes its 'private' supertype" y | sed "s/.*'internal' sub-interface exposes its 'private' supertype //g" | sort | uniq) \
  $(grep "'internal' function exposes its 'private' parameter type " y | sed "s/.*'internal' function exposes its 'private' parameter type //g" | sort | uniq) \
  $(grep "'internal' subclass exposes its 'private' supertype " y | sed "s/.*'internal' subclass exposes its 'private' supertype //g" | sort | uniq)
do
grep -rlw "private interface $f" | xargs sed "s/private interface $f/internal interface $f/g" -i
grep -rlw "private class $f" | xargs sed "s/private class $f/internal class $f/g" -i
grep -rlw "private abstract class $f" | xargs sed "s/private abstract class $f/internal abstract class $f/g" -i
done
./gradlew publishToMavenLocal
git status

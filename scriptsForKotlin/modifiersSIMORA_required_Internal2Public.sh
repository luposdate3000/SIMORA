#!/bin/bash
./gradlew publishToMavenLocal > x 2>y
grep -rlw "public fun main" | xargs sed "s/public fun main/public fun main/g" -i
grep "Modifier 'internal' is not applicable inside 'interface'" y | sed "s/^e: //g" | sed "s/:.*//g" | sort | uniq | xargs sed "s/ internal fun/ public fun/g" -i
for f in \
  $(grep "'public' sub-interface exposes its 'internal' supertype" y | sed "s/.*'public' sub-interface exposes its 'internal' supertype //g" | sort | uniq) \
  $(grep "'public' function exposes its 'internal' parameter type " y | sed "s/.*'public' function exposes its 'internal' parameter type //g" | sort | uniq) \
  $(grep "'public' subclass exposes its 'internal' supertype " y | sed "s/.*'public' subclass exposes its 'internal' supertype //g" | sort | uniq)
do
grep -rlw "internal interface $f" | xargs sed "s/internal interface $f/public interface $f/g" -i
grep -rlw "internal class $f" | xargs sed "s/internal class $f/public class $f/g" -i
grep -rlw "internal abstract class $f" | xargs sed "s/internal abstract class $f/public abstract class $f/g" -i
done
./gradlew publishToMavenLocal
git status

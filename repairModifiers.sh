#!/bin/bash


#reset ...

find -name *.kt | xargs sed "s/public fun/internal fun/g" -i
find -name *.kt | xargs sed "s/public var/internal var/g" -i
find -name *.kt | xargs sed "s/public val/internal val/g" -i
find -name *.kt | xargs sed "s/public class/internal class/g" -i
find -name *.kt | xargs sed "s/public abstract class/internal abstract class/g" -i
find -name *.kt | xargs sed "s/public interface/internal interface/g" -i

exit
#combination with luposdate3000

for f in \
  $(grep "'internal' parameter type " ../luposdate3000/y | sed "s/.*'internal' parameter type //g" | sort | uniq | grep -v " ") \
  $(grep "'internal' parameter type argument " ../luposdate3000/y | sed "s/.*'internal' parameter type //g" | sort | uniq) \
  $(grep "Cannot access '.*': it is internal in '.*'" ../luposdate3000/y | sed "s/.*Cannot access '//g" | sed "s/'.*//g" | sort | uniq)
do
grep -rlw "internal class $f" | xargs sed "s/internal class $f/public class $f/g" -i
grep -rlw "internal interface $f" | xargs sed "s/internal interface $f/public interface $f/g" -i
grep -rlw "internal var $f" | xargs sed "s/internal var $f/public var $f/g" -i
grep -rlw "internal val $f" | xargs sed "s/internal val $f/public val $f/g" -i
done
for f in \
  $(grep "Cannot access '<init>': it is internal in '" ../luposdate3000/y | sed "s/.*Cannot access '<init>': it is internal in '//g"| sed "s/'.*//g" | sort | uniq)
do
  find -name "$f.kt" | xargs sed "s/internal constructor/public constructor/g" -i
done

for ff in \
 $(grep "Cannot access '.*': it is internal in '.*'" ../luposdate3000/y | sed "s/.*Cannot access '//g" | sed "s/': it is internal in '/;/g" | sed "s/'.*//g" | sort | uniq)
do
arrIN=(${ff//;/ })
find -name "${arrIN[1]}.kt" | xargs sed "s/internal fun ${arrIN[0]}/public fun ${arrIN[0]}/g" -i
done

#within simora itself

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




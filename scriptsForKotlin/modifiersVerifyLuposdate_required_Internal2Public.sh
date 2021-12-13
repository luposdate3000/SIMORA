#!/bin/bash
for f in \
  $(grep "'internal' parameter type " ../luposdate3000/y | sed "s/.*'internal' parameter type //g" | sort | uniq | grep -v " ") \
  $(grep "'internal' parameter type argument " ../luposdate3000/y | sed "s/.*'internal' parameter type //g" | sort | uniq) \
  $(grep "Cannot access '.*': it is internal in '.*'" ../luposdate3000/y | sed "s/.*Cannot access '//g" | sed "s/'.*//g" | sort | uniq)
do
grep -rlw "internal class $f" | xargs sed "s/internal class $f/public class $f/g" -i
grep -rlw "internal interface $f" | xargs sed "s/internal interface $f/public interface $f/g" -i
grep -rlw "internal var $f" | xargs sed "s/internal var $f/public var $f/g" -i
grep -rlw "internal lateinit var $f" | xargs sed "s/internal lateinit var $f/public lateinit var $f/g" -i
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


for f in \
  $(grep "'private' parameter type " ../luposdate3000/y | sed "s/.*'private' parameter type //g" | sort | uniq | grep -v " ") \
  $(grep "'private' parameter type argument " ../luposdate3000/y | sed "s/.*'private' parameter type //g" | sort | uniq) \
  $(grep "Cannot access '.*': it is private in '.*'" ../luposdate3000/y | sed "s/.*Cannot access '//g" | sed "s/'.*//g" | sort | uniq)
do
grep -rlw "private class $f" | xargs sed "s/private class $f/public class $f/g" -i
grep -rlw "private interface $f" | xargs sed "s/private interface $f/public interface $f/g" -i
grep -rlw "private var $f" | xargs sed "s/private var $f/public var $f/g" -i
grep -rlw "private lateinit var $f" | xargs sed "s/private lateinit var $f/public lateinit var $f/g" -i
grep -rlw "private val $f" | xargs sed "s/private val $f/public val $f/g" -i
done
for f in \
  $(grep "Cannot access '<init>': it is private in '" ../luposdate3000/y | sed "s/.*Cannot access '<init>': it is private in '//g"| sed "s/'.*//g" | sort | uniq)
do
  find -name "$f.kt" | xargs sed "s/private constructor/public constructor/g" -i
done

for ff in \
 $(grep "Cannot access '.*': it is private in '.*'" ../luposdate3000/y | sed "s/.*Cannot access '//g" | sed "s/': it is private in '/;/g" | sed "s/'.*//g" | sort | uniq)
do
arrIN=(${ff//;/ })
find -name "${arrIN[1]}.kt" | xargs sed "s/private fun ${arrIN[0]}/public fun ${arrIN[0]}/g" -i
done


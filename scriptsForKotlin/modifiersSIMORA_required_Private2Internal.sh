#!/bin/bash

./gradlew publishToMavenLocal > x 2>y

for f in \
  $(grep "Cannot access '.*': it is private in file" y | sed "s/.*Cannot access '//g" | sed "s/'.*//g" | sort | uniq)
do
grep -rlw "private interface $f" | xargs sed "s/private interface $f/internal interface $f/g" -i
grep -rlw "private class $f" | xargs sed "s/private class $f/internal class $f/g" -i
grep -rlw "private abstract class $f" | xargs sed "s/private abstract class $f/internal abstract class $f/g" -i
grep -rlw "private fun $f" | xargs sed "s/private fun $f/internal fun $f/g" -i
done

for ff in \
  $(grep "Cannot access '.*': it is private in '.*'" y | sed "s/.*Cannot access '//g" | sed "s/': it is private in '/;/g" | sed "s/'.*//g" | sort | uniq) \
  $(grep "Cannot access '.*': it is invisible (private in a supertype) in '.*'" y | sed "s/.*Cannot access '//g" | sed "s/': it is invisible (private in a supertype) in '/;/g"| sed "s/'.*//g" | sort | uniq)
do
arrIN=(${ff//;/ })
find -name "${arrIN[1]}.kt" | xargs sed "s/private fun ${arrIN[0]}/internal fun ${arrIN[0]}/g" -i
find -name "${arrIN[1]}.kt" | xargs sed "s/private val ${arrIN[0]}/internal val ${arrIN[0]}/g" -i
find -name "${arrIN[1]}.kt" | xargs sed "s/private var ${arrIN[0]}/internal var ${arrIN[0]}/g" -i
find -name "${arrIN[1]}.kt" | xargs sed "s/private lateinit var ${arrIN[0]}/internal lateinit var ${arrIN[0]}/g" -i
done

./gradlew publishToMavenLocal
git status

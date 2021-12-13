#!/bin/bash
find -name *.kt | xargs sed "s/internal fun/private fun/g" -i
find -name *.kt | xargs sed "s/internal var/private var/g" -i
find -name *.kt | xargs sed "s/internal lateinit var/private lateinit var/g" -i
find -name *.kt | xargs sed "s/internal val/private val/g" -i
find -name *.kt | xargs sed "s/internal class/private class/g" -i
find -name *.kt | xargs sed "s/internal abstract class/private abstract class/g" -i
find -name *.kt | xargs sed "s/internal interface/private interface/g" -i

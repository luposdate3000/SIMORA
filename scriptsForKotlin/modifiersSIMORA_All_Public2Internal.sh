#!/bin/bash
find -name *.kt | xargs sed "s/public fun/internal fun/g" -i
find -name *.kt | xargs sed "s/public var/internal var/g" -i
find -name *.kt | xargs sed "s/public lateinit var/internal lateinit var/g" -i
find -name *.kt | xargs sed "s/public val/internal val/g" -i
find -name *.kt | xargs sed "s/public class/internal class/g" -i
find -name *.kt | xargs sed "s/public abstract class/internal abstract class/g" -i
find -name *.kt | xargs sed "s/public interface/internal interface/g" -i

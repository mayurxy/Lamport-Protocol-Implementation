#!/bin/bash

# Root directory of your project
#PROJDIR=$HOME/AOS/Project1
#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
#CONFIG=$PROJDIR/config.txt
#
# Directory your java classes are in
#
#BINDIR=$PROJDIR/bin
#
# Your main project class
#

rm *.class
javac Test.java

java Test

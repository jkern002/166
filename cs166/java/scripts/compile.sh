#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

#set the script to exit out on error
set -e

# compile the java program
javac -d $DIR/../classes $DIR/../src/Cafe.java

#run the java program
#Use your database name and portss
java -cp $DIR/../classes:$CLASSPATH Cafe mydb $PGPORT


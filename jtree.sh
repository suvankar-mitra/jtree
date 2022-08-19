#!/bin/bash

#########################################
# Check if Java is installed
#########################################
java_v=$(java --version)
if [ "$java_v" == "" ]; then
  echo ">> Java is not installed in your system!"
  echo ">> Please consider installing Java Runtime to execute this program."
  echo ">> https://openjdk.org/install/"
  exit 1
else
  echo ">> Java is installed in your system!"
  echo "$java_v"
fi
echo ""

#########################################
# Create jar
#########################################
echo ">> Maven build"
mvn clean package --file pom.xml

echo ">> Jar created"
ls ./target/jtree*-jar-with-dependencies.jar

#########################################
# Run
#########################################
echo ">> Run jtree [java -cp ./target/jtree*-jar-with-dependencies.jar com.suvmitra.jtree.MainKt ./]"
java -cp ./target/jtree*-jar-with-dependencies.jar com.suvmitra.jtree.MainKt ./
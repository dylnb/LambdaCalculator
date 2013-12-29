#!/bin/sh

# This script puts all of the class files in the required
# libraries into the main LambdaCalculator.jar file so
# that it has no other dependencies.

cd dist
jar xf lib/swing-layout-1.0.4.jar
jar xf lib/AbsoluteLayout.jar
jar uf LambdaCalculator.jar org


cd ..
if [ -z "$1" -o "$1" == "teacher" ]
then
  launch4j/launch4j LC_TE.xml
else
  if [ "$1" == "student" ]
  then
    launch4j/launch4j LC_SE.xml
  else
    echo "Argument should be student or teacher"
    exit 1
  fi
fi

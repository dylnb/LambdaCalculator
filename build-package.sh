#!/bin/sh

# This script puts all of the class files in the required
# libraries into the main LambdaCalculator.jar file so
# that it has no other dependencies.

cd dist
$JAVA_HOME/bin/jar xf lib/swing-layout-1.0.jar
$JAVA_HOME/bin/jar xf lib/AbsoluteLayout.jar
$JAVA_HOME/bin/jar uf LambdaCalculator.jar org

cd ..
launch4j/launch4j ../launch4j.xml


#!/bin/sh

# This script puts all of the class files in the required
# libraries into the main LambdaCalculator.jar file so
# that it has no other dependencies.

cd dist
jar xf lib/swing-layout-1.0.4.jar
jar xf lib/AbsoluteLayout.jar
jar uf LambdaCalculator.jar org


cd ..
launch4j/launch4j ../LC2.xml


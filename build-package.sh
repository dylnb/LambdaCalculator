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

# Then, to build the mac dmgs:
#
# $JAVA_HOME/bin/javapackager -deploy \
#   -title "Lambda Calculator TE" \
#   -name "Lambda Calculator TE" \
#   -appclass lambdacalc.Main \
#   -native dmg \
#   -outdir apps/osx-teacher \
#   -outfile LCTE \
#   -srcdir dist \
#   -srcfiles LambdaCalculator.jar \
#   -Bicon=images/hat-logo-teacher.icns
#
# $JAVA_HOME/bin/javapackager -deploy \
#   -title "Lambda Calculator SE" \
#   -name "Lambda Calculator SE" \
#   -appclass lambdacalc.Main \
#   -native dmg \
#   -outdir apps/osx-student \
#   -outfile LCSE \
#   -srcdir dist \
#   -srcfiles LambdaCalculator.jar \
#   -Bicon=images/hat-logo-student.icns

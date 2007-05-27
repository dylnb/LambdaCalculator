Lambda: The Semantics Tool
==========================

Joint work by Lucas Champollion, Josh Tauberer and Maribel Romero
Department of Linguistics, University of Pennsylvania
Supported by a University of Pennsylvania SAS technology grant.

Lambda is an interactive, graphics-based pedagogical software that helps 
students of formal semantics practice the typed lambda calculus.

Up-to-date information about this project is on the Lambda
website:
  http://www.ling.upenn.edu/lambda/
and its SourceForge page:
  http://sourceforge.net/projects/lambdacalc

Copying
-------

This project is distributable under the terms of the GNU GPL
(the latest version).

Building
--------

The project is meant to be built with NetBeans.

To create the packaged Windows EXE, you must get launch4j from 
http://launch4j.sourceforge.net/ and extract the files into the 
"launch4j" subdirectory of this directory.  We've been using version 
2.1.3 of launch4j.

The build-package.sh script in this directory is a post-build step
that 1) combines all of the dependency jar files in 'dist' into
to the main dist/LambdaCalculator.jar file so that that jar has no
dependencies during distribution, and 2) executes launch4j to
build the nice wrapped Windows EXE (Lambda.exe in this directory).

Running
-------

To run the .jar package dist/LambdaCalculator.jar, just run
  java -jar dist/LambdaCalculator.jar


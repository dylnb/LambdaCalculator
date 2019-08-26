# **Launch4j Instructions**


This update provides a new windows runnable application using the launch4j exe wrapper. We have configured the wrapper to also bundle JRE 1.8 with the application so as to let windows users not need to download Java. The way to use launch4j is as follows: 


1. Download the zip file of the Lambda Calculator project from github.
2. Unzip the file.
3. To open the launch4j executable, open the file and go to launch4j.
4. Find the executable file named “Launch4j” and double click it. A GUI will open for launch4j.
5. Open the LC_SE.xml file to edit.
6. On mac, open terminal and cd into the zip file.
7. Run sh build-package.sh student on terminal.
8. This will create an exe file in the launch4j folder of the project, as well as a dmg file for mac in apps/osx-student. 
9. To update the windows version, zip the .exe file and the JRE folder into a single zip, and it is ready for distribution. 


### **NOTE 1**: 

The sh command will not run if the file is being imported from a zip to netbeans and possibly other IDEs as they remove the executable permissions from all files necessary to run the launch4j module. 


### **NOTE 2**: The jdkpackager does not work if you have $JAVA_HOME set to JDK 11+. It is best to set $JAVA_HOME to a lower JDK. For future reference, Java is planning on providing a new JDKpackager in Java 13 using JLinks.

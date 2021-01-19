# **Packaging instructions for new releases**

This update provides a new windows runnable application using the launch4j exe wrapper. We have configured the wrapper to also bundle JRE 1.8 with the application so as to let windows users not need to download Java. The way to use launch4j is as follows: 

1. Make sure the $JAVA_HOME variable on your machine is set to JDK version 1.8.0. Building the application using a later release can lead to issues (see Note 2).
2. Make sure the version number and year are updated in `LambdaCalculator/build-package.sh` on github.
3. Download the zip file of the Lambda Calculator project from github.
4. Unzip the file.
5. Open the Java project from the unzipped folder with your favorite IDE.
6. Check whether GOD_MODE is set to true or false on Main.java.
7. If true, proceed using the `TE` version, otherwise use the `SE`. 
8. Clean and build the project.
9. To open the launch4j executable, open the Lambda Calculator folder and go to the launch4j folder.
10. Find the executable file named **Launch4j** and double click it. A GUI will open for launch4j.
11. Press open on the top left, and open the `LC_SE.xml` OR `LC_TE.xml` file in the launch4j directory to edit the configurations.
12. On a Mac, open terminal and cd into the unzipped folder from Step 2.
13. Run `sh build-package.sh student` OR `sh build-package.sh teacher` on terminal.
14. This will create an exe file in the launch4j folder of the project, as well as a dmg file for Mac in apps/osx-student. Note: As of 2020 in Mac OSX Catalina, the dmg file is created within a temporary directory and docked. To get the location of the temporary directory, right click on the application, click Get Info, and see the filepath under Disk Image.
15. Create a new folder in `launch4j` and name it `LCSE` OR `LCTE`.
16. Copy `jre180` and `Lambda Calculator SE.exe` OR `Lambda Calculator TE.exe` into the folder.
17. Cd into `launch4j/LCSE` or `launch4j/LCTE` from terminal then type `ls -la` to see if there are any extra files besides `.` and `..`, such as `.DS_Store`.
18. Delete these extra files using `rm -r filename`. Make sure you don't have finder open pointing to the folder.
19. Cd back into `launch4j` and create a zip file using `zip -r LCSE.zip LCSE` OR `zip -r LCTE.zip LCTE`. 
20. The zip file is now ready to distribute.
21. In Main.java, set GOD_MODE to the other boolean and repeats steps 6-19. 
22. Test whether the version number was set correctly by installing the TE and SE versions and looking at the About window in Mac. 
23. When you are ready to release the new version, click on `Releases`, located on the righthand side of the repository in github, then click on `Draft a new release`.
24. Once you've published the new release, go to the `gh-pages` branch in github and open `index.html`. Under `<!-- Download -->`, replace the hyperlinks to link to the newly released versions of the Lambda Calculator.

NOTE 1: The `sh` command will not run if the file is being imported from a zip to netbeans and possibly other IDEs as they remove the executable permissions from all files necessary to run the launch4j module. 

NOTE 2: The jdkpackager does not work if you have $JAVA_HOME set to JDK 11+. It is best to set $JAVA_HOME to a lower JDK, preferably JDK 8. For future reference, Java is planning on providing a new JDKpackager in Java 13 using JLinks. 

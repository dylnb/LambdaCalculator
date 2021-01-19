# **Packaging instructions for new releases**

This update provides a new windows runnable application using the launch4j exe wrapper. We have configured the wrapper to also bundle JRE 1.8 with the application so as to let windows users not need to download Java. The way to use launch4j is as follows: 

1. Add a new entry to the change log in `LambdaCalculator/docs/change-log.md` summarizing changes in the new version.
2. Make sure the `$JAVA_HOME` variable on your machine is set to JDK version 1.8.0. An example of a proper path for `$JAVA_HOME` on Mac is `/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home`. Building the application using a later release can lead to issues (see Note 2).
3. Update the version number, year, and author names in `LambdaCalculator/build-package.sh` and `LambdaCalculator/src/lambdacalc/Main.java`.
4. To open the launch4j executable, open the Lambda Calculator folder and go to the launch4j folder.
5. Find the executable file named **Launch4j** and double click it. A GUI will open for launch4j.
6. Press open on the top left, and open the `LC_SE.xml` file in the launch4j directory. Edit the configurations in the Version Info tab, updating the year, version number, and authors, and save the file. Do the same for the `LC_TE.xml` file.
7. Push these changes and the final version code to the Github `build` branch, and create a pull request to merge the `build` branch into the `master` branch.
8. Check that the newly merged `master` branch functions as expected by building and running both the student and teacher editions (Change the `GOD_MODE` variable in `LambdaCalculator/src/lambdacalc/Main.java` to `false` for the student edition and `true` for the teacher edition). Once you've completed this check, download the zip file of the `master` branch from github.
9. Unzip the file.
10. Open the Java project from the unzipped folder with your favorite IDE.
11. Check whether the `GOD_MODE` variable is set to true or false in `LambdaCalculator/src/lambdacalc/Main.java`.
12. If true, proceed using the `TE` version, otherwise use the `SE`. 
13. Clean and build the project.
14. On a Mac, open terminal and cd into the unzipped folder from Step 9.
15. Run `sh build-package.sh student` OR `sh build-package.sh teacher` on terminal. On Mac OS version 11.0.1 (Big Sur), a dialog window will open, saying '[application_name] cannot be opened because the developer cannot be verified.' Click Cancel on this window, navigate to your Mac's System Preferences, and click on Security and Privacy. Click on 'Allow anyway' to allow your Mac to open the application. Run the `sh build-package.sh student` OR `sh build-package.sh teacher` command again, and another window should appear, saying 'MacOS cannot verify the developer of [application name]. Are you sure you want to open it?'. This time, click Open. Repeat this process for each application.
16. This will create an exe file in the launch4j folder of the project, as well as a dmg file for Mac in apps/osx-student (some error messages are expected, see Note 3). Note: As of 2020 in Mac OSX Catalina, the dmg file is created within a temporary directory and docked. To get the location of the temporary directory, right click on the application, click Get Info, and see the filepath under Disk Image.
17. Create a new folder in `launch4j` and name it `LCSE` OR `LCTE`.
18. Copy `jre1.8.0_221.jre` and `Lambda Calculator SE.exe` OR `Lambda Calculator TE.exe` into the folder. Rename `jre1.8.0_221.jre` to `jre180`.
19. Cd into `launch4j/LCSE` or `launch4j/LCTE` from terminal then type `ls -la` to see if there are any extra files besides `.` and `..`, such as `.DS_Store`.
20. Delete these extra files using `rm -r filename`. Make sure you don't have finder open pointing to the folder.
21. Cd back into `launch4j` and create a zip file using `zip -r LCSE.zip LCSE` OR `zip -r LCTE.zip LCTE`. 
22. The zip file is now ready to distribute.
23. In Main.java, set GOD_MODE to the other boolean and repeats steps 15-21. 
24. Test whether the version number was set correctly by installing the TE and SE versions and looking at the About window in Mac. 
25. When you are ready to release the new version, click on `Releases`, located on the righthand side of the repository in github, then click on `Draft a new release`.
26. Once you've published the new release, go to the `gh-pages` branch in github and open `index.html`. Under `<!-- Download -->`, replace the hyperlinks to link to the newly released versions of the Lambda Calculator.

NOTE 1: The `sh` command will not run if the file is being imported from a zip to Netbeans and possibly other IDEs as they remove the executable permissions from all files necessary to run the launch4j module. 

NOTE 2: The jdkpackager does not work if you have $JAVA_HOME set to JDK 11+. It is best to set $JAVA_HOME to a lower JDK, preferably JDK 8. For future reference, Java is planning on providing a new JDKpackager in Java 13 using JLinks. 

NOTE 3: Some errors are to be expected even after completing Step 15 successfully. Below is an example of the expected terminal output:
```
/var/folders/y5/5mgmtppn1kb_8z2wcf5p81qc0000gn/T/fxbundler7017394988225596965/macosx/Lambda Calculator SE-dmg-setup.scpt:644:751: execution error: Finder got an error: The operation can’t be completed because there is already an item with that name. (-48)
java.io.IOException: Exec failed with code 1 command [[osascript, /var/folders/y5/5mgmtppn1kb_8z2wcf5p81qc0000gn/T/fxbundler7017394988225596965/macosx/Lambda Calculator SE-dmg-setup.scpt] in unspecified directory
at com.oracle.tools.packager.IOUtils.exec(IOUtils.java:165)
at com.oracle.tools.packager.IOUtils.exec(IOUtils.java:138)
at com.oracle.tools.packager.IOUtils.exec(IOUtils.java:132)
at com.oracle.tools.packager.mac.MacDmgBundler.buildDMG(MacDmgBundler.java:376)
at com.oracle.tools.packager.mac.MacDmgBundler.bundle(MacDmgBundler.java:92)
at com.oracle.tools.packager.mac.MacDmgBundler.execute(MacDmgBundler.java:556)
at com.sun.javafx.tools.packager.PackagerLib.generateNativeBundles(PackagerLib.java:352)
at com.sun.javafx.tools.packager.PackagerLib.generateDeploymentPackages(PackagerLib.java:319)
at com.sun.javafx.tools.packager.Main.main(Main.java:476)
  Config files are saved to /var/folders/y5/5mgmtppn1kb_8z2wcf5p81qc0000gn/T/fxbundler7017394988225596965/macosx. Use them to customize package.
Exception in thread "main" com.sun.javafx.tools.packager.PackagerException: Error: Bundler "DMG Installer" (dmg) failed to produce a bundle.
at com.sun.javafx.tools.packager.PackagerLib.generateNativeBundles(PackagerLib.java:354)
at com.sun.javafx.tools.packager.PackagerLib.generateDeploymentPackages(PackagerLib.java:319)
at com.sun.javafx.tools.packager.Main.main(Main.java:476)
```

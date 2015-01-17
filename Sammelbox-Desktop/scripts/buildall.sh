#!/bin/bash

clear

echo "-------------------------------------------------------------------------"
echo "------------------------- PLEASE READ CAREFULLY -------------------------"
echo "-------------------------------------------------------------------------"
echo " This script should only be used to build multiple jars at once and is"
echo " NOT NEEDED if you want to build Sammelbox on your local machine!"
echo " Instead, please use 'mvn clean install' to automatically build an"
echo " executable with respect your operating system."
echo " (Also see: https://github.com/jeromewagener/Sammelbox/wiki/Building-Sammelbox-using-Maven)"
echo " FYI, lets just say it don't 'like' the Maven release plugin ;-)" 
echo "-------------------------------------------------------------------------"
echo " Important to know:"
echo "-------------------------------------------------------------------------"
echo "  *) The builds will be based on the current pom.xml specification"
echo "      i.e: " `grep -m 1 '<version>' ../pom.xml`
echo "  *) In order to enable the compilation for different platforms,"
echo "      TESTS will be disabled"
echo "  *) If you use this script to build 'release' versions, please not that"
echo "      the pom.xml needs to be manually updated! (e.g. version numbers)"
echo "-------------------------------------------------------------------------"
read -p "Do you want to continue (y/n)? " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    exit 1
fi

cd ..

# clean the release folder if it exists
rm -rf release

# (re)create emptry release folder
mkdir release


# build linux x64 jar and copy jar
mvn clean install -DskipTests -P'linux64,!linux32,!win64,!win32,!macos64,!macos32'
cp target/*.jar release

# build linux x32 jar and copy jar
#mvn clean install -DskipTests -P'linux32,!linux64,!win64,!win32,!macos64,!macos32'
#cp target/*.jar release


# build win x64 jar and copy jar
mvn clean install -DskipTests -P'win64,!win32,!linux64,!linux32,!macos64,!macos32'
cp target/*.jar release

# build win x32 jar and copy jar
#mvn clean install -DskipTests -P'win32,!win64,!linux64,!linux32,!macos64,!macos32'
#cp target/*.jar release


# build macos x32 jar and copy jar
#mvn clean install -DskipTests -P'macos64,!macos32,win64,!win32,!linux64,!linux32'
#cp target/*.jar release

# build macos x64 jar and copy jar
mvn clean install -DskipTests -P'macos32,!macos64,win64,!win32,!linux64,!linux32'
cp target/*.jar release

cd scripts

echo "----------------------------------"
echo "--- $0 successfully finished.. "
echo "----------------------------------"

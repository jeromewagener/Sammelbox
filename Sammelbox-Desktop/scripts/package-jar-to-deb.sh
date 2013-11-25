#!/bin/bash

clear

# Check parameters
if [ $# -ne 4 ]
then
    echo "--------------------------------------------------------------------------" 
    echo "USAGE:   " $0 "<sammelbox-jar-file-path> <mail-address> <name-for-deb-package-with-version)> <arch(amd64/i386)>"
    echo "EXAMPLE: " $0 "/home/user/jerome/sammelbox-xx.jar mail@example.com sammelbox-1.0-beta amd64"
    echo "--------------------------------------------------------------------------"
    echo "README: please make sure that you are following these rules before creating a package for release purposes!"
    echo "   1) you have assigned the correct jar with the correct architecture"
    echo "   2) you have adapted all the versions and the changelogs"
    echo "   3) you know what you are doing :-)"
    echo "--------------------------------------------------------------------------"
    exit
else
    echo "--------------------------------------------------------------------------"
    echo "README: please make sure that you are following these rules before creating a package for release purposes!"
    echo "   1) you have assigned the correct jar with the correct architecture"
    echo "   2) you have adapted all the versions and the changelogs"
    echo "   3) you know what you are doing :-)"
    echo "--------------------------------------------------------------------------"

    read -p "Do you want to continue (y/n)? " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]
    then
        exit 1
    fi
fi

# Remember current working directory
CURRENT_DIR=`pwd`

# Delete package folder if present
if [ -d $CURRENT_DIR/../package ]
then
   read -p "Do you want to clean `readlink -m $CURRENT_DIR/../package` from existing *.deb packages (y/n)? "

   if [[ ! $REPLY =~ ^[Nn]$ ]]
   then
      rm -rf $CURRENT_DIR/../package
   fi
fi

# Delete sandbox if present
if [ -d /tmp/sammelbox-sandbox ]
then
   rm -rf /tmp/sammelbox-sandbox
fi

# Create new sandbox
mkdir /tmp/sammelbox-sandbox

# Create work folder inside
mkdir /tmp/sammelbox-sandbox/$3

# Copy "files-for-app" into sandbox
cp debArchiveFiles/filesForApp/* /tmp/sammelbox-sandbox/$3

# Copy jar into deb folder and rename to it sammelbox.jar
cp $1 /tmp/sammelbox-sandbox/$3/sammelbox.jar

# Create tar.gz of sandbox and copy inside sandbox
tar -czf /tmp/sammelbox-sandbox/$3/$3.tar.gz -C /tmp/sammelbox-sandbox $3

# Create deb files
cd /tmp/sammelbox-sandbox/$3
dh_make -e $2 -c gpl3 -s -f /tmp/sammelbox-sandbox/$3/$3.tar.gz

# remove tar.gz
rm -rf /tmp/sammelbox-sandbox/$3/$3.tar.gz

# from new debian dir, remove .ex and readme files
rm -rf /tmp/sammelbox-sandbox/$3/debian/*.EX
rm -rf /tmp/sammelbox-sandbox/$3/debian/*.ex
rm -rf /tmp/sammelbox-sandbox/$3/debian/*README*
rm -rf /tmp/sammelbox-sandbox/$3/debian/*readme*

# overwrite files from within files-for-debian
cp -f $CURRENT_DIR/debArchiveFiles/filesForDebian/* /tmp/sammelbox-sandbox/$3/debian

# Replace architecture in control file
sed -i s/VAR_ARCH/$4/g /tmp/sammelbox-sandbox/$3/debian/control

# execute fakeroot dpkg-buildpackage -F (Specifies a normal full build, binary and source packages will be built. This is the same as the default case when no build option is specified)
fakeroot dpkg-buildpackage -F -a$4

# recreate package folder (if necessary)
if [ ! -d $CURRENT_DIR/../package ]
then
   mkdir $CURRENT_DIR/../package
fi

# copy created *.deb under new name into sammelbox/Sammelbox-Desktop/packages
cp -f /tmp/sammelbox-sandbox/*.deb $CURRENT_DIR/../package

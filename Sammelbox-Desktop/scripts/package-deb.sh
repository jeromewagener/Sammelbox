#!/bin/bash

# Check parameters
if [ $# -ne 3 ] 
then
    echo "Usage: " $0 "<sammelbox-jar-file-path> <mail-address> <name-for-deb-package-with-version)>"
    echo "Example: " $0 "/home/user/jerome/sammelbox-xx.jar mail@example.com sammelbox-1.0-beta"
fi

# Remember current working directory
CURRENT_DIR=`pwd`

# Delete package folder if present
if [ -d $CURRENT_DIR/../package ]
then
   rm -rf $CURRENT_DIR/../package
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

# execute fakeroot dpkg-buildpackage -F
fakeroot dpkg-buildpackage -F

# recreate package folder
mkdir $CURRENT_DIR/../package

# copy created *.deb under new name into sammelbox/Sammelbox-Desktop/packages
cp /tmp/sammelbox-sandbox/*.deb $CURRENT_DIR/../package

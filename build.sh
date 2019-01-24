#!/bin/bash

ID=ti.fidel
VERSION=1.0.0
MODULENAME=$ID-android-$VERSION.zip
LIBS=android/nativelibs
MODULE=android/dist/$MODULENAME
ARCHS=( "arm64-v8a" "armeabi-v7a" "x86" )
rm $MODULE
ti build -b -p android -d android
cd android/dist/
unzip  *.zip
for a in "${ARCHS[@]}"
do 
   cp -v ../../$LIBS/$a/*   modules/android/$ID/$VERSION/libs/$a/	
done
zip -umr $MODULENAME modules/
unzip -ou $MODULENAME -d '/Users/fuerst/Library/Application Support/Titanium/'
cd ../../
git add android/src/*
git commit -m " "
git push origin master
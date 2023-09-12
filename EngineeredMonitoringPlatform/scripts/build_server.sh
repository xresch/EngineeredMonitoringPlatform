#!bash

FOLDER_CFW=../../../CoreFramework
FOLDER_EMP=../
FOLDER_TARGET=$FOLDER_EMP/target


#================================
# Create EMP Binaries
#================================
mvn clean install -f $FOLDER_EMP

#================================
# Create CFW Binaries
#================================
mvn clean install -f $FOLDER_CFW

#================================
# Create EMP Server Folder
#================================
VERSION=$(basename $FOLDER_TARGET/*.jar | sed -r -e 's/.*-(.*).jar/\1/')
FOLDER_EMPSERVER=$FOLDER_TARGET/EMPServer_v$VERSION

echo "--------- build_server.sh -----------"
echo "Create Folder $FOLDER_EMPSERVER"
cp -R $FOLDER_CFW/target/CFWServer_*/ $FOLDER_EMPSERVER

#================================
# Replace CFW with EMP files
#================================
echo "Copy ./resources"
rm -R $FOLDER_EMPSERVER/resources
cp -R $FOLDER_EMP/resources $FOLDER_EMPSERVER

echo "Copy ./config"
rm -R $FOLDER_EMPSERVER/config
cp -R $FOLDER_EMP/config $FOLDER_EMPSERVER

echo "Copy ./LICENSE"
rm -R $FOLDER_EMPSERVER/LICENSE
cp $FOLDER_EMP/LICENSE $FOLDER_EMPSERVER

echo "Copy $FOLDER_TARGET/*.jar"
cp $FOLDER_TARGET/*.jar $FOLDER_EMPSERVER/extensions
echo "Copy $FOLDER_TARGET/lib to $FOLDER_EMPSERVER/extensions"
cp -R $FOLDER_TARGET/lib/* $FOLDER_EMPSERVER/extensions
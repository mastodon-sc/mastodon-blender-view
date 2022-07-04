#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR
killall blender
folder=~/Applications/blender-2.93.8-linux-x64/2.93/scripts/addons/blender-mastodon-view
rm -r $folder
mkdir $folder
cp *.py $folder
script -c ~/Applications/blender-2.93.8-linux-x64/blender

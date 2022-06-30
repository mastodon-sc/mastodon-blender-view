#!/bin/bash
killall blender
folder=~/Applications/blender-2.93.8-linux-x64/2.93/scripts/addons/blender-mastodon-view
rm -r $folder
mkdir $folder
cp *.py $folder
~/Applications/blender-2.93.8-linux-x64/blender

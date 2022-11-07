#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR
killall blender
blender_folder=~/Applications/blender-2.93.8-linux-x64
#blender_folder=~/Applications/blender-3.3.1-linux-x64
addons_folder=$(realpath $blender_folder/*/scripts/addons)
folder=$addons_folder/blender-mastodon-view
rm -r "$folder"
mkdir "$folder"
cp *.py "$folder"
script -c "$blender_folder/blender"

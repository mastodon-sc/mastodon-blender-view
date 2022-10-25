# Mastodon Blender 3D View

A view that allows to visualize Mastodon tracking data in 3D using Blender.

## Description

Mastodon Blender View currently consists of two components.
A plugin for Blender that is written in Python,
and a client that reads Mastodon project data and streams it to the Blender plugin.
Both components communicate via the gRPC library / protocol.

## Project status

This project is currently under development and at a prototype level.

## License

All the source code of this project is published under BSD 2 license.

## Installation & Execution

Pre-requirements:
* Install Blender (recommended version 2.93.8)
* Install Java 8 or newer
* Install Git & Maven

Installation:
* Git clone this project
* Copy the blender_addon directory to into the "scripts/addons/" folder of
  your Blender installation.
* Go inside your blenders python directory ("python/bin").
* Run `./python* -m ensurepip` to ensure that pip is installed.
* Install dependencies using pip:
  * $ ./pip3 install grpc grpc-tools google-api-python-client.
* Start Blender and activate the plugin.
* Restart Blender
* Open the file mastodon-plugin/src/main/java/org/mastodon/ViewServiceClient.java
  and change the projectPath to point to a mastodon project you would like
  to visualize.
* Open a terminal and go to the mastodon-plugin/ folder.
* Run:
  * $ mvn exec:java -Dexec.mainClass=org.mastodon.ViewServiceClient
* The last step should have transferred the Mastodon tracking data to Blender.
  It is usually necessary to remove the default cube in Blender, and move the
  time slider in order to see the embryo.

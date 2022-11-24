# Mastodon Blender 3D View

The Mastodon Blender 3D view is a plugin for the [Mastodon](https://github.com/mastodon-sc/mastodon/) cell tracking software. 
The plugin allows it visualize Mastodon cell tracking data in the [Blender](https://blender.org) 3d modelling software.
This visualization is far more that static, not only can the data be rotated and shown from all angles.
Blender & Mastodon are linked together. You may use Blender to select objects, just as if it's a part of Mastodon.

https://user-images.githubusercontent.com/24407711/203746188-3193d841-30f0-4006-9e4f-df36087f353e.mov

(This is a plugin for the Mastodon cell tracking software used in biological research.
Please don't confuse it for the social network called Mastodon.)

## Installation

1. Install [Fiji](https://imagej.net/downloads) and [Blender](https://blender.org/download) on your computer. Make sure to install Blender as a "portable" installation. 
2. Next step is to [active](https://imagej.net/update-sites/following) the "Tomancak" update site in Fiji.
3. In Mastodon's main menu, you will find an entry ```Plugins > Setup Blender Addon ...```, click it and follow the instruction. 

That's it, you are ready to use it.
   
## Usage

#### Quick Start

In the Mastodon main menu click ```Window > New Blender Window ...``` wait for 20 seconds, for the data to load in Blender. And play around! Finally make sure to find the "Mastodon 3D View" panel in Blender, it's useful. 

#### "Mastodon 3D View" panel in Blender

<img src="https://user-images.githubusercontent.com/24407711/203944663-f3b81845-ae51-4528-aa59-3fa5fb5aeef6.png" align="left" width="200px"/>

Click on the 3D view in the Blender window.
Press ```N```, This makes a few tabs appear on the right edge of the 3D view.
One of the tabs is called "Mastodon 3D View".
Click it and you will be able to:

* Select a Mastodon synchronization group
* Change sphere sizes
* Select a "tag set" (The colors will be visualized in the 3d view.)
* Update the "tag set" (If you made changes in tags in Mastodon.)

<br clear="left"/>

#### Synchronize Mastodon and Blender

In Mastodon TrackScheme window click one of the lock symbols 1, 2 or 3.
In Blender go to the "Mastodon 3D View" panel, and under "Synchronization Group" chose the same number 1, 2 or 3.

![image](https://user-images.githubusercontent.com/24407711/203946393-b0ac8a2e-5457-4051-b0fe-8644c6d5ad65.png)
![image](https://user-images.githubusercontent.com/24407711/203945908-b26ace3f-21b4-407e-a204-a14bb5ac04ca.png)

Now time points and active spot are synchronized between Blender and Mastodon.

## Project Details

#### Status

The project is currently under development and at a prototype stage.
That said, the plugin can already be easily installed and used.

Help is needed to test the plugin on MacOS.
I'm also open to suggestions how to improve the tool.

#### Limitations

* The plugin assumes that the tracking data doesn't change.
  Changing the tracking data while using the 3D view, might lead to some unexpected behaviour.
* Tag sets are not automatically updated. You need to click ```Update Tags``` in
  Blender "Mastodon 3D View" tab.
* Currently, an error message shows up, when you close the Blender window. You may savely ignore this.

#### License

The source code of this project is published under BSD 2 license.

#### Development

Maven is used as a build tool. It is therefor possible to open the project with Eclipse, IntelliJ or any other java IDE. The class `StartMastodonLauncher` may be used as an entry point to execute the code.

The project can be fully compiled with just one commnad:

```shell
mvn package
```

Once compiled, it can be launched with this command:

```
mvn -Dexec.mainClass=org.mastodon.blender.StartMastodonLauncher -Dexec.classpathScope=test clean test-compile exec:java
```

The Mastodon Blender 3D View is an open source project.
You are welcome to contact me, if you would like to contribute.

#### How does it work?

Mastodon Blender View consists of two components.
An addon for Blender that is written in Python and a plugin for Mastodon.
Both components communicate using google RPC library.

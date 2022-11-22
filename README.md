# Mastodon Blender 3D View

A view that allows to visualize [Mastodon cell tracking](https://github.com/mastodon-sc/mastodon/) data in 3D using [Blender](https://blender.org).

(This is a plugin for the Mastodon cell tracking software used in biological research.
Please don't confuse it for the social network called Mastodon.)

## Installation & Quick Start

1. Download and install **Blender Portable**: https://blender.org/download
   
2. Download and install Fiji: https://imagej.net/downloads
   
3. Enable the "Tomancak" update-site. [(see instructions)](https://imagej.net/update-sites/following)
   
4. Restart Fiji.
5. Open your Mastodon project. (In Fiji's main menu click: ```Plugins > Mastodon```)
6. In the Mastodon window's menu select ```Plugins > Setup Blender Addon ...```. Follow the steps explained in the setup dialog.
7. In the Mastodon menu click ```Window > New Blender Window``` to launch the 3D view. Wait a moment, until the tracking data is loaded in Blender.
8. Make sure to find the "Mastodon 3D View" panel in Blender. Such that you can sync Blender with Mastodon, show tags etc.

## Usage

#### Start Blender from Mastodon

In Mastodon's main menu simple select: ```Window > New Blender Window```

#### "Mastodon 3D View" panel in Blender

Click on the 3D view in the Blender window.
Press ```N```, This makes a few tabs appear on the right edge of the 3D view.
One of the tabs is called "Mastodon 3D View".
Click it and you will be able to:
* Select a Mastodon synchronization group.
* Change sphere sizes.
* Select a "tag set", the colors will be visualized in the 3d view.
* Update the "tag set", if you made changes in Mastodon.

#### Synchronize Mastodon and Blender

In Mastodon TrackScheme of BDV window click one of the lock symbols 1, 2 or 3.
In Blender go to the "Mastodon 3D View" panel, and under Synchronization group
chose the same number 1, 2 or 3.

Now time points and active spot are synchronized between Blender and Mastodon.

## Project Status

The project is currently under development and at a prototype stage.
That said, the plugin can already be easily installed and used.

Help is needed to test the plugin on MacOS.
I'm also open to suggestions how to improve the tool.

## Limitations

* The plugin assumes that the tracking data doesn't change.
  Changing the tracking data while using the 3D view, might lead to some unexpected behaviour.
* Tag sets are not automatically updated. You need to click ```Update Tags``` in
  Blender "Mastodon 3D View" tab.
* Currently, an error message shows up, when you close the Blender window. You may savely ignore this.

## License

The source code of this project is published under BSD 2 license.

## Development

The Mastodon Blender 3D View is an open source project.
You are welcome to contact me, if you would like to contribute.

#### How does it work?

Mastodon Blender View currently consists of two components.
An addon for Blender that is written in Python and a plugin for Mastodon.
Both components communicate using google RPC library.

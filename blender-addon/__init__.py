bl_info = {
    "name": "Mastodon Blender View",
    "author": "Matthias Arzt",
    "version": (0, 1, 0),
    "blender": (2, 93, 8),
    "location": "View3D > Mastodon",
    "warning": "",
    "wiki_url": "",
    "category": "3D View"
}

from . import my_blender_addon


def register():
    my_blender_addon.register()


def unregister():
    my_blender_addon.unregister()

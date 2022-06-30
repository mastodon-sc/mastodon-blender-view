bl_info = {
    "name": "Mastodon Blender Addon",
    "author": "Matthias Arzt",
    "version": (0, 1, 0),
    "blender": (2, 93, 8),
    "location": "View3D > NewTab",
    "warning": "",
    "wiki_url": "",
    "category": "Add Mesh"
}

from . import my_blender_addon


def register():
    my_blender_addon.register()


def unregister():
    my_blender_addon.unregister()

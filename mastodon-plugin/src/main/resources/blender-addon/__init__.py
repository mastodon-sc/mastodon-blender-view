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

from . import mb_panel
from . import mb_server


def register():
    mb_panel.register()
    mb_server.register()


def unregister():
    mb_panel.unregister()
    mb_server.unregister()

###
# #%L
# A Mastodon plugin data allows to show the embryo in Blender.
# %%
# Copyright (C) 2022 Matthias Arzt
# %%
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# #L%
###
import bpy
from . import mb_server

class BlenderMastodonViewProperties(bpy.types.PropertyGroup):

    def update_sphere_size(self, context):
        sphere_size = self.sphere_size
        if mb_server.mastodon_blender_server is not None:
            mb_server.mastodon_blender_server.many_spheres.set_sphere_size(
                sphere_size)

    sphere_size: bpy.props.FloatProperty(name="Sphere Size", soft_min=0.05,
                                         soft_max=1.0, default=0.1,
                                         update=update_sphere_size)

    def update_sync_group(self, context):
        index = int(self.sync_group)
        print(index)
        mb_server.mastodon_blender_server.view_service.set_sync_group(index)

    sync_group: bpy.props.EnumProperty(
        name="Mastodon Sync Group",
        description="This does the same as selecting a lock symbol in Mastodon",
        items=[
            ('-1', "*", ""),
            ('0', "1", ""),
            ('1', "2", ""),
            ('2', "3", ""),
        ],
        update=update_sync_group
    )

    def get_tag_set_items(self, context):
        empty = [("-1", "none", "")]
        if mb_server.mastodon_blender_server is None:
            return empty
        view_service = mb_server.mastodon_blender_server.view_service
        tag_set_list = view_service.tag_set_list
        return empty + [(str(i), tag_set, "") for i, tag_set in enumerate(tag_set_list)]

    def update_tag_set(self, context):
        if mb_server.mastodon_blender_server is None:
            return
        index = int(self.tag_set)
        mb_server.mastodon_blender_server.view_service.set_tag_set_index(index)

    tag_set: bpy.props.EnumProperty(
        name="Mastodon Tag Set",
        description="Tag set that is used to color the spheres",
        items=get_tag_set_items,
        update=update_tag_set
    )


class BlenderMastodonUpdateTags(bpy.types.Operator):
    """Add a simple box mesh"""
    bl_idname = "mastodon.update_tags"
    bl_label = "Update Tags"
    bl_options = {'REGISTER', 'UNDO'}

    def execute(self, context):

        if mb_server.mastodon_blender_server is not None:
            mb_server.mastodon_blender_server.view_service.update_colors()
        print(context)

        return {'FINISHED'}


class TestPanel(bpy.types.Panel):
    bl_label = "Mastodon 3D View"
    bl_idname = "_PT_ Mastodon 3D View"
    bl_space_type = 'VIEW_3D'
    bl_region_type = 'UI'
    bl_category = 'Mastodon 3D View'

    def draw(self, context):
        layout = self.layout
        layout.label(text="Synchronization Group", icon='LOCKED')
        property_group = context.scene.blender_mastodon_view_properties
        layout.prop(property_group, "sync_group", expand=True)
        layout.row().label(text="Visualization Settings", icon='SETTINGS')
        layout.prop(property_group, "sphere_size")
        layout.prop(property_group, "tag_set")
        layout.row().operator('mastodon.update_tags')


classes = [TestPanel, BlenderMastodonViewProperties, BlenderMastodonUpdateTags]


def register():
    for cls in classes:
        bpy.utils.register_class(cls)
    bpy.types.Scene.blender_mastodon_view_properties = \
        bpy.props.PointerProperty(type=BlenderMastodonViewProperties)


def unregister():
    for cls in classes:
        bpy.utils.unregister_class(cls)
    del bpy.types.Scene.blender_mastodon_view_properties


if __name__ == "__main__":
    register()

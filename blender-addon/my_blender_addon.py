import bpy
from . import greeter_server


def update_sphere_size(properties_group, context):
    sphere_size = properties_group.sphere_size
    global my_greeter_server
    if my_greeter_server is not None:
        my_greeter_server.many_spheres.set_sphere_size(sphere_size)


class BlenderMastodonViewProperties(bpy.types.PropertyGroup):
    sphere_size: bpy.props.FloatProperty(name="Sphere Size", soft_min=0.1,
                                         soft_max=1.0, default=0.1,
                                         update=update_sphere_size)


class TestPanel(bpy.types.Panel):
    bl_label = "MyTestPanel"
    bl_idname = "PT MyTestPanel"
    bl_space_type = 'VIEW_3D'
    bl_region_type = 'UI'
    bl_category = 'NewTab'

    def draw(self, context):
        layout = self.layout
        layout.prop(context.scene.blender_mastodon_view_properties,
                    "sphere_size")
        row = layout.row()
        row.label(text="gRPC Hello World Example 3", icon='CUBE',
                  )

my_greeter_server = None

def start_server():
    global my_greeter_server
    my_greeter_server = greeter_server.MastodonBlenderServer()


classes = [TestPanel, BlenderMastodonViewProperties]


def register():
    for cls in classes:
        bpy.utils.register_class(cls)
    bpy.types.Scene.blender_mastodon_view_properties = \
        bpy.props.PointerProperty(type=BlenderMastodonViewProperties)
    bpy.app.timers.register(start_server, first_interval=1)


def unregister():
    global my_greeter_server
    if my_greeter_server is not None:
        my_greeter_server.stop(None)
        my_greeter_server = None

    for cls in classes:
        bpy.utils.unregister_class(cls)
    del bpy.types.Scene.blender_mastodon_view_properties


if __name__ == "__main__":
    register()

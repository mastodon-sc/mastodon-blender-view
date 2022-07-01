import bpy
from . import greeter_server


class TestPanel(bpy.types.Panel):
    bl_label = "MyTestPanel"
    bl_idname = "PT MyTestPanel"
    bl_space_type = 'VIEW_3D'
    bl_region_type = 'UI'
    bl_category = 'NewTab'

    def draw(self, context):
        layout = self.layout

        row = layout.row()
        row.label(text="gRPC Hello World Example 3", icon='CUBE')
        row = layout.row()
        row.operator("mesh.primitive_cube_add")
        row.operator("mesh.primitive_uv_sphere_add")


my_greeter_server = None


def register():
    bpy.utils.register_class(TestPanel)
    bpy.app.timers.register(start_server, first_interval=1)


def start_server():
    global my_greeter_server
    my_greeter_server = greeter_server.serve()


def unregister():
    global my_greeter_server
    bpy.utils.unregister_class(TestPanel)
    if my_greeter_server is not None:
        my_greeter_server.stop(None)
        my_greeter_server = None


if __name__ == "__main__":
    register()

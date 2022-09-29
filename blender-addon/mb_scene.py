import bpy
import random
from . import mb_utils

class ManySpheres:
    collection = None
    parent_object = None
    reference_sphere = None

    def __init__(self):
        def init_collection():
            self.collection = bpy.data.collections.new("mastodon_blender_view")
            bpy.context.scene.collection.children.link(self.collection)

        def init_parent_object():
            bpy.ops.object.empty_add(type='PLAIN_AXES', align='WORLD',
                                     location=(0, 0, 0), scale=(1, 1, 1))
            self.parent_object = bpy.context.active_object
            bpy.ops.collection.objects_remove_all()
            self.collection.objects.link(self.parent_object)

        def init_reference_sphere():
            bpy.ops.mesh.primitive_ico_sphere_add(enter_editmode=False,
                                                  align='WORLD',
                                                  location=(0, 0, 0),
                                                  scale=(1, 1, 1))
            bpy.ops.object.shade_smooth()
            self.reference_sphere = bpy.context.active_object
            bpy.ops.collection.objects_remove_all()  # remove the sphere from its collection

        def init_sphere_material():
            material = bpy.data.materials.new(name="Object Color")
            material.use_nodes = True
            principled_node = material.node_tree.nodes.get('Principled BSDF')
            principled_node.inputs[0].default_value = (1, 0, 0, 1)
            object_info_node = material.node_tree.nodes.new(
                'ShaderNodeObjectInfo')
            material.node_tree.links.new(object_info_node.outputs[1],
                                         principled_node.inputs[0])
            self.reference_sphere.active_material = material

        init_collection()
        init_parent_object()
        init_reference_sphere()
        init_sphere_material()

    def add_moving_spot(self, request):
        sphere = self.reference_sphere.copy()
        sphere.name = request.id
        sphere.parent = self.parent_object
        sphere.scale = (0.1, 0.1, 0.1)
        sphere.color = (random.random(), random.random(), random.random(), 1)
        mb_utils.hide_object(sphere, time=0)
        mb_utils.show_object(sphere, time=request.timepoints[0])

        last_time = 0
        for i in range(len(request.timepoints)):
            x = request.coordinates[i * 3]
            y = request.coordinates[i * 3 + 1]
            z = request.coordinates[i * 3 + 2]
            time = request.timepoints[i]
            sphere.location = (x, y, z)
            sphere.keyframe_insert(data_path="location", frame=time)
            last_time = time

        mb_utils.hide_object(sphere, time=last_time + 1)
        self.collection.objects.link(sphere)
        return

    def set_sphere_size(self, size):
        for sphere in self.parent_object.children:
            sphere.scale = [size, size, size]

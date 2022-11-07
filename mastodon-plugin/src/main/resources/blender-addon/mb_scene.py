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
import bidict
import random
from . import mb_utils


def get_color_channel(color_as_int, channel):
    return float((color_as_int >> (8 * channel)) & 0xff) / 255


def to_blender_color(color_as_int):
    red = get_color_channel(color_as_int, 2)
    green = get_color_channel(color_as_int, 1)
    blue = get_color_channel(color_as_int, 0)
    return red, green, blue, 1


class ManySpheres:
    collection = None
    parent_object = None
    reference_sphere = None
    ids_to_spheres = bidict.bidict()

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
        sphere.name = request.label
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
        self.ids_to_spheres[request.id] = sphere
        return

    def set_sphere_size(self, size):
        for sphere in self.parent_object.children:
            sphere.scale = [size, size, size]

    def set_spot_colors(self, request):
        ids = request.ids
        colors = request.colors
        for i in range(len(ids)):
            id = ids[i]
            color = colors[i]
            self.ids_to_spheres[id].color = to_blender_color(color)

    def set_time_point(self, request):
        time_point = request.timepoint
        bpy.context.scene.frame_set(time_point)

    def get_active_spot_id(self):
        try:
            return self.ids_to_spheres.inverse[bpy.context.active_object]
        except KeyError:
            return None

    def set_active_spot_id(self, request):
        try:
            id = request.id
            sphere = self.ids_to_spheres[id]
            bpy.context.view_layer.objects.active = sphere
            for obj in bpy.context.selected_objects:
                obj.select_set(False)
            sphere.select_set(True)
        except KeyError:
            pass

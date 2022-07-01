# Copyright 2015 gRPC authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""The Python implementation of the GRPC helloworld.Greeter server."""

from concurrent import futures

import bpy
import grpc
from . import helloworld_pb2
from . import helloworld_pb2_grpc
from functools import partial
import queue


class ManySpheres:
    collection = None
    parent_object = None
    reference_sphere = None
    reference_object_connection = None

    def __init__(self):
        self.init_collection()
        self.init_parent_object()
        self.init_reference_objects_collection()
        self.init_reference_sphere()

    def init_collection(self):
        self.collection = bpy.data.collections.new("mastodon_blender_view")
        bpy.context.scene.collection.children.link(self.collection)

    def init_parent_object(self):
        bpy.ops.object.empty_add(type='PLAIN_AXES', align='WORLD',
                                 location=(0, 0, 0), scale=(1, 1, 1))
        self.parent_object = bpy.context.active_object
        bpy.ops.collection.objects_remove_all()  # remove the sphere from its collection
        self.collection.objects.link(self.parent_object)

    def init_reference_objects_collection(self):
        self.reference_objects_collection = bpy.data.collections.new(
            "mastodon_reference_objects")
        bpy.context.scene.collection.children.link(self.reference_objects_collection)

    def init_reference_sphere(self):
        bpy.ops.mesh.primitive_ico_sphere_add(enter_editmode=False,
                                              align='WORLD', location=(0, 0, 0),
                                              scale=(1, 1, 1))
        bpy.ops.object.shade_smooth()
        self.reference_sphere = bpy.context.active_object
        bpy.ops.collection.objects_remove_all()  # remove the sphere from its collection
        self.reference_objects_collection.objects.link(self.reference_sphere)

    def add_sphere(self, x, y, z):
        sphere = self.reference_sphere.copy()
        sphere.location = (x, y, z)
        sphere.parent = self.parent_object
        self.collection.objects.link(sphere)
        return


class Greeter(helloworld_pb2_grpc.GreeterServicer):

    def __init__(self):
        self.many_spheres = ManySpheres()

    def addSphere(self, request, context):
        run_in_main_thread(partial(self.many_spheres.add_sphere,
                                   request.x, request.y, request.z))
        return helloworld_pb2.Empty()


# Implement run in main thread

execution_queue = queue.Queue()


def run_in_main_thread(function):
    execution_queue.put(function)


def execute_queued_functions():
    while not execution_queue.empty():
        function = execution_queue.get()
        function()
    return 1.0


bpy.app.timers.register(execute_queued_functions)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    helloworld_pb2_grpc.add_GreeterServicer_to_server(Greeter(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    return server

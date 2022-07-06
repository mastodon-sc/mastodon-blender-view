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

    def __init__(self):
        def init_collection():
            self.collection = bpy.data.collections.new("mastodon_blender_view")
            bpy.context.scene.collection.children.link(self.collection)

        def init_parent_object():
            bpy.ops.object.empty_add(type='PLAIN_AXES', align='WORLD',
                                     location=(0, 0, 0), scale=(1, 1, 1))
            self.parent_object = bpy.context.active_object
            bpy.ops.collection.objects_remove_all()  # remove the sphere from its collection
            self.collection.objects.link(self.parent_object)

        def init_reference_sphere():
            bpy.ops.mesh.primitive_ico_sphere_add(enter_editmode=False,
                                                  align='WORLD',
                                                  location=(0, 0, 0),
                                                  scale=(1, 1, 1))
            bpy.ops.object.shade_smooth()
            self.reference_sphere = bpy.context.active_object
            bpy.ops.collection.objects_remove_all()  # remove the sphere from its collection

        init_collection()
        init_parent_object()
        init_reference_sphere()

    def add_moving_spot(self, request):
        sphere = self.reference_sphere.copy()
        sphere.name = request.id
        sphere.parent = self.parent_object
        self.insert_visibility_keyframe(sphere, 0, False)
        self.insert_visibility_keyframe(sphere, request.timepoints[0], True)

        last_time = 0
        for i in range(len(request.timepoints)):
            x = request.coordinates[i * 3]
            y = request.coordinates[i * 3 + 1]
            z = request.coordinates[i * 3 + 2]
            time = request.timepoints[i]
            sphere.location = ( x, y, z )
            sphere.keyframe_insert(data_path="location", frame=time)
            last_time = time

        self.insert_visibility_keyframe(sphere, last_time + 1, False)
        self.collection.objects.link(sphere)
        return

    @staticmethod
    def insert_visibility_keyframe(sphere, time, visible):
        sphere.hide_viewport = not visible
        sphere.hide_render = not visible
        sphere.keyframe_insert(data_path="hide_viewport", frame=time)
        sphere.keyframe_insert(data_path="hide_render", frame=time)

class Greeter(helloworld_pb2_grpc.GreeterServicer):

    def __init__(self):
        self.many_spheres = ManySpheres()

    def addMovingSpot(self, request, context):
        run_in_main_thread(partial(self.many_spheres.add_moving_spot, request))
        return helloworld_pb2.Empty()

    @staticmethod
    def parse_coordinates(coordinates):
        return coordinates.x, coordinates.y, coordinates.z


# Implement run in main thread

class MainThreadQueue:
    execution_queue = queue.Queue()
    waiting = False

    def enqueue(self, function):
        self.execution_queue.put(function)
        if not self.waiting:
            self.waiting = True
            bpy.app.timers.register(self.execute_queued_functions)

    def execute_queued_functions(self):
        self.waiting = False
        while not self.execution_queue.empty():
            function = self.execution_queue.get()
            function()
        return None


main_thread_queue = MainThreadQueue()


def run_in_main_thread(function):
    global main_thread_queue
    main_thread_queue.enqueue(function)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    helloworld_pb2_grpc.add_GreeterServicer_to_server(Greeter(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    return server

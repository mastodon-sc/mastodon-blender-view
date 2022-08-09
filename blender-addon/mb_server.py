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
"""The Python implementation of the GRPC helloworld.ViewService server."""

from concurrent import futures

import grpc
import bpy
from . import mb_scene
from . import mastodon_blender_view_pb2 as pb
from . import mastodon_blender_view_pb2_grpc as rpc
from . import mb_utils
from functools import partial


class ViewService(rpc.ViewServiceServicer):
    many_spheres = None

    def __init__(self, many_spheres):
        self.many_spheres = many_spheres

    def addMovingSpot(self, request, context):
        mb_utils.run_in_main_thread(
            partial(self.many_spheres.add_moving_spot, request))
        return pb.Empty()


class MastodonBlenderServer:
    many_spheres = None

    def __init__(self):
        self.many_spheres = mb_scene.ManySpheres()
        self.server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
        greeter = ViewService(self.many_spheres)
        rpc.add_ViewServiceServicer_to_server(greeter, self.server)
        self.server.add_insecure_port('[::]:50051')
        self.server.start()

    def stop(self):
        self.server.stop()


mastodon_blender_server = None


def register():
    bpy.app.timers.register(delayed_start_server, first_interval=1)


def delayed_start_server():
    global mastodon_blender_server
    mastodon_blender_server = MastodonBlenderServer()


def unregister():
    global mastodon_blender_server
    if mastodon_blender_server is not None:
        mastodon_blender_server.stop(None)
        mastodon_blender_server = None

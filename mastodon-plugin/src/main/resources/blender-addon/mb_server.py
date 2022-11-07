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
import queue
from . import mb_scene
from . import mastodon_blender_view_pb2 as pb
from . import mastodon_blender_view_pb2_grpc as rpc
from . import mb_utils
from functools import partial


class ViewService(rpc.ViewServiceServicer):
    many_spheres : mb_scene.ManySpheres = None
    active_spot_id = None
    time_point = None
    changes_queue = queue.Queue()
    tag_set_list = []
    tag_set_index = -1

    def __init__(self, many_spheres):
        self.many_spheres = many_spheres
        callback = self.active_object_changed_callback
        subscribe_to_active_object_change_event(self, callback)
        bpy.app.handlers.frame_change_post.append(self.frame_change_callback)

    def addMovingSpot(self, request, context):
        mb_utils.run_in_main_thread(
            partial(self.many_spheres.add_moving_spot, request))
        return pb.Empty()

    def setSpotColors(self, request, context):
        mb_utils.run_in_main_thread(
            partial(self.many_spheres.set_spot_colors, request))
        return pb.Empty()

    def setTimePoint(self, request, context):
        self.time_point = request.timepoint
        mb_utils.run_in_main_thread(
            partial(self.many_spheres.set_time_point, request))
        return pb.Empty()

    def subscribeToChange(self, request, context):
        while context.is_active():
            try:
                change_id = self.changes_queue.get(timeout=1)
                if change_id == None:
                    change_id = 0xffffffff
                yield pb.ChangeMessage(id=change_id)
            except queue.Empty:
                pass

    def active_object_changed_callback(self):
        active_spot_id = self.many_spheres.get_active_spot_id()
        if self.active_spot_id != active_spot_id:
            self.active_spot_id = active_spot_id
            self.changes_queue.put(pb.ACTIVE_SPOT)

    def frame_change_callback(self, scene, _):
        time_point = scene.frame_current
        if self.time_point != time_point:
            self.time_point = time_point
            self.changes_queue.put(pb.TIME_POINT)

    def setActiveSpot(self, request, context):
        mb_utils.run_in_main_thread(
            partial(self.many_spheres.set_active_spot_id, request))
        return pb.Empty()

    def getActiveSpot(self, request, context):
        return pb.ActiveSpotResponse(id=self.active_spot_id);

    def getTimePoint(self, request, context):
        timepoint = self.time_point
        return pb.TimePointResponse(timePoint=timepoint)

    def update_colors(self):
        self.changes_queue.put(pb.UPDATE_COLORS_REQUEST)

    def setTagSetList(self, request, context):
        self.tag_set_list = request.tag_set_names
        return pb.Empty()

    def getSelectedTagSet(self, request, context):
        return pb.SelectedTagSetResponse(index=self.tag_set_index)

    def set_tag_set_index(self, tag_set_index):
        self.tag_set_index = tag_set_index
        self.changes_queue.put(pb.SELECTED_TAG_SET)


def subscribe_to_active_object_change_event(owner, callback):
    bpy.msgbus.subscribe_rna(
        key=(bpy.types.LayerObjects, 'active'),
        owner=owner,
        args=(callback,),
        notify=lambda x: x()
    )


class MastodonBlenderServer:
    many_spheres = None
    view_service = None

    def __init__(self):
        self.many_spheres = mb_scene.ManySpheres()
        self.server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
        self.view_service = ViewService(self.many_spheres)
        rpc.add_ViewServiceServicer_to_server(self.view_service, self.server)
        self.server.add_insecure_port('[::]:50846')
        self.server.start()

    def stop(self):
        self.server.stop(grace=2)


mastodon_blender_server = None


def register():
    bpy.app.timers.register(delayed_start_server, first_interval=1)


def delayed_start_server():
    global mastodon_blender_server
    mastodon_blender_server = MastodonBlenderServer()


def unregister():
    global mastodon_blender_server
    if mastodon_blender_server is not None:
        mastodon_blender_server.stop()
        mastodon_blender_server = None

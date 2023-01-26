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
import queue


# Implement run in main thread

class MainThreadQueue:

    def __init__(self):
        self.execution_queue = queue.Queue()
        self.waiting = False

    def enqueue(self, function):
        self.execution_queue.put(function)
        if not self.waiting:
            self.waiting = True
            bpy.app.timers.register(self.execute_queued_functions)

    def execute_queued_functions(self):
        while not self.execution_queue.empty():
            function = self.execution_queue.get()
            function()
        self.waiting = False
        return None


main_thread_queue = MainThreadQueue()


def run_in_main_thread(function):
    main_thread_queue.enqueue(function)


def show_object(obj, time):
    insert_visibility_keyframe(obj, time=time, visible=True)


def hide_object(obj, time):
    insert_visibility_keyframe(obj, time=time, visible=False)


def insert_visibility_keyframe(obj, time, visible):
    obj.hide_viewport = not visible
    obj.hide_render = not visible
    obj.keyframe_insert(data_path="hide_viewport", frame=time)
    obj.keyframe_insert(data_path="hide_render", frame=time)


def get_color_channel(color_as_int, channel):
    return float((color_as_int >> (8 * channel)) & 0xff) / 255


def to_blender_color(color_as_int):
    red = get_color_channel(color_as_int, 2)
    green = get_color_channel(color_as_int, 1)
    blue = get_color_channel(color_as_int, 0)
    return red, green, blue, 1

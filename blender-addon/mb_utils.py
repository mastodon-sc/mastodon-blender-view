import bpy
import queue

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

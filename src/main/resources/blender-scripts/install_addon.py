###
# #%L
# A Mastodon plugin data allows to show the embryo in Blender.
# %%
# Copyright (C) 2022 - 2024 Matthias Arzt
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
import os
import ensurepip
import subprocess
import bpy
import sys
import addon_utils

# install pip

def get_python_path():
    try:
        # 2.92 and older
        path = bpy.app.binary_path_python
    except AttributeError:
        # 2.93 and later
        path = sys.executable
    return os.path.abspath(path)


os.environ.pop("PIP_REQ_TRACKER", None)
ensurepip.bootstrap()
os.environ.pop("PIP_REQ_TRACKER", None)

# install dependencies

python_path = get_python_path()
packages = {'grpcio', 'bidict', 'grpcio-tools'}
subprocess.check_output([python_path, '-m', 'pip', 'install', *packages])

# test if dependencies are installed

try:
    import bidict
    import grpc
    import google.protobuf
except ModuleNotFoundError:
    raise Exception("dependency installation failed")

print("dependencies installed")

# install addon

argv = sys.argv
addon_zip = argv[argv.index('--') + 1]
bpy.ops.preferences.addon_install(filepath=addon_zip)

print("mastodon blender view addon installed")

filename_init_py = [m.__file__ for m in addon_utils.modules() if m.__name__ == "mastodon_blender_view"][0]
addon_dir = os.path.dirname(filename_init_py)
subprocess.check_output([python_path, '-m', 'grpc_tools.protoc', '-I.', '--python_out=.', '--grpc_python_out=.', 'mastodon_blender_view/mastodon-blender-view.proto'], cwd=os.path.dirname(addon_dir))

try:
    from mastodon_blender_view import mastodon_blender_view_pb2
    from mastodon_blender_view import mastodon_blender_view_pb2_grpc
except ModuleNotFoundError:
    raise Exception("installing google RPC generated code failed")

print("google RPC code compiled")

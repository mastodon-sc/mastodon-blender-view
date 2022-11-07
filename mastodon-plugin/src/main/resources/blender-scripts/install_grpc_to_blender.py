import os
import ensurepip
import subprocess
import bpy
import sys


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

python_path = get_python_path()
packages = {'grpcio', 'bidict', 'grpcio-tools'}
subprocess.check_output([python_path, '-m', 'pip', 'install', *packages])

try:
    import bidict
    import grpc
    import google.protobuf
    print("dependencies installed")
except ModuleNotFoundError:
    print("installation failed")

# also some other google library see some other pr on this project

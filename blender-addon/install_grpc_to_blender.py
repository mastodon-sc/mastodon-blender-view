import os
import ensurepip
import subprocess
import bpy
import sys


def python_path():
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

subprocess.check_output([python_path(), '-m', 'pip', 'install', 'grpcio', 'bidict', 'grpcio-tools'])
# also some other google library see some other pr on this project

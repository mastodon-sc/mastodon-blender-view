#!/bin/sh
# python 3.9
# conda install grpcio
# conda install grpcio-tools
python -m grpc_tools.protoc -I. --python_out=../blender-addon --grpc_python_out=../blender-addon helloworld.proto
sed -e "s/^import helloworld_pb2 as helloworld__pb2$/from . import helloworld_pb2 as helloworld__pb2/" -i ../blender-addon/helloworld_pb2_grpc.py

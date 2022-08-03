#!/bin/sh
# python 3.9
# conda install grpcio
# conda install grpcio-tools
python -m grpc_tools.protoc -I. --python_out=../blender-addon --grpc_python_out=../blender-addon mastodon-blender-view.proto
sed -e "s/^import mastodon_blender_view_pb2 as mastodon__blender__view__pb2$/from . import mastodon_blender_view_pb2 as mastodon__blender__view__pb2/" -i ../blender-addon/mastodon_blender_view_pb2_grpc.py

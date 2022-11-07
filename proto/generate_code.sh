#!/bin/sh
# python 3.9
# conda install grpcio
# conda install grpcio-tools
folder=mastodon-plugin/src/main/resources/blender-addon
python -m grpc_tools.protoc -I. --python_out=../${folder} --grpc_python_out=../${folder} mastodon-blender-view.proto
sed -e "s/^import mastodon_blender_view_pb2 as mastodon__blender__view__pb2$/from . import mastodon_blender_view_pb2 as mastodon__blender__view__pb2/" -i ../${folder}/mastodon_blender_view_pb2_grpc.py

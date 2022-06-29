#!/bin/sh
# python 3.9
# conda install grpcio
# conda install grpcio-tools
python -m grpc_tools.protoc -I. --python_out=../helloworld --grpc_python_out=../helloworld helloworld.proto

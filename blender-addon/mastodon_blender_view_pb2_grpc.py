# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc

from . import mastodon_blender_view_pb2 as mastodon__blender__view__pb2


class ViewServiceStub(object):
    """Missing associated documentation comment in .proto file."""

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.addMovingSpot = channel.unary_unary(
                '/mastodon_blender_view.ViewService/addMovingSpot',
                request_serializer=mastodon__blender__view__pb2.AddMovingSpotRequest.SerializeToString,
                response_deserializer=mastodon__blender__view__pb2.Empty.FromString,
                )
        self.setSpotColors = channel.unary_unary(
                '/mastodon_blender_view.ViewService/setSpotColors',
                request_serializer=mastodon__blender__view__pb2.SetSpotColorsRequest.SerializeToString,
                response_deserializer=mastodon__blender__view__pb2.Empty.FromString,
                )
        self.setTimePoint = channel.unary_unary(
                '/mastodon_blender_view.ViewService/setTimePoint',
                request_serializer=mastodon__blender__view__pb2.SetTimePointRequest.SerializeToString,
                response_deserializer=mastodon__blender__view__pb2.Empty.FromString,
                )
        self.getTimePoint = channel.unary_unary(
                '/mastodon_blender_view.ViewService/getTimePoint',
                request_serializer=mastodon__blender__view__pb2.Empty.SerializeToString,
                response_deserializer=mastodon__blender__view__pb2.TimePointResponse.FromString,
                )
        self.setActiveSpot = channel.unary_unary(
                '/mastodon_blender_view.ViewService/setActiveSpot',
                request_serializer=mastodon__blender__view__pb2.SetActiveSpotRequest.SerializeToString,
                response_deserializer=mastodon__blender__view__pb2.Empty.FromString,
                )
        self.getActiveSpot = channel.unary_unary(
                '/mastodon_blender_view.ViewService/getActiveSpot',
                request_serializer=mastodon__blender__view__pb2.Empty.SerializeToString,
                response_deserializer=mastodon__blender__view__pb2.ActiveSpotResponse.FromString,
                )
        self.subscribeToChange = channel.unary_stream(
                '/mastodon_blender_view.ViewService/subscribeToChange',
                request_serializer=mastodon__blender__view__pb2.Empty.SerializeToString,
                response_deserializer=mastodon__blender__view__pb2.ChangeMessage.FromString,
                )
        self.setTagSetList = channel.unary_unary(
                '/mastodon_blender_view.ViewService/setTagSetList',
                request_serializer=mastodon__blender__view__pb2.SetTagSetListRequest.SerializeToString,
                response_deserializer=mastodon__blender__view__pb2.Empty.FromString,
                )
        self.getSelectedTagSet = channel.unary_unary(
                '/mastodon_blender_view.ViewService/getSelectedTagSet',
                request_serializer=mastodon__blender__view__pb2.Empty.SerializeToString,
                response_deserializer=mastodon__blender__view__pb2.SelectedTagSetResponse.FromString,
                )


class ViewServiceServicer(object):
    """Missing associated documentation comment in .proto file."""

    def addMovingSpot(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def setSpotColors(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def setTimePoint(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def getTimePoint(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def setActiveSpot(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def getActiveSpot(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def subscribeToChange(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def setTagSetList(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def getSelectedTagSet(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_ViewServiceServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'addMovingSpot': grpc.unary_unary_rpc_method_handler(
                    servicer.addMovingSpot,
                    request_deserializer=mastodon__blender__view__pb2.AddMovingSpotRequest.FromString,
                    response_serializer=mastodon__blender__view__pb2.Empty.SerializeToString,
            ),
            'setSpotColors': grpc.unary_unary_rpc_method_handler(
                    servicer.setSpotColors,
                    request_deserializer=mastodon__blender__view__pb2.SetSpotColorsRequest.FromString,
                    response_serializer=mastodon__blender__view__pb2.Empty.SerializeToString,
            ),
            'setTimePoint': grpc.unary_unary_rpc_method_handler(
                    servicer.setTimePoint,
                    request_deserializer=mastodon__blender__view__pb2.SetTimePointRequest.FromString,
                    response_serializer=mastodon__blender__view__pb2.Empty.SerializeToString,
            ),
            'getTimePoint': grpc.unary_unary_rpc_method_handler(
                    servicer.getTimePoint,
                    request_deserializer=mastodon__blender__view__pb2.Empty.FromString,
                    response_serializer=mastodon__blender__view__pb2.TimePointResponse.SerializeToString,
            ),
            'setActiveSpot': grpc.unary_unary_rpc_method_handler(
                    servicer.setActiveSpot,
                    request_deserializer=mastodon__blender__view__pb2.SetActiveSpotRequest.FromString,
                    response_serializer=mastodon__blender__view__pb2.Empty.SerializeToString,
            ),
            'getActiveSpot': grpc.unary_unary_rpc_method_handler(
                    servicer.getActiveSpot,
                    request_deserializer=mastodon__blender__view__pb2.Empty.FromString,
                    response_serializer=mastodon__blender__view__pb2.ActiveSpotResponse.SerializeToString,
            ),
            'subscribeToChange': grpc.unary_stream_rpc_method_handler(
                    servicer.subscribeToChange,
                    request_deserializer=mastodon__blender__view__pb2.Empty.FromString,
                    response_serializer=mastodon__blender__view__pb2.ChangeMessage.SerializeToString,
            ),
            'setTagSetList': grpc.unary_unary_rpc_method_handler(
                    servicer.setTagSetList,
                    request_deserializer=mastodon__blender__view__pb2.SetTagSetListRequest.FromString,
                    response_serializer=mastodon__blender__view__pb2.Empty.SerializeToString,
            ),
            'getSelectedTagSet': grpc.unary_unary_rpc_method_handler(
                    servicer.getSelectedTagSet,
                    request_deserializer=mastodon__blender__view__pb2.Empty.FromString,
                    response_serializer=mastodon__blender__view__pb2.SelectedTagSetResponse.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'mastodon_blender_view.ViewService', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


 # This class is part of an EXPERIMENTAL API.
class ViewService(object):
    """Missing associated documentation comment in .proto file."""

    @staticmethod
    def addMovingSpot(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/mastodon_blender_view.ViewService/addMovingSpot',
            mastodon__blender__view__pb2.AddMovingSpotRequest.SerializeToString,
            mastodon__blender__view__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def setSpotColors(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/mastodon_blender_view.ViewService/setSpotColors',
            mastodon__blender__view__pb2.SetSpotColorsRequest.SerializeToString,
            mastodon__blender__view__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def setTimePoint(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/mastodon_blender_view.ViewService/setTimePoint',
            mastodon__blender__view__pb2.SetTimePointRequest.SerializeToString,
            mastodon__blender__view__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def getTimePoint(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/mastodon_blender_view.ViewService/getTimePoint',
            mastodon__blender__view__pb2.Empty.SerializeToString,
            mastodon__blender__view__pb2.TimePointResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def setActiveSpot(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/mastodon_blender_view.ViewService/setActiveSpot',
            mastodon__blender__view__pb2.SetActiveSpotRequest.SerializeToString,
            mastodon__blender__view__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def getActiveSpot(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/mastodon_blender_view.ViewService/getActiveSpot',
            mastodon__blender__view__pb2.Empty.SerializeToString,
            mastodon__blender__view__pb2.ActiveSpotResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def subscribeToChange(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_stream(request, target, '/mastodon_blender_view.ViewService/subscribeToChange',
            mastodon__blender__view__pb2.Empty.SerializeToString,
            mastodon__blender__view__pb2.ChangeMessage.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def setTagSetList(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/mastodon_blender_view.ViewService/setTagSetList',
            mastodon__blender__view__pb2.SetTagSetListRequest.SerializeToString,
            mastodon__blender__view__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def getSelectedTagSet(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/mastodon_blender_view.ViewService/getSelectedTagSet',
            mastodon__blender__view__pb2.Empty.SerializeToString,
            mastodon__blender__view__pb2.SelectedTagSetResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

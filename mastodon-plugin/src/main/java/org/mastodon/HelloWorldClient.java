/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mastodon;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;

public class HelloWorldClient
{

	private final GreeterGrpc.GreeterBlockingStub blockingStub;

	public HelloWorldClient( Channel channel )
	{
		blockingStub = GreeterGrpc.newBlockingStub( channel );
	}

	public void addSphere( String id, List<Coordinates> coordinates )
	{
		blockingStub.addMovingSpot( AddMovingSportRequest.newBuilder()
				.setId( id )
				.addAllCoordinates( coordinates )
				.build() );
	}

	private static Coordinates coordinates( float x, float y, float z, int time )
	{
		return Coordinates.newBuilder()
				.setX( x )
				.setY( y )
				.setZ( z )
				.setTime( time )
				.build();
	}

	public static void main( String... args )
	{
		ManagedChannel channel = ManagedChannelBuilder.forTarget( "localhost:50051" ).usePlaintext().build();
		try
		{
			HelloWorldClient client = new HelloWorldClient( channel );
			List<Coordinates> coordinates = new ArrayList<>( 100 );
			for ( int i = 0; i <= 100; i++ )
				coordinates.add( coordinates( i / 10.f, 0, 0, i ) );
			client.addSphere( "sphere_label", coordinates );
		}
		finally
		{
			channel.shutdownNow(); //.awaitTermination( 5, TimeUnit.SECONDS );
		}
	}
}

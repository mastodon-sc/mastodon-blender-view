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

import com.google.common.base.Stopwatch;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;

public class HelloWorldClient {

	private final GreeterGrpc.GreeterBlockingStub blockingStub;

	public HelloWorldClient(Channel channel) {
		blockingStub = GreeterGrpc.newBlockingStub(channel);
	}

	public void addSphere(String id, long time, Coordinates coordinates) {
		blockingStub.addSpot( AddSpotRequest.newBuilder()
				.setId( id )
				.setTime( time )
				.setCoordinates( coordinates )
		.build());
	}

	private void moveSphere( String id, int time, Coordinates coordinates )
	{
		blockingStub.moveSpot( MoveSpotRequest.newBuilder()
				.setId( id )
				.setTime( time )
				.setCoordinates( coordinates )
				.build());
	}

	private void hideSphere( String id, int time )
	{
		blockingStub.hideSpot( HideSpotRequest.newBuilder()
				.setId( id )
				.setTime( time )
				.build());
	}

	private static Coordinates coordinates( float x, float y, float z )
	{
		Coordinates coordinates = Coordinates.newBuilder().setX( x ).setY( y ).setZ( z ).build();
		return coordinates;
	}

	public static void main(String[] args) throws Exception {
		ManagedChannel channel = ManagedChannelBuilder.forTarget( "localhost:50051" ).usePlaintext().build();
		try {
			HelloWorldClient client = new HelloWorldClient(channel);
			Stopwatch stopwatch = Stopwatch.createStarted();
			client.addSphere("id", 0, coordinates(0, 0, 0));
			for ( int i = 0; i < 100; i++ )
				client.moveSphere( "id", i, coordinates(i / 10.f, 0, 0) );
			client.hideSphere("id", 100);
			System.out.println(stopwatch.toString());
		}
		finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}
	}

}

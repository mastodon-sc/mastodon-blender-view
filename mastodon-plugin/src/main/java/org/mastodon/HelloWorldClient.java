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

	public void addSphere(float x, float y, float z) {
		Coordinates coordinates = Coordinates.newBuilder().setX( x ).setY( y ).setZ( z ).build();
		blockingStub.addSphere( coordinates );
	}

	public static void main(String[] args) throws Exception {
		ManagedChannel channel = ManagedChannelBuilder.forTarget( "localhost:50051" ).usePlaintext().build();
		try {
			HelloWorldClient client = new HelloWorldClient(channel);
			Stopwatch stopwatch = Stopwatch.createStarted();
			int n = 20;

			for ( int x = 0; x < n; x++ )
				for ( int y = 0; y < n; y++ )
					for ( int z = 0; z < n; z++ )
						client.addSphere( x, y, z );

			System.out.println(stopwatch.toString());
		}
		finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}
	}
}

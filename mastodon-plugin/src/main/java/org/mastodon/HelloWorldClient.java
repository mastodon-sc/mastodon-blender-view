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
import net.imglib2.util.StopWatch;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import java.io.IOException;
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
		System.out.println( "label: " + id + ", " + coordinates.size() + " coordinates" );
		blockingStub.addMovingSpot( AddMovingSpotRequest.newBuilder().setId( id ).addAllCoordinates( coordinates ).build() );
	}

	private static Coordinates coordinates( float x, float y, float z, int time )
	{
		return Coordinates.newBuilder().setX( x ).setY( y ).setZ( z ).setTime( time ).build();
	}

	public static void main( String... args ) throws Exception
	{
		// TODO: if we can transfer more efficiently
		// TODO: show multiple embryos
		// TODO: visualize tag colors
		// TODO: synchronize selection between blender and Mastodon
		transferEmbryo();
	}

	private static void transferEmbryo() throws Exception
	{
		ManagedChannel channel = ManagedChannelBuilder.forTarget( "localhost:50051" ).usePlaintext().build();
		try (Context context = new Context())
		{
			Model embryoA = openAppModel( context, "/home/arzt/Datasets/Mette/E1.mastodon" );
			ModelGraph graph = embryoA.getGraph();
			HelloWorldClient client = new HelloWorldClient( channel );
			StopWatch watch = StopWatch.createAndStart();
			for ( Spot spot : graph.vertices() )
			{
				if ( spot.incomingEdges().size() != 1 )
					transferTracklet( graph, spot, client );
				if ( spot.outgoingEdges().size() > 1 )
					transferChildTracklets( graph, spot, client );
			}
			System.out.println(watch);
		}
		finally
		{
			channel.shutdownNow(); //.awaitTermination( 5, TimeUnit.SECONDS );
		}
	}

	private static void transferTracklet( ModelGraph graph, Spot start, HelloWorldClient client )
	{
		Spot spot = graph.vertexRef();
		try
		{
			List<Coordinates> tracklet = new ArrayList<>();
			spot.refTo( start );
			tracklet.add( coordinates( spot ) );
			while ( spot.outgoingEdges().size() == 1 )
			{
				spot.outgoingEdges().iterator().next().getTarget( spot );
				if ( spot.incomingEdges().size() != 1 )
					break;
				tracklet.add( coordinates( spot ) );
			}
			client.addSphere( start.getLabel(), tracklet );
		}
		finally
		{
			graph.releaseRef( spot );
		}
	}

	private static Coordinates coordinates( Spot spot )
	{
		float s = 0.05f;
		return Coordinates.newBuilder()
				.setX( s * spot.getFloatPosition( 0 ) )
				.setY( s * spot.getFloatPosition( 1 ) )
				.setZ( s * spot.getFloatPosition( 2 ) )
				.setTime( spot.getTimepoint() ).build();
	}

	private static void transferChildTracklets( ModelGraph graph, Spot spot, HelloWorldClient client )
	{
		Spot ref = graph.vertexRef();
		for ( Link link : spot.outgoingEdges() )
		{
			Spot child = link.getTarget( ref );
			transferTracklet( graph, child, client );
		}
		graph.releaseRef( ref );
	}

	private static void runClient()
	{
		ManagedChannel channel = ManagedChannelBuilder.forTarget( "localhost:50051" ).usePlaintext().build();
		try
		{
			HelloWorldClient client = new HelloWorldClient( channel );
			List<Coordinates> coordinates = new ArrayList<>( 100 );
			for ( int i = 0; i <= 100; i++ )
				coordinates.add( coordinates( i / 10.f, 1, 1, i ) );
			client.addSphere( "sphere_label", coordinates );
		}
		finally
		{
			channel.shutdownNow(); //.awaitTermination( 5, TimeUnit.SECONDS );
		}
	}

	private static Model openAppModel( Context context, String projectPath )
	{
		try
		{
			MamutProject project = new MamutProjectIO().load( projectPath );
			final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
			final boolean isNewProject = project.getProjectRoot() == null;
			if ( !isNewProject )
			{
				try (final MamutProject.ProjectReader reader = project.openForReading())
				{
					final RawGraphIO.FileIdToGraphMap<Spot, Link> idmap = model.loadRaw( reader );
					// Load features.
					MamutRawFeatureModelIO.deserialize( context, model, idmap, reader );
				}
				catch ( final ClassNotFoundException e )
				{
					e.printStackTrace();
				}
			}
			return model;
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}
}

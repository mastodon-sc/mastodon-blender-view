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
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
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
import java.util.Collection;

public class HelloWorldClient
{

	private final GreeterGrpc.GreeterBlockingStub blockingStub;

	private static final AffineTransform3D transform = new AffineTransform3D();

	static
	{
		transform.scale( 0.05 );
	}

	public HelloWorldClient( Channel channel )
	{
		blockingStub = GreeterGrpc.newBlockingStub( channel );
	}

	public static void main( String... args ) throws Exception
	{
		// TODO: if we can transfer more efficiently
		// TODO: show multiple embryos
		// TODO: visualize tag colors
		// TODO: synchronize selection between blender and Mastodon
		transferEmbryo();
	}

	private static AffineTransform3D getNormalizingTransform( Collection<Spot> spots )
	{
		RealLocalizable mean = getMean( spots );
		double variance = getVariance( spots, mean );

		double s = 1 / Math.sqrt( variance );
		AffineTransform3D transform = new AffineTransform3D();
		transform.translate( - mean.getDoublePosition( 0 ),
				- mean.getDoublePosition( 1 ),
				- mean.getDoublePosition( 2 ) );
		transform.scale( s );
		return transform;
	}

	private static RealLocalizable getMean( Collection<Spot> spots )
	{
		double x = 0;
		double y = 0;
		double z = 0;
		for ( Spot spot : spots )
		{
			x += spot.getDoublePosition( 0 );
			y += spot.getDoublePosition( 1 );
			z += spot.getDoublePosition( 2 );
		}
		int n = spots.size();
		RealLocalizable mean = RealPoint.wrap( new double[] { x / n, y / n, z / n } );
		return mean;
	}

	private static double getVariance( Collection<Spot> spots, RealLocalizable mean )
	{
		double variance = 0;
		for ( Spot spot : spots )
		{
			variance += sqr( spot.getDoublePosition( 0 ) - mean.getDoublePosition( 0 ) );
			variance += sqr( spot.getDoublePosition( 1 ) - mean.getDoublePosition( 1 ) );
			variance += sqr( spot.getDoublePosition( 2 ) - mean.getDoublePosition( 2 ) );
		}
		variance /= ( spots.size() - 1 );
		return variance;
	}

	private static double sqr( double v )
	{
		return v * v;
	}

	private static void transferEmbryo() throws Exception
	{
		ManagedChannel channel = ManagedChannelBuilder.forTarget( "localhost:50051" ).usePlaintext().build();
		try (Context context = new Context())
		{
			Model embryoA = openAppModel( context, "/home/arzt/Datasets/Mette/E1.mastodon" );
			transform.set(getNormalizingTransform( embryoA.getGraph().vertices() ));
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
			System.out.println( watch );
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
			AddMovingSpotRequest.Builder request = AddMovingSpotRequest.newBuilder();
			request.setId( start.getLabel() );
			spot.refTo( start );
			coordinates( request, spot );
			while ( spot.outgoingEdges().size() == 1 )
			{
				spot.outgoingEdges().iterator().next().getTarget( spot );
				if ( spot.incomingEdges().size() != 1 )
					break;
				coordinates( request, spot );
			}
			client.blockingStub.addMovingSpot( request.build() );
		}
		finally
		{
			graph.releaseRef( spot );
		}
	}

	private static void coordinates( AddMovingSpotRequest.Builder request, Spot spot )
	{
		float s = 0.05f;
		RealPoint point = new RealPoint( 3 );
		transform.apply( spot, point );
		request.addCoordinates( point.getFloatPosition( 0 ) );
		request.addCoordinates( point.getFloatPosition( 1 ) );
		request.addCoordinates( point.getFloatPosition( 2 ) );
		request.addTimepoints( spot.getTimepoint() );
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
			AddMovingSpotRequest.Builder request = AddMovingSpotRequest.newBuilder();
			request.setId( "lineage" );
			for ( int i = 0; i <= 100; i++ )
			{
				request.addCoordinates( i / 10.f );
				request.addCoordinates( 1 );
				request.addCoordinates( 1 );
				request.addTimepoints( i );
			}
			client.blockingStub.addMovingSpot( request.build() );
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

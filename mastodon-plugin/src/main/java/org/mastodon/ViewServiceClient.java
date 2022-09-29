/*-
 * #%L
 * A Mastodon plugin data allows to show the embryo in Blender.
 * %%
 * Copyright (C) 2022 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.StopWatch;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefSetImp;
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

public class ViewServiceClient
{

	private static final String projectPath = "/home/arzt/Datasets/Mette/E1.mastodon";

	//private static final String projectPath = "/home/arzt/Datasets/DeepLineage/Johannes/2022-01-27_Ml_NL45xNL26_fused_part5_2022-07-06_Matthias.mastodon";

	private final ViewServiceGrpc.ViewServiceBlockingStub blockingStub;

	private static final AffineTransform3D transform = new AffineTransform3D();

	static
	{
		transform.scale( 0.05 );
	}

	public ViewServiceClient( Channel channel )
	{
		blockingStub = ViewServiceGrpc.newBlockingStub( channel );
	}

	public static void main( String... args ) throws Exception
	{
		// TODO: visualize tag colors
		// TODO: show multiple embryos
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
			ViewServiceClient client = new ViewServiceClient( channel );
			Model embryoA = openAppModel( context, projectPath );
			StopWatch watch = StopWatch.createAndStart();
			transferCoordinates( client, embryoA );
			System.out.println( watch );
		}
		finally
		{
			channel.shutdownNow(); //.awaitTermination( 5, TimeUnit.SECONDS );
		}
	}

	private static void transferCoordinates( ViewServiceClient client, Model embryoA )
	{
		ModelGraph graph = embryoA.getGraph();
		transform.set(getNormalizingTransform( graph.vertices() ));
		for ( Spot spot : getTrackletStarts( graph ) )
			transferTracklet( graph, spot, client );
	}

	private static RefSet<Spot> getTrackletStarts( ModelGraph graph )
	{
		Spot ref = graph.vertexRef();
		try {
			RefSet<Spot> set = new RefSetImp<>( graph.vertices().getRefPool() );
			for ( Spot spot : graph.vertices() )
			{
				if ( spot.incomingEdges().size() != 1 )
					set.add( spot );
				if ( spot.outgoingEdges().size() > 1 )
					for ( Link link : spot.outgoingEdges() )
						set.add(link.getTarget( ref ) );
			}
			return set;
		}
		finally {
			graph.releaseRef( ref );
		}
	}

	private static void transferTracklet( ModelGraph graph, Spot start, ViewServiceClient client )
	{
		Spot spot = graph.vertexRef();
		try
		{
			AddMovingSpotRequest.Builder request = AddMovingSpotRequest.newBuilder();
			request.setId( start.getInternalPoolIndex() );
			request.setLabel( start.getLabel() );
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
		RealPoint point = new RealPoint( 3 );
		transform.apply( spot, point );
		request.addCoordinates( point.getFloatPosition( 0 ) );
		request.addCoordinates( point.getFloatPosition( 1 ) );
		request.addCoordinates( point.getFloatPosition( 2 ) );
		request.addTimepoints( spot.getTimepoint() );
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

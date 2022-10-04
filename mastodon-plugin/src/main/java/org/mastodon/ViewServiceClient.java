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
import io.grpc.stub.StreamObserver;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.StopWatch;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ViewServiceClient
{

	private static final String projectPath = "/home/arzt/Datasets/Mette/E1.mastodon";

	//private static final String projectPath = "/home/arzt/Datasets/DeepLineage/Johannes/2022-01-27_Ml_NL45xNL26_fused_part5_2022-07-06_Matthias.mastodon";

	private final ViewServiceGrpc.ViewServiceBlockingStub blockingStub;

	private final ViewServiceGrpc.ViewServiceStub nonBlockingStub;

	public ViewServiceClient( Channel channel )
	{
		blockingStub = ViewServiceGrpc.newBlockingStub( channel );
		nonBlockingStub = ViewServiceGrpc.newStub( channel );
	}

	public static void main( String... args ) throws Exception
	{
		// TODO: synchronize object selection between blender and Mastodon
		// TODO: enable selecting the tag set
		// TODO: enable setting tags
		// TODO: synchronize time points between blender and Mastodon
		// TODO: show multiple embryos
		MamutAppModel appModel = MastodonUtils.showGuiAndGetAppModel( projectPath );
		transferEmbryo( appModel );
	}

	private void nonBlockingPrintActiveObject( MamutAppModel appModel )
	{
		GroupHandle groupHandle = appModel.getGroupManager().createGroupHandle();
		ModelGraph graph = appModel.getModel().getGraph();
		groupHandle.setGroupId( 0 );
		NavigationHandler<Spot, Link> navigationModel = groupHandle.getModel( appModel.NAVIGATION );
		FocusModel<Spot, Link> focusModel = new AutoNavigateFocusModel<>( appModel.getFocusModel(), navigationModel );
		GraphIdBimap<Spot, Link> graphIdBimap = graph.getGraphIdBimap();
		focusModel.listeners().add( () -> {
			Spot ref = graph.vertexRef();
			Spot focusedSpot = focusModel.getFocusedVertex( ref );
			int id = graphIdBimap.vertexIdBimap().getId( focusedSpot );
			SetActiveSpotRequest request = SetActiveSpotRequest.newBuilder().setId( id ).build();
			blockingStub.setActiveSpot( request );
		} );
		nonBlockingStub.subscribeToActiveSpotChange( Empty.newBuilder().build(), new StreamObserver<ActiveSpotResponse>()
		{
			@Override
			public void onNext( ActiveSpotResponse activeObjectIdResponse )
			{
				int id = activeObjectIdResponse.getId();
				System.out.println( id );
				if(id > 0)
				{
					Spot ref = graph.vertexRef();
					Spot vertex = graphIdBimap.getVertex( id, ref );
					focusModel.focusVertex( vertex );
					graph.releaseRef( graph.vertexRef() );
				}
			}

			@Override
			public void onError( Throwable throwable )
			{
				throwable.printStackTrace();
			}

			@Override
			public void onCompleted()
			{

			}
		} );
	}

	private static void transferEmbryo( MamutAppModel appModel ) throws Exception
	{
		Model model = appModel.getModel();
		ManagedChannel channel = ManagedChannelBuilder.forTarget( "localhost:50051" ).usePlaintext().build();
		Runtime.getRuntime().addShutdownHook( new Thread( channel::shutdown ) );
		ViewServiceClient client = new ViewServiceClient( channel );
		StopWatch watch = StopWatch.createAndStart();
		client.transferCoordinates( model.getGraph() );
		client.transferColors( model );
		client.transferTimePoint( 42 );
		client.nonBlockingPrintActiveObject( appModel );
		System.out.println( watch );
	}

	private void printActiveObject()
	{
		Iterator<ActiveSpotResponse> activeObjectIterator = blockingStub.subscribeToActiveSpotChange( Empty.newBuilder().build() );
		while(activeObjectIterator.hasNext()) {
			System.out.println(activeObjectIterator.next().getId());
		}
	}

	private void transferTimePoint( int timePoint )
	{
		blockingStub.setTimePoint(SetTimePointRequest.newBuilder()
				.setTimepoint(timePoint)
				.build());
	}

	private void transferCoordinates( ModelGraph graph )
	{
		AffineTransform3D transform = PointCloudNormalizationUtils.getNormalizingTransform( graph.vertices() );
		for ( Spot spot : getTrackletStarts( graph ) )
			transferTracklet( graph, spot, transform );
	}

	private void transferColors( Model model )
	{
		int defaultColor = 0x444444;
		TagSetStructure.TagSet tagSet = getTagSet( model, "2d112_many_colors" );
		SetSpotColorsRequest.Builder request = SetSpotColorsRequest.newBuilder();
		ObjTagMap<Spot, TagSetStructure.Tag> spotToTag = model.getTagSetModel().getVertexTags().tags( tagSet );
		RefSet<Spot> trackletStarts = getTrackletStarts( model.getGraph() );
		for ( Spot spot : trackletStarts ) {
			TagSetStructure.Tag tag = spotToTag.get( spot );
			request.addIds( spot.getInternalPoolIndex() );
			request.addColors( tag == null ? defaultColor : tag.color() );
		}
		blockingStub.setSpotColors( request.build() );
	}

	private static TagSetStructure.TagSet getTagSet( Model model, String name ) {
		TagSetModel<Spot, Link> tagSetModel = model.getTagSetModel();
		TagSetStructure tagSetStructure = tagSetModel.getTagSetStructure();
		for( TagSetStructure.TagSet tagSet : tagSetStructure.getTagSets() ) {
			if ( tagSet.getName().equals( name ))
				return tagSet;
		}
		throw new NoSuchElementException();
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

	private void transferTracklet( ModelGraph graph, Spot start, AffineTransform3D transform )
	{
		Spot spot = graph.vertexRef();
		try
		{
			AddMovingSpotRequest.Builder request = AddMovingSpotRequest.newBuilder();
			request.setId( start.getInternalPoolIndex() );
			request.setLabel( start.getLabel() );
			spot.refTo( start );
			coordinates( request, spot, transform );
			while ( spot.outgoingEdges().size() == 1 )
			{
				spot.outgoingEdges().iterator().next().getTarget( spot );
				if ( spot.incomingEdges().size() != 1 )
					break;
				coordinates( request, spot, transform );
			}
			blockingStub.addMovingSpot( request.build() );
		}
		finally
		{
			graph.releaseRef( spot );
		}
	}

	private void coordinates( AddMovingSpotRequest.Builder request, Spot spot, AffineTransform3D transform )
	{
		RealPoint point = new RealPoint( 3 );
		transform.apply( spot, point );
		request.addCoordinates( point.getFloatPosition( 0 ) );
		request.addCoordinates( point.getFloatPosition( 1 ) );
		request.addCoordinates( point.getFloatPosition( 2 ) );
		request.addTimepoints( spot.getTimepoint() );
	}

}

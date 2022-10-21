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
import net.imglib2.util.Pair;
import net.imglib2.util.StopWatch;
import net.imglib2.util.ValuePair;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefArrayList;
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
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ViewServiceClient
{

	private static final String projectPath = "/home/arzt/Datasets/Mette/E1.mastodon";

	private final ViewServiceGrpc.ViewServiceBlockingStub blockingStub;

	private final ViewServiceGrpc.ViewServiceStub nonBlockingStub;

	private final MamutAppModel appModel;

	private final GroupHandle groupHandle;

	private final NavigationHandler<Spot, Link> navigationModel;

	private final FocusModel<Spot, Link> focusModel;

	private final TimepointModel timePointModel;

	public ViewServiceClient( Channel channel, MamutAppModel appModel )
	{
		blockingStub = ViewServiceGrpc.newBlockingStub( channel );
		nonBlockingStub = ViewServiceGrpc.newStub( channel );
		this.appModel = appModel;
		this.groupHandle = appModel.getGroupManager().createGroupHandle();
		groupHandle.setGroupId( 0 );
		navigationModel = groupHandle.getModel( appModel.NAVIGATION );
		focusModel = new AutoNavigateFocusModel<>( appModel.getFocusModel(), navigationModel );
		timePointModel = groupHandle.getModel( appModel.TIMEPOINT );
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

	int known_active_object = -1;

	private void synchronizeFocusedObject()
	{
		ModelGraph graph = appModel.getModel().getGraph();
		GraphIdBimap<Spot, Link> graphIdBimap = graph.getGraphIdBimap();
		focusModel.listeners().add( () -> {
			sendFocusedSpot( graph, graphIdBimap );
		} );
		timePointModel.listeners().add( () -> {
			int timepoint = timePointModel.getTimepoint();
			transferTimePoint( timepoint );
		} );

		nonBlockingStub.subscribeToActiveSpotChange( Empty.newBuilder().build(), new StreamObserver<ActiveSpotResponse>()
		{
			@Override
			public void onNext( ActiveSpotResponse activeObjectIdResponse )
			{
				receiveFocusedSpotChange( activeObjectIdResponse );
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

	private void receiveFocusedSpotChange( ActiveSpotResponse activeObjectIdResponse )
	{
		int id = activeObjectIdResponse.getId();
		if(known_active_object == id)
			return;
		known_active_object = id;
		System.out.println( id );
		if(id < 0)
			return;
		ModelGraph graph = appModel.getModel().getGraph();
		Spot ref = graph.vertexRef();
		try {
			GraphIdBimap<Spot, Link> graphIdBimap = appModel.getModel().getGraphIdBimap();
			Spot vertex = graphIdBimap.getVertex( id, ref );
			selectAllBranchNodesAndEdges( vertex );
			focusModel.focusVertex( vertex );
		}
		finally {
			graph.releaseRef( graph.vertexRef() );
		}
	}

	private void selectAllBranchNodesAndEdges( Spot branchStart )
	{
		ModelGraph graph = appModel.getModel().getGraph();
		SelectionModel<Spot, Link> selectionModel = appModel.getSelectionModel();
		selectionModel.clearSelection();
		Pair<RefList<Spot>, RefList<Link>> pair = BranchGraphUtils.getBranchSpotsAndLinks( graph, branchStart );
		RefList<Spot> branchSpots = pair.getA();
		RefList<Link> branchEdges = pair.getB();
		selectionModel.setVerticesSelected( branchSpots, true );
		selectionModel.setEdgesSelected( branchEdges, true );
	}

	private void sendFocusedSpot( ModelGraph graph, GraphIdBimap<Spot, Link> graphIdBimap )
	{
		Spot ref = graph.vertexRef();
		Spot ref2 = graph.vertexRef();
		try
		{
			Spot focusedSpot = focusModel.getFocusedVertex( ref );
			if(focusedSpot == null)
				return;
			Spot focusedBranchStart = BranchGraphUtils.getBranchStart(focusedSpot, ref2 );
			int id = graphIdBimap.vertexIdBimap().getId( focusedBranchStart );
			known_active_object = id;
			SetActiveSpotRequest request = SetActiveSpotRequest.newBuilder().setId( id ).build();
			blockingStub.setActiveSpot( request );
		}
		finally
		{
			graph.releaseRef( ref );
			graph.releaseRef( ref2 );
		}
	}

	private static void transferEmbryo( MamutAppModel appModel )
	{
		Model model = appModel.getModel();
		ManagedChannel channel = ManagedChannelBuilder.forTarget( "localhost:50051" ).usePlaintext().build();
		Runtime.getRuntime().addShutdownHook( new Thread( channel::shutdown ) );
		ViewServiceClient client = new ViewServiceClient( channel, appModel );
		StopWatch watch = StopWatch.createAndStart();
		client.transferCoordinates( model.getGraph() );
		client.transferColors( model );
		client.transferTimePoint( 42 );
		client.synchronizeFocusedObject();
		MastodonUtils.printModelEvents(appModel);
		System.out.println( watch );
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
		for ( Spot spot : BranchGraphUtils.getAllBranchStarts( graph ) )
			transferTracklet( graph, spot, transform );
	}

	private void transferColors( Model model )
	{
		int defaultColor = 0x444444;
		TagSetStructure.TagSet tagSet = getTagSet( model, "2d112_many_colors" );
		SetSpotColorsRequest.Builder request = SetSpotColorsRequest.newBuilder();
		ObjTagMap<Spot, TagSetStructure.Tag> spotToTag = model.getTagSetModel().getVertexTags().tags( tagSet );
		RefSet<Spot> trackletStarts = BranchGraphUtils.getAllBranchStarts( model.getGraph() );
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

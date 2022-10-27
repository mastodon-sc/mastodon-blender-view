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
package org.mastodon.blender;

import javax.swing.SwingUtilities;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Pair;
import net.imglib2.util.StopWatch;
import org.mastodon.AddMovingSpotRequest;
import org.mastodon.ChangeMessage;
import org.mastodon.Empty;
import org.mastodon.SetActiveSpotRequest;
import org.mastodon.SetSpotColorsRequest;
import org.mastodon.SetTagSetListRequest;
import org.mastodon.SetTimePointRequest;
import org.mastodon.ViewServiceGrpc;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
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
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

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

	int known_active_object = -1;

	int knownTimePoint = 0;

	private TagSetStructure.TagSet tagSet;

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
		// TODO: have an deployable actual mastodon plugin
		// TODO: visualize time points in the hierarchy view using colors
		// TODO: synchronize object selection between blender and Mastodon
		// TODO: enable setting tags
		// TODO: show multiple embryos
		MamutAppModel appModel = MastodonUtils.showGuiAndGetAppModel( projectPath );
		transferEmbryo( appModel );
	}

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

		nonBlockingStub.subscribeToChange( Empty.newBuilder().build(), new StreamObserver<ChangeMessage>()
		{
			@Override
			public void onNext( ChangeMessage changeMessage )
			{
				processChangeMessage( changeMessage );
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

	private void processChangeMessage( ChangeMessage changeMessage )
	{
		switch ( changeMessage.getId() ) {
		case TIME_POINT:
			SwingUtilities.invokeLater( this::onTimePointChange );
			break;
		case ACTIVE_SPOT:
			SwingUtilities.invokeLater( this::onActiveSpotChange );
			break;
		case UPDATE_COLORS_REQUEST:
			transferColors();
			break;
		case SELECTED_TAG_SET:
			getSelectedTagSet();
			transferColors();
			break;
		default:
			System.err.println("Unexpected event received from blender mastodon plugin.");
		}
	}

	private void getSelectedTagSet()
	{
		int index = blockingStub.getSelectedTagSet( Empty.newBuilder().build() ).getIndex();
		List<TagSetStructure.TagSet> tagSets = appModel.getModel().getTagSetModel().getTagSetStructure().getTagSets();
		tagSet = index >= 0 && index < tagSets.size() ? tagSets.get( index ) : null;
	}

	private void onTimePointChange()
	{
		int timePoint = getTimePoint();
		System.out.println("on time point changed to: " + timePoint);
		if(timePoint == knownTimePoint)
			return;
		knownTimePoint = timePoint;
		timePointModel.setTimepoint( timePoint );
	}

	private int getTimePoint()
	{
		return blockingStub.getTimePoint( Empty.newBuilder().build() ).getTimePoint();
	}

	private void onActiveSpotChange()
	{
		int id = getActiveSpotId();
		if(known_active_object == id)
			return;
		known_active_object = id;
		System.out.println( id );
		if(id < 0)
			return;
		ModelGraph graph = appModel.getModel().getGraph();
		Spot ref = graph.vertexRef();
		Spot ref2 = graph.vertexRef();
		try {
			GraphIdBimap<Spot, Link> graphIdBimap = appModel.getModel().getGraphIdBimap();
			Spot branchStart = graphIdBimap.getVertex( id, ref );
			selectAllBranchNodesAndEdges( branchStart );
			focusModel.focusVertex( BranchGraphUtils.findVertexForTimePoint(branchStart, knownTimePoint, ref2) );
		}
		finally {
			graph.releaseRef( ref );
			graph.releaseRef( ref2 );
		}
	}

	private int getActiveSpotId()
	{
		return blockingStub.getActiveSpot( Empty.newBuilder().build() ).getId();
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
			if(known_active_object == id)
				return;
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
		MastodonUtils.logMastodonEvents(appModel);
		ManagedChannel channel = ManagedChannelBuilder.forTarget( "localhost:50051" ).usePlaintext().build();
		Runtime.getRuntime().addShutdownHook( new Thread( channel::shutdown ) );
		ViewServiceClient client = new ViewServiceClient( channel, appModel );
		StopWatch watch = StopWatch.createAndStart();
		client.transferCoordinates( model.getGraph() );
		client.tagSet = getSelectedTagSet( model, "2d112_many_colors" );
		client.transferColors();
		client.transferTimePoint( 42 );
		client.synchronizeFocusedObject();
		client.synchronizeTagSetList();
		System.out.println( watch );
	}

	private void synchronizeTagSetList()
	{
		transferTagSetList();
		appModel.getModel().getTagSetModel().listeners().add( this::transferTagSetList );
	}

	private void transferTagSetList()
	{
		List<TagSetStructure.TagSet> tagSets = appModel.getModel().getTagSetModel().getTagSetStructure().getTagSets();
		SetTagSetListRequest.Builder request = SetTagSetListRequest.newBuilder();
		for(TagSetStructure.TagSet tagSet : tagSets)
			request.addTagSetNames(tagSet.getName());
		blockingStub.setTagSetList(request.build());
	}

	private void transferTimePoint( int timePoint )
	{
		if( knownTimePoint == timePoint )
			return;
		knownTimePoint = timePoint;
		System.out.println("set time point to " + timePoint);
		blockingStub.setTimePoint( SetTimePointRequest.newBuilder()
				.setTimepoint(timePoint)
				.build());
	}

	private void transferCoordinates( ModelGraph graph )
	{
		AffineTransform3D transform = PointCloudNormalizationUtils.getNormalizingTransform( graph.vertices() );
		for ( Spot spot : BranchGraphUtils.getAllBranchStarts( graph ) )
			transferTracklet( graph, spot, transform );
	}

	private void transferColors()
	{
		Model model = appModel.getModel();;
		int defaultColor = 0x444444;
		SetSpotColorsRequest.Builder request = SetSpotColorsRequest.newBuilder();
		Function<Spot, TagSetStructure.Tag> spotToTag = tagSet == null ?
				spot -> null :
				model.getTagSetModel().getVertexTags().tags( tagSet )::get;
		RefSet<Spot> trackletStarts = BranchGraphUtils.getAllBranchStarts( model.getGraph() );
		for ( Spot spot : trackletStarts ) {
			TagSetStructure.Tag tag = spotToTag.apply( spot );
			request.addIds( spot.getInternalPoolIndex() );
			request.addColors( tag == null ? defaultColor : tag.color() );
		}
		blockingStub.setSpotColors( request.build() );
	}

	private static TagSetStructure.TagSet getSelectedTagSet( Model model, String name ) {
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

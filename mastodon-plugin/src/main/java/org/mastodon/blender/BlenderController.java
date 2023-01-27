/*-
 * #%L
 * A Mastodon plugin data allows to show the embryo in Blender.
 * %%
 * Copyright (C) 2022 - 2023 Matthias Arzt
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

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Pair;

import org.mastodon.SetSelectionRequest;
import org.mastodon.SetSpotPositionRequest;
import org.mastodon.collection.RefList;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointListener;
import org.mastodon.model.TimepointModel;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.spatial.SpatialIndex;
import org.scijava.Context;

import java.util.List;
import java.util.function.ToIntFunction;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Manages a Blender window.
 */
public class BlenderController
{

	private final ViewServiceClient client;

	private final MamutAppModel appModel;

	private final Model model;

	private final GroupHandle groupHandle;

	private final FocusModel<Spot, Link> focusModel;

	private final TimepointModel timePointModel;

	private final FocusListener focusListener = this::onFocusModelEvent;

	private final TimepointListener timepointListener = this::onTimepointModelEvent;

	private final TagSetModel.TagSetModelListener tagSetModelListener = this::sendTagSetList;

	private TagSetStructure.TagSet tagSet;

	private int knownTimepoint = 0;

	int known_active_object = -1;

	private TIntIntHashMap spotIdToBranchId;

	private TIntSet startIds;

	private AffineTransform3D transform;

	public BlenderController( Context context, MamutAppModel appModel ) {
		this.appModel = appModel;
		this.model = appModel.getModel();
		this.groupHandle = appModel.getGroupManager().createGroupHandle();
		this.groupHandle.setGroupId( -1 );
		NavigationHandler<Spot, Link> navigationModel = groupHandle.getModel( appModel.NAVIGATION );
		this.focusModel = new AutoNavigateFocusModel<>( appModel.getFocusModel(), navigationModel );
		this.timePointModel = groupHandle.getModel( appModel.TIMEPOINT );
		this.client = new ViewServiceClient( context, new ViewServiceListener() );
		sendCoordinates();
		sendColors();
		sendTagSetList();
		triggerRepaint();
		client.subscribeToChangeEvents();
		subscribeListeners();
		prepareSendProperties();
		//MastodonUtils.logMastodonEvents(appModel);
	}

	private void subscribeListeners()
	{
		focusModel.listeners().add( focusListener );
		timePointModel.listeners().add( timepointListener );
		model.getTagSetModel().listeners().add( tagSetModelListener );
	}

	private void unsubscribeListeners()
	{
		focusModel.listeners().remove( focusListener );
		timePointModel.listeners().remove( timepointListener );
		model.getTagSetModel().listeners().remove( tagSetModelListener );
	}

	private void onFocusModelEvent()
	{
		ModelGraph graph = model.getGraph();
		Spot ref = graph.vertexRef();
		sendFocusedSpot( focusModel.getFocusedVertex( ref ), graph );
	}

	public void sendFocusedSpot( Spot spot, ModelGraph graph )
	{
		Spot ref2 = graph.vertexRef();
		try
		{
			if(spot == null)
				return;
			Spot focusedBranchStart = BranchGraphUtils.getBranchStart(spot, ref2 );
			int id = focusedBranchStart.getInternalPoolIndex();
			if(known_active_object == id)
				return;
			known_active_object = id;
			client.sendActiveSpotId( id );
		}
		finally
		{
			graph.releaseRef( ref2 );
		}
	}

	private void onTimepointModelEvent()
	{
		//sendTimepoint();
		sendProperties();
		sendSelection();
	}

	private void sendSelection()
	{
		SetSelectionRequest.Builder request = SetSelectionRequest.newBuilder();
		int timepoint = timePointModel.getTimepoint();
		SelectionModel< Spot, Link > selection = appModel.getSelectionModel();
		for ( Spot spot : selection.getSelectedVertices() ) {
			if(timepoint == spot.getTimepoint())
				request.addIds( spotIdToBranchId.get( spot.getInternalPoolIndex() ) );
		}
		client.sendSelection( request.build() );
	}

	private void sendProperties()
	{
		SetSpotPositionRequest.Builder request = SetSpotPositionRequest.newBuilder();
		SpatialIndex< Spot > spotsOfTimepoint = model.getSpatioTemporalIndex().getSpatialIndex( timePointModel.getTimepoint() );
		float[] position = new float[3];
		for( Spot spotB : spotsOfTimepoint ) {
			int branchId = spotIdToBranchId.get( spotB.getInternalPoolIndex() );
			spotB.localize( position );
			request.addIds( branchId );
			transform.apply( position, position );
			request.addCoordinates( position[ 0 ] );
			request.addCoordinates( position[ 1 ] );
			request.addCoordinates( position[ 2 ] );
		}
		client.sendSpotVisibilityAndPosition(request.build());
	}

	private void prepareSendProperties()
	{
		ModelGraph graph = model.getGraph();
		spotIdToBranchId = new TIntIntHashMap();
		startIds = new TIntHashSet();
		transform = PointCloudNormalizationUtils.getNormalizingTransform( graph.vertices() );
		Spot spot = graph.vertexRef();
		try
		{
			for ( Spot start : BranchGraphUtils.getAllBranchStarts( model.getGraph() ) ) {
				spot.refTo( start );
				startIds.add( start.getInternalPoolIndex() );
				spotIdToBranchId.put( spot.getInternalPoolIndex(), start.getInternalPoolIndex() );
				while ( spot.outgoingEdges().size() == 1 )
				{
					spot.outgoingEdges().iterator().next().getTarget( spot );
					if ( spot.incomingEdges().size() != 1 )
						break;
					spotIdToBranchId.put( spot.getInternalPoolIndex(), start.getInternalPoolIndex() );
				}
			}
		}
		finally
		{
			graph.releaseRef( spot );
		}
	}

	private void triggerRepaint()
	{
		int timepoint = timePointModel.getTimepoint();
		sendTimepoint( timepoint + 1 );
		sendTimepoint( timepoint );
	}

	private void sendTimepoint()
	{
		int timepoint = timePointModel.getTimepoint();
		if( knownTimepoint == timepoint )
			return;
		sendTimepoint( timepoint );
	}

	private void sendTimepoint( int timepoint )
	{
		knownTimepoint = timepoint;
		client.sendTimepoint( timepoint );
	}

	private void sendCoordinates()
	{
		client.sendCoordinates( model.getGraph() );
	}

	private void sendColors() {
		sendColors( getSpotToColorFunction() );
	}

	public void sendColors( ToIntFunction< Spot > spotToColorFunction )
	{
		client.sendColors( model.getGraph(), spotToColorFunction );
	}

	private ToIntFunction<Spot> getSpotToColorFunction()
	{
		final int defaultColor = 0x444444;
		if( tagSet == null )
			return spot -> defaultColor;
		ObjTagMap<Spot, TagSetStructure.Tag> spotColors =
				model.getTagSetModel().getVertexTags().tags( tagSet );
		return spot -> {
			TagSetStructure.Tag tag = spotColors.get( spot );
			return tag != null ? tag.color() : defaultColor;
		};
	}

	private void sendTagSetList()
	{
		List<TagSetStructure.TagSet> tagSetList =
				model.getTagSetModel().getTagSetStructure().getTagSets();
		client.sendTagSetList( tagSetList );
	}

	private class ViewServiceListener implements ViewServiceClient.Listener {

		@Override
		public void onSyncGroupChanged()
		{
			groupHandle.setGroupId( client.receiveSyncGroupIndex() );
		}

		@Override
		public void onTimepointChanged()
		{
			SwingUtilities.invokeLater(this::updateTimepointModel );
		}

		private void updateTimepointModel() {
			int timePoint = client.receiveTimepoint();
			//System.out.println("on time point changed to: " + timePoint);
			if(timePoint == knownTimepoint)
				return;
			knownTimepoint = timePoint;
			timePointModel.setTimepoint( timePoint );
		}

		@Override
		public void onActiveSpotChanged()
		{
			SwingUtilities.invokeLater( this::onActiveSpotChange );
		}

		private void onActiveSpotChange()
		{
			int id = client.receiveActiveSpotId();
			if(known_active_object == id)
				return;
			known_active_object = id;
			//System.out.println("Blender => Mastodon: active spot changed to " + id);
			if(id < 0)
				return;
			ModelGraph graph = appModel.getModel().getGraph();
			Spot ref = graph.vertexRef();
			Spot ref2 = graph.vertexRef();
			try {
				GraphIdBimap<Spot, Link> graphIdBimap = appModel.getModel().getGraphIdBimap();
				Spot branchStart = graphIdBimap.getVertex( id, ref );
				selectAllBranchNodesAndEdges( branchStart );
				focusModel.focusVertex( BranchGraphUtils.findVertexForTimePoint(branchStart, knownTimepoint, ref2) );
			}
			finally {
				graph.releaseRef( ref );
				graph.releaseRef( ref2 );
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

		@Override
		public void onUpdateColorsRequest()
		{
			sendColors();
		}

		@Override
		public void onSelectedTagSetChanged()
		{
			tagSet = client.receiveTagSet(model.getTagSetModel().getTagSetStructure());
			sendColors();
		}

		@Override
		public void onConnectionLost()
		{
			unsubscribeListeners();
		}
	}
}

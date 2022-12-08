package org.mastodon.blender;

import javax.swing.SwingUtilities;
import net.imglib2.util.Pair;
import org.mastodon.collection.RefList;
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
import org.mastodon.model.tag.TagSetStructure;
import org.scijava.Context;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Manages a Blender window.
 */
public class BlenderController
{

	private final ViewServiceClient client;

	private final MamutAppModel appModel;

	private final Model model;

	private final GroupHandle groupHandle;

	private final NavigationHandler<Spot, Link> navigationModel;

	private final FocusModel<Spot, Link> focusModel;

	private final TimepointModel timePointModel;

	private TagSetStructure.TagSet tagSet;

	private int knownTimepoint = 0;

	int known_active_object = -1;

	public BlenderController( Context context, MamutAppModel appModel ) {
		this.appModel = appModel;
		this.model = appModel.getModel();
		this.groupHandle = appModel.getGroupManager().createGroupHandle();
		this.groupHandle.setGroupId( -1 );
		this.navigationModel = groupHandle.getModel( appModel.NAVIGATION );
		this.focusModel = new AutoNavigateFocusModel<>( appModel.getFocusModel(), navigationModel );
		this.timePointModel = groupHandle.getModel( appModel.TIMEPOINT );
		this.client = new ViewServiceClient( context, new ViewServiceListener() );
		sendCoordinates();
		sendColors();
		sendTagSetList();
		triggerRepaint();
		client.subscribeToChangeEvents();
		focusModel.listeners().add( this::onFocusModelEvent );
		timePointModel.listeners().add( this::onTimepointModelEvent );
		model.getTagSetModel().listeners().add( this::sendTagSetList );
		//MastodonUtils.logMastodonEvents(appModel);
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
		sendTimepoint();
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
		client.sendColors( model.getGraph(), getSpotToColorFunction() );
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
	}
}

package org.mastodon.blender;

import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.TimepointModel;
import org.mastodon.model.tag.TagSetStructure;
import org.scijava.Context;

/**
 * Manages a Blender window.
 */
public class BlenderManager
{

	private final ViewServiceClient client;

	private final MamutAppModel appModel;

	private final Model model;

	private final GroupHandle groupHandle;

	private final NavigationHandler<Spot, Link> navigationModel;

	private final FocusModel<Spot, Link> focusModel;

	private final TimepointModel timePointModel;

	private TagSetStructure.TagSet tagSet;

	public BlenderManager( Context context, MamutAppModel appModel ) {
		this.appModel = appModel;
		this.model = appModel.getModel();
		this.groupHandle = appModel.getGroupManager().createGroupHandle();
		this.groupHandle.setGroupId( -1 );
		this.navigationModel = groupHandle.getModel( appModel.NAVIGATION );
		this.focusModel = new AutoNavigateFocusModel<>( appModel.getFocusModel(), navigationModel );
		this.timePointModel = groupHandle.getModel( appModel.TIMEPOINT );
		this.client = ViewServiceClient.createViewServiceClient( context, appModel );
		client.transferCoordinates( model.getGraph() );
		client.transferColors();
		int timepoint = timePointModel.getTimepoint();
		client.transferTimePoint( timepoint + 1 );
		client.transferTimePoint( timepoint );
		client.synchronizeFocusedObject();
		client.synchronizeTagSetList();
	}

}

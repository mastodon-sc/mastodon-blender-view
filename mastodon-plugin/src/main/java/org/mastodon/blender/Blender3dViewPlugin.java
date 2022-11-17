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

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.blender.setup.BlenderSetup;
import org.mastodon.blender.setup.StartBlender;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@Plugin( type = MamutPlugin.class )
public class Blender3dViewPlugin extends AbstractContextual implements MamutPlugin
{
	@Parameter
	private Context context;

	private static final String SHOW_IN_BLENDER = "[blender-3d-view] show in blender";
	private static final String SETUP_BLENDER = "[blender-3d-view] setup blender";

	private static final String[] SHOW_IN_BLENDER_KEYS = { "not mapped" };
	private static final String[] SETUP_BLENDER_KEYS = { "not mapped" };

	private static final Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( SHOW_IN_BLENDER, "Show in Blender" );
		menuTexts.put( SETUP_BLENDER, "Setup Blender ..." );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( SHOW_IN_BLENDER, SHOW_IN_BLENDER_KEYS, "Show the spots in a Blender 3D view." );
			descriptions.add( SETUP_BLENDER, SETUP_BLENDER_KEYS, "Show a setup window that helps to configure Blender to be used from Mastodon." );
		}
	}

	private final AbstractNamedAction showInBlender;
	private final AbstractNamedAction setupBlender;

	private MamutPluginAppModel pluginAppModel;

	public Blender3dViewPlugin()
	{
		showInBlender = new RunnableAction( SHOW_IN_BLENDER, this::startBlenderView );
		setupBlender = new RunnableAction( SETUP_BLENDER, this::showSetup );
		updateEnabledActions();
	}

	@Override
	public void setAppPluginModel( final MamutPluginAppModel model )
	{
		this.pluginAppModel = model;
		updateEnabledActions();
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "File",
								item( SETUP_BLENDER )),
				menu( "Window",
								item( SHOW_IN_BLENDER )) );

	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( showInBlender, SHOW_IN_BLENDER_KEYS );
		actions.namedAction( setupBlender, SETUP_BLENDER_KEYS );
	}

	private void updateEnabledActions()
	{
		final MamutAppModel appModel = ( pluginAppModel == null ) ?
				null : pluginAppModel.getAppModel();
		showInBlender.setEnabled( appModel != null );
	}

	private void startBlenderView()
	{
		if ( pluginAppModel != null )
		{
			new Thread(() -> {
				int port = StartBlender.getFreePort();
				try {
					StartBlender.startBlender( context, port );
				}
				catch ( Throwable e ) {
					BlenderSetup.startSetupWithMessage( context, e );
					return;
				}
				ViewServiceClient.start( port, pluginAppModel.getAppModel() );
			}).start();
		}
	}

	private void showSetup() {
		BlenderSetup.showSetup( context );
	}

}

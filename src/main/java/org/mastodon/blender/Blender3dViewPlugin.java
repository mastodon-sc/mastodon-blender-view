/*-
 * #%L
 * A Mastodon plugin data allows to show the embryo in Blender.
 * %%
 * Copyright (C) 2022 - 2024 Matthias Arzt
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
import org.mastodon.blender.csv.ExportGraphAsCsvAction;
import org.mastodon.blender.csv.StartBlenderWithCsvAction;
import org.mastodon.blender.setup.BlenderSetup;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
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

	private static final String EXPORT_CSV = "[blender-3d-view] export graph as csv";

	private static final String START_BLENDER_WITH_CSV = "[blender-3d-view] start blender with csv";

	private static final String[] NO_KEYS = { "not mapped" };

	private static final Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( SHOW_IN_BLENDER, "New Interactive Blender Window" );
		menuTexts.put( SETUP_BLENDER, "Setup Blender Addon ..." );
		menuTexts.put( EXPORT_CSV, "Export CSV for Blender" );
		menuTexts.put( START_BLENDER_WITH_CSV, "Open CSV in Blender" );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( SHOW_IN_BLENDER, NO_KEYS, "Show the spots in a Blender 3D view." );
			descriptions.add( SETUP_BLENDER, NO_KEYS, "Show a setup window that helps to configure Blender to be used from Mastodon." );
			descriptions.add( EXPORT_CSV, NO_KEYS, "Export the Graph As CSV" );
			descriptions.add( START_BLENDER_WITH_CSV, NO_KEYS, "Export the graph as CSV and open it with Blender." );
		}
	}

	private final AbstractNamedAction showInBlender;

	private final AbstractNamedAction setupBlender;

	private final AbstractNamedAction exportCsv;

	private final AbstractNamedAction startBlenderWithCsv;

	private ProjectModel projectModel;

	public Blender3dViewPlugin()
	{
		showInBlender = new RunnableAction( SHOW_IN_BLENDER, this::startBlenderView );
		setupBlender = new RunnableAction( SETUP_BLENDER, this::showSetup );
		exportCsv = new RunnableAction( EXPORT_CSV, this::exportCsv );
		startBlenderWithCsv = new RunnableAction( START_BLENDER_WITH_CSV, this::startBlenderWithCsv );
		updateEnabledActions();
	}

	@Override
	public void setAppPluginModel( final ProjectModel model )
	{
		this.projectModel = model;
		updateEnabledActions();
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "Window",
						item( SHOW_IN_BLENDER ) ),
				menu( "Plugins",
						menu( "Blender",
								item( SHOW_IN_BLENDER ),
								item( SETUP_BLENDER ),
								item( START_BLENDER_WITH_CSV ),
								item( EXPORT_CSV ) ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( showInBlender, NO_KEYS );
		actions.namedAction( setupBlender, NO_KEYS );
		actions.namedAction( exportCsv, NO_KEYS );
		actions.namedAction( startBlenderWithCsv, NO_KEYS );
	}

	private void updateEnabledActions()
	{
		showInBlender.setEnabled( projectModel != null );
		exportCsv.setEnabled( projectModel != null );
		startBlenderWithCsv.setEnabled( projectModel  != null );
	}

	private void startBlenderView()
	{
		if ( projectModel != null )
		{
			new Thread(() -> {
				try {
					new BlenderController( projectModel );
				}
				catch ( StartBlenderException e ) {
					BlenderSetup.startSetupWithMessage( context, e );
				}
			}).start();
		}
	}

	private void showSetup()
	{
		new Thread(() -> BlenderSetup.showSetup( context ) ).start();
	}

	private void exportCsv()
	{
		ExportGraphAsCsvAction.run( projectModel );
	}

	private void startBlenderWithCsv()
	{
		StartBlenderWithCsvAction.run( projectModel );
	}
}

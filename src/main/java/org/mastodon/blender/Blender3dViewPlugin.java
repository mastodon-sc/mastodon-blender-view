/*-
 * #%L
 * A Mastodon plugin data allows to show the embryo in Blender.
 * %%
 * Copyright (C) 2022 - 2025 Matthias Arzt
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
import org.mastodon.blender.interactive.BlenderInteractiveCommand;
import org.mastodon.blender.setup.BlenderSettingsCommand;
import org.mastodon.blender.setup.BlenderSetup;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;

@Plugin( type = MamutPlugin.class )
public class Blender3dViewPlugin extends AbstractContextual implements MamutPlugin
{

	public static final String ADVANCED_VISUALS = "Advanced Visuals";

	public static final String LINKED_TO_MASTODON = "Linked to Mastodon";

	@Parameter
	private Context context;

	private static final String START_BLENDER_WITH_CSV = "[blender-3d-view] start detached blender with csv";

	private static final String SHOW_IN_BLENDER = "[blender-3d-view] start linked blender";

	private static final String EXPORT_CSV = "[blender-3d-view] export graph as csv";

	private static final String SETUP_BLENDER = "[blender-3d-view] setup blender";

	private static final String BLENDER_TEMPLATE_SETTINGS = "[blender-3d-view] configure blender template files";

	private static final String[] NO_KEYS = { "not mapped" };

	private static final Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( START_BLENDER_WITH_CSV, "New Blender View (" + ADVANCED_VISUALS + ")" );
		menuTexts.put( SHOW_IN_BLENDER, "New Blender View (" + LINKED_TO_MASTODON + ")" );
		menuTexts.put( EXPORT_CSV, "Export CSV for Blender" );
		menuTexts.put( SETUP_BLENDER, "Setup Blender Addon..." );
		menuTexts.put( BLENDER_TEMPLATE_SETTINGS, "Configure Blender Template Files..." );
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
			descriptions.add( START_BLENDER_WITH_CSV, NO_KEYS,
					"Export the graph as CSV and open it with Blender, detached from Mastodon." );
			descriptions.add( SHOW_IN_BLENDER, NO_KEYS, "Show the spots in a linked Blender 3D view." );
			descriptions.add( EXPORT_CSV, NO_KEYS, "Export the Graph As CSV" );
			descriptions.add( SETUP_BLENDER, NO_KEYS, "Show a setup window that helps to configure Blender to be used from Mastodon." );
			descriptions.add( BLENDER_TEMPLATE_SETTINGS, NO_KEYS,
					"Define template files to be used for the Mastodon Blender visualizations." );
		}
	}

	private final AbstractNamedAction startBlenderWithCsv;

	private final AbstractNamedAction showInBlender;

	private final AbstractNamedAction exportCsv;

	private final AbstractNamedAction setupBlender;

	private final AbstractNamedAction blenderSettings;

	private ProjectModel projectModel;

	public Blender3dViewPlugin()
	{
		startBlenderWithCsv = new RunnableAction( START_BLENDER_WITH_CSV, this::startBlenderWithCsv );
		showInBlender = new RunnableAction( SHOW_IN_BLENDER, this::startBlenderView );
		exportCsv = new RunnableAction( EXPORT_CSV, this::exportCsv );
		setupBlender = new RunnableAction( SETUP_BLENDER, this::showSetup );
		blenderSettings = new RunnableAction( BLENDER_TEMPLATE_SETTINGS, this::showBlenderSettings );
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
		return Collections.singletonList(
				menu(
						"Window",
						menu(
								"Blender Views",
								item( START_BLENDER_WITH_CSV ),
								item( SHOW_IN_BLENDER ),
								separator(),
								item( EXPORT_CSV ),
								item( BLENDER_TEMPLATE_SETTINGS ),
								item( SETUP_BLENDER )
						)
				) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( startBlenderWithCsv, NO_KEYS );
		actions.namedAction( showInBlender, NO_KEYS );
		actions.namedAction( exportCsv, NO_KEYS );
		actions.namedAction( setupBlender, NO_KEYS );
		actions.namedAction( blenderSettings, NO_KEYS );
	}

	private void updateEnabledActions()
	{
		startBlenderWithCsv.setEnabled( projectModel != null );
		showInBlender.setEnabled( projectModel != null );
		exportCsv.setEnabled( projectModel != null );
	}

	private void startBlenderView()
	{
		context.service( CommandService.class ).run( BlenderInteractiveCommand.class, true, "projectModel", projectModel,
				"context", projectModel.getContext() );
	}

	private void startBlenderWithCsv()
	{
		StartBlenderWithCsvAction.run( projectModel );
	}

	private void exportCsv()
	{
		ExportGraphAsCsvAction.run( projectModel );
	}

	private void showSetup()
	{
		new Thread(() -> BlenderSetup.showSetup( context ) ).start();
	}

	private void showBlenderSettings()
	{
		context.service( CommandService.class ).run( BlenderSettingsCommand.class, true );
	}
}

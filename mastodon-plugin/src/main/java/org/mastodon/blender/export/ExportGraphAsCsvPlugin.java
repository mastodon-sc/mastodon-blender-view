package org.mastodon.blender.export;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.awt.FileDialog;
import java.awt.Frame;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

/**
 * Plugin to export the graph as CSV.
 */
@Plugin( type = MamutPlugin.class )
public class ExportGraphAsCsvPlugin implements MamutPlugin
{

	private static final String ID = "[tomancak] export mastodon graph";

	private static final String[] KEYS = { "not mapped" };

	private static final Map< String, String > menuTexts =
			Collections.singletonMap( ID, "Export Graph As CSV" );

	private MamutPluginAppModel pluginAppModel = null;

	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( CommandDescriptions descriptions )
		{
			descriptions.add( ID, KEYS, "Export the Graph As CSV" );
		}
	}

	private final AbstractNamedAction action;

	public ExportGraphAsCsvPlugin()
	{
		action = new RunnableAction( ID, () -> {
			if ( pluginAppModel != null )
				run();
		} );
		updateEnabledActions();
	}

	@Override
	public void setAppPluginModel( MamutPluginAppModel model )
	{
		this.pluginAppModel = model;
		updateEnabledActions();
	}

	private void updateEnabledActions()
	{
		final MamutAppModel appModel = ( pluginAppModel == null ) ? null : pluginAppModel.getAppModel();
		action.setEnabled( appModel != null );
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", item( ID ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( action, KEYS );
	}

	private void run()
	{
		String filename = openCsvFileDialog();
		boolean isCancelled = filename == null;
		if ( isCancelled )
			return;
		GraphToCsvUtils.writeCsv( pluginAppModel.getAppModel().getModel(), filename );
	}

	private static String openCsvFileDialog()
	{
		// show a file save dialog with title "Export Graph As CSV" that allows to select a CSV file
		FileDialog dialog = new FileDialog( ( Frame ) null, "Export Graph As CSV", FileDialog.SAVE );
		dialog.setFilenameFilter( ( dir, name ) -> name.endsWith( ".csv" ) );
		dialog.setFile( "graph.csv" );
		dialog.setVisible( true );
		// return if no file was selected
		if ( dialog.getFile() == null )
			return null;
		return dialog.getDirectory() + dialog.getFile();
	}
}

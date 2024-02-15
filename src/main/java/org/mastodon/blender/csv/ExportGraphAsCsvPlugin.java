package org.mastodon.blender.csv;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.awt.FileDialog;
import java.awt.Frame;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

/**
 * Export the Mastodon graph as CSV file such that it can be opened with Blender.
 */
@Plugin( type = MamutPlugin.class )
public class ExportGraphAsCsvPlugin implements MamutPlugin
{

	private static final String ID = "[tomancak] export mastodon graph";

	private static final String[] KEYS = { "not mapped" };

	private static final Map< String, String > menuTexts = Collections.singletonMap( ID, "Export CSV for Blender" );

	private ProjectModel projectModel = null;

	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.BIGDATAVIEWER );
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
			if ( projectModel != null )
				run();
		} );
		updateEnabledActions();
	}

	@Override
	public void setAppPluginModel( ProjectModel model )
	{
		this.projectModel = model;
		updateEnabledActions();
	}

	private void updateEnabledActions()
	{
		action.setEnabled( projectModel != null );
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", menu( "Blender", item( ID ) ) ) );
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
		String projectFile = projectModel.getProject().getProjectRoot().getAbsolutePath();
		String filename = saveCsvFileDialog( projectFile.replace( ".mastodon", "" ) + ".csv" );
		boolean isCancelled = filename == null;
		if ( isCancelled )
			return;
		GraphToCsvUtils.writeCsv( projectModel.getModel(), filename );
	}

	private static String saveCsvFileDialog( String defaultFile )
	{
		// show a file save dialog with title "Export Graph As CSV" that allows to select a CSV file
		FileDialog dialog = new FileDialog( ( Frame ) null, "Export Graph As CSV", FileDialog.SAVE );
		dialog.setFilenameFilter( ( dir, name ) -> name.endsWith( ".csv" ) );
		dialog.setFile( defaultFile );
		dialog.setVisible( true );
		// return if no file was selected
		if ( dialog.getFile() == null )
			return null;
		return dialog.getDirectory() + dialog.getFile();
	}

}

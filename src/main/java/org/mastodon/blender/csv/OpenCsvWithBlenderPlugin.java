package org.mastodon.blender.csv;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.blender.setup.StartBlender;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.Context;
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
public class OpenCsvWithBlenderPlugin implements MamutPlugin
{

	private static final String ID = "[mastodon-blender-view] open csv in blender";

	private static final String[] KEYS = { "not mapped" };

	private static final Map< String, String > menuTexts = Collections.singletonMap( ID, "Open CSV in Blender" );

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
			descriptions.add( ID, KEYS, "Export the graph as CSV and open it with Blender." );
		}
	}

	private final AbstractNamedAction action;

	public OpenCsvWithBlenderPlugin()
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
		try
		{
			Context context = projectModel.getContext();
			String csv = Files.createTempFile( "mastodon-graph", ".csv" ).toFile().getAbsolutePath();
			GraphToCsvUtils.writeCsv( projectModel.getModel(), csv );
			String blenderProject = copyResource( "/csv/empty_with_geometry_nodes.blend" );
			String loadCsvPy = copyResource( "/csv/read_csv.py" );
			String tagset = selectTagSet( projectModel );

			List< String > args = Arrays.asList(
					blenderProject,
					"--python",
					loadCsvPy
			);
			Path blenderPath = StartBlender.getBlenderPath( context );
			List< String > command = new ArrayList<>();
			command.add( blenderPath.toString() );
			command.addAll( args );
			ProcessBuilder builder = new ProcessBuilder( command.toArray( new String[ 0 ] ) );
			builder.environment().put( "MASTODON_BLENDER_CSV_FILE", csv );
			builder.environment().put( "MASTODON_BLENDER_TAG_SET", tagset );
			builder.start();
		}
		catch ( CancellationException e ) {
			// ignore
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private static String selectTagSet( ProjectModel projectModel )
	{
		List< String > tagSets = projectModel.getModel().getTagSetModel().getTagSetStructure().getTagSets().stream().map( TagSetStructure.TagSet::getName ).collect( Collectors.toList() );
		Object result = JOptionPane.showInputDialog( null, "Please select a tag set for visualization", "Blender Using Geometry Nodes",
				JOptionPane.PLAIN_MESSAGE, null, tagSets.toArray(), tagSets.get( 0 ) );
		if ( result == null )
			throw new CancellationException();
		return (String) result;
	}

	private static String copyResource( String resourceName ) throws IOException
	{
		final String prefix = FilenameUtils.getBaseName( resourceName );
		final String suffix = "." + FilenameUtils.getExtension( resourceName );
		File tmp = File.createTempFile( prefix, suffix );
		URL source = StartBlender.class.getResource( resourceName );
		FileUtils.copyURLToFile( source, tmp );
		return tmp.getAbsolutePath();
	}
}

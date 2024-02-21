package org.mastodon.blender.csv;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mastodon.blender.setup.StartBlender;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.model.tag.TagSetStructure;

/**
 * Export the Mastodon graph as CSV file such that it can be opened with Blender.
 */
public class StartBlenderWithCsvAction
{

	public static void run(ProjectModel projectModel)
	{
		String tagset = showSelectTagSetDialog( projectModel );
		if ( tagset == null )
			return;
		new Thread(() -> {
			try
			{
				String csv = createCsv( projectModel );
				startBlenderWithCsv( projectModel, tagset, csv );
			}
			catch ( Throwable e )
			{
				e.printStackTrace();
			}
		}).start();
	}

	private static String createCsv( ProjectModel projectModel ) throws IOException
	{
		String csv = Files.createTempFile( "mastodon-graph", ".csv" ).toFile().getAbsolutePath();
		Model model = projectModel.getModel();
		ReentrantReadWriteLock lock = model.getGraph().getLock();
		lock.readLock().lock();
		try
		{
			GraphToCsvUtils.writeCsv( model, csv );
		}
		finally
		{
			lock.readLock().unlock();
		}
		return csv;
	}

	private static void startBlenderWithCsv( ProjectModel projectModel, String tagset, String csv ) throws IOException
	{
		String blenderFile = copyResource( "/csv/empty_with_geometry_nodes.blend" );
		String pythonScript = copyResource( "/csv/read_csv.py" );
		Map<String, String> environment = new HashMap<>();
		environment.put( "MASTODON_BLENDER_CSV_FILE", csv );
		environment.put( "MASTODON_BLENDER_TAG_SET", tagset );
		StartBlender.startBlenderRunPythonScript( projectModel.getContext(), blenderFile, pythonScript, environment );
	}

	private static String showSelectTagSetDialog( ProjectModel projectModel )
	{
		List< String > tagSets = projectModel.getModel().getTagSetModel().getTagSetStructure().getTagSets().stream().map( TagSetStructure.TagSet::getName ).collect( Collectors.toList() );
		Object result = JOptionPane.showInputDialog( null, "Please select a tag set for visualization", "Blender Using Geometry Nodes",
				JOptionPane.PLAIN_MESSAGE, null, tagSets.toArray(), tagSets.get( 0 ) );
		return (String) result;
	}

	private static String copyResource( String resourceName ) throws IOException
	{
		final String prefix = FilenameUtils.getBaseName( resourceName );
		final String suffix = "." + FilenameUtils.getExtension( resourceName );
		File tmp = File.createTempFile( prefix, suffix );
		URL source = StartBlender.class.getResource( resourceName );
		if ( source == null )
			throw new IOException( "Could not find resource: " + resourceName );
		FileUtils.copyURLToFile( source, tmp );
		return tmp.getAbsolutePath();
	}
}

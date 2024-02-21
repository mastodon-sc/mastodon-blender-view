package org.mastodon.blender.csv;

import java.awt.FileDialog;
import java.awt.Frame;

import org.mastodon.mamut.ProjectModel;

/**
 * Export the Mastodon graph as CSV file such that it can be opened with Blender.
 */
public class ExportGraphAsCsvAction
{

	public static void run( ProjectModel projectModel )
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

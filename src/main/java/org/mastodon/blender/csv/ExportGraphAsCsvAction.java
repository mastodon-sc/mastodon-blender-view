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
		GraphToCsvUtils.writeCsv( projectModel, filename );
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

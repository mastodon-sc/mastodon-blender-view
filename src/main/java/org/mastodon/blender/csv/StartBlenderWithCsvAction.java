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
package org.mastodon.blender.csv;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mastodon.blender.setup.BlenderSettingsService;
import org.mastodon.blender.setup.BlenderSetup;
import org.mastodon.blender.setup.StartBlender;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.scijava.Context;

/**
 * Export the Mastodon graph as CSV file such that it can be opened with Blender.
 */
public class StartBlenderWithCsvAction
{

	public static void run(ProjectModel projectModel)
	{
		ColoringModelMain< Spot, Link, BranchSpot, BranchLink > coloringModel = GraphToCsvUtils.createColoringModel( projectModel );
		String selectedColorScheme = ColorSchemeChoice.showDialog( projectModel, coloringModel );
		if ( selectedColorScheme == null )
			return;
		new Thread(() -> {
			try
			{
				String csv = createCsv( projectModel, coloringModel );
				try
				{
					startBlenderWithCsv( projectModel, selectedColorScheme, csv );
				}
				catch ( Exception e )
				{
					BlenderSetup.startSetupWithMessage( projectModel.getContext(), e );
				}
			}
			catch ( Throwable e )
			{
				e.printStackTrace();
			}
		}).start();
	}

	private static String createCsv( ProjectModel projectModel, ColoringModelMain< Spot, Link, BranchSpot, BranchLink > coloringModel )
			throws IOException
	{
		String csv = Files.createTempFile( "mastodon-graph", ".csv" ).toFile().getAbsolutePath();
		Model model = projectModel.getModel();
		ReentrantReadWriteLock lock = model.getGraph().getLock();
		lock.readLock().lock();
		try
		{
			GraphToCsvUtils.writeCsv( projectModel.getModel(), csv, coloringModel );
		}
		finally
		{
			lock.readLock().unlock();
		}
		return csv;
	}

	private static void startBlenderWithCsv( ProjectModel projectModel, String tagset, String csv ) throws IOException
	{
		Context context = projectModel.getContext();
		BlenderSettingsService service = context.service( BlenderSettingsService.class );
		String blenderFile = service.getCopyOfCsvBlenderTemplate().getAbsolutePath();
		String pythonScript = copyResource( "/csv/read_csv.py" );
		Map<String, String> environment = new HashMap<>();
		environment.put( "MASTODON_BLENDER_CSV_FILE", csv );
		environment.put( "MASTODON_BLENDER_TAG_SET", tagset );
		StartBlender.startBlenderRunPythonScript( context, blenderFile, pythonScript, environment );
	}

	private static String copyResource( String resourceName ) throws IOException
	{
		final String prefix = FilenameUtils.getBaseName( resourceName );
		final String suffix = "." + FilenameUtils.getExtension( resourceName );
		File tmp = File.createTempFile( prefix, suffix );
		tmp.deleteOnExit();
		URL source = StartBlender.class.getResource( resourceName );
		if ( source == null )
			throw new IOException( "Could not find resource: " + resourceName );
		FileUtils.copyURLToFile( source, tmp );
		return tmp.getAbsolutePath();
	}
}

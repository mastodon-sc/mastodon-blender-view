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
package org.mastodon.blender.setup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mastodon.blender.ViewServiceClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

public class BlenderSetupUtils
{

	public static Path findMyAddonFolder( Path blenderPath )
	{
		try
		{
			Path root = blenderPath.getParent();
			Path addonsFolder = Files.list( root )
					.map( path -> path.resolve( "scripts" ).resolve( "addons" ) )
					.filter( path -> Files.exists( path ) && Files.isDirectory( path ) )
					.findFirst()
					.orElseThrow( NoSuchElementException::new );
			return addonsFolder.resolve( StartBlender.ADDON_NAME );
		}
		catch ( IOException e )
		{
			throw new RuntimeException(e);
		}
	}

	public static boolean verifyBlenderBinary( Path blenderPath )
	{
		try
		{
			String output = runCommandGetOutput( blenderPath.toString(),
					"-b",
					"--python-expr",
					"print(\"Blender started from within Mastodon.\")" );
			return output.contains( "Blender started from within Mastodon." );
		}
		catch ( Exception e ) {
			return false;
		}
	}

	public static void installDependency( Path blenderPath )
			throws IOException, InterruptedException
	{
		URL resource = BlenderSetupUtils.class.getResource( "/blender-scripts/install_grpc_to_blender.py" );
		File destination = Files.createTempFile( "intall_grpc_to_blender", ".py" ).toFile();
		FileUtils.copyURLToFile( resource, destination );
		String output = runCommandGetOutput( blenderPath.toString(),
				"--background",
				"--python", destination.getAbsolutePath() );
		if ( ! output.contains( "dependencies installed" ) )
			throw new RuntimeException("dependency installation failed");
	}

	public static void copyAddon( Path blenderPath ) throws IOException
	{
		Path destination = prepareAddonDirectory( blenderPath );
		final String resource = "/blender-addon/";
		List<String> filenames = readLinesFromResource( resource + "file_list.txt" );
		for ( String filename : filenames )
			copyResourceToFile( resource + filename, destination.resolve( filename ) );
	}

	private static Path prepareAddonDirectory( Path blenderPath ) throws IOException
	{
		Path resolve = findMyAddonFolder( blenderPath );
		if( Files.exists( resolve ) )
			FileUtils.deleteDirectory( resolve.toFile() );
		Files.createDirectory( resolve );
		return resolve;
	}

	private static List<String> readLinesFromResource( String resource ) throws IOException
	{
		try (InputStream stream = BlenderSetupUtils.class.getResourceAsStream( resource ))
		{
			return IOUtils.readLines( stream, StandardCharsets.UTF_8 );
		}
	}

	private static void copyResourceToFile( String resource, Path resolve )
			throws IOException
	{
		try (InputStream stream = BlenderSetupUtils.class.getResourceAsStream( resource ))
		{
			FileUtils.copyInputStreamToFile( stream, resolve.toFile() );
		}
	}

	private static String runCommandGetOutput( String... command )
			throws IOException, InterruptedException
	{
		Process process = new ProcessBuilder( command ).start();
		String output = IOUtils.toString( process.getInputStream() );
		process.waitFor();
		return output;
	}

	public static void runAddonTest( Path blenderPath )
			throws IOException, InterruptedException
	{
		String script = "import mastodon_blender_view.mb_server as mb_server;" //
				+ " mb_server.delayed_start_server();import time; time.sleep(2)";
		int port = StartBlender.getFreePort();
		Process process = StartBlender.startBlender( blenderPath, //
				port,
				"--background", //
				"--python-expr", script );
		ViewServiceClient.closeBlender(port);
		process.waitFor();
	}

	public static boolean isMastodonAddonInstalled( Path blenderPath )
	{
		Path mastodonAddonFolder = findMyAddonFolder( blenderPath );
		return Files.exists( mastodonAddonFolder );
	}
}

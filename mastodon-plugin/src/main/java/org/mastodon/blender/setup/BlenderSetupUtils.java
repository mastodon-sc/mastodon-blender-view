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

	public static void installAddon( Path blenderPath ) throws IOException
	{
		final String resource = "/mastodon_blender_view.zip";
		Path tmpDir = Files.createTempDirectory( "mastodon_blender_setup" );
		Path addonZip = tmpDir.resolve( "mastodon_blender_view.zip" );
		copyResourceToFile( resource, addonZip );
		runBlenderInstall( blenderPath, addonZip );
	}

	private static void runBlenderInstall( Path blenderPath, Path addonZip )
	{
		String python = "import bpy; bpy.ops.preferences.addon_install(filepath='" + addonZip.toAbsolutePath().toString() + "')";
		String output = executePythonExpression( blenderPath, python );
		System.out.println(output);
	}

	public static void uninstallAddon( Path blenderPath )
	{
		String python = "import bpy; bpy.ops.preferences.addon_remove(module='mastodon_blender_view')";
		String output = executePythonExpression( blenderPath, python );
		System.out.println(output);
	}

	private static String executePythonExpression( Path blenderPath, String python )
	{
		return runCommandGetOutput( blenderPath.toString(), "--background", "--python-expr", python );
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
	{
		try
		{
			Process process = new ProcessBuilder( command ).start();
			String output = IOUtils.toString( process.getInputStream(), StandardCharsets.UTF_8 );
			process.waitFor();
			return output;
		}
		catch ( IOException | InterruptedException e )
		{
			throw new RuntimeException( e );
		}
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
		String python = "import addon_utils; print([module.__name__ for module in addon_utils.modules()])";
		String output = executePythonExpression( blenderPath, python );
		return output.contains( "'mastodon_blender_view'" );
	}
}
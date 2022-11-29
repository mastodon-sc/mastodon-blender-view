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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlenderSetupUtils
{

	public static boolean verifyBlenderBinary( Path blenderPath )
	{
		try {
			String output = runPythonWithinBlender( blenderPath,
					"print('Blender started from within Mastodon.')" );
			return output.contains( "Blender started from within Mastodon." );
		}
		catch ( Throwable e ) {
			return false;
		}
	}

	public static void installAddon( Path blenderPath ) throws IOException
	{
		Path tmpDir = Files.createTempDirectory( "mastodon_blender_setup" );
		Path addonZip = tmpDir.resolve( "install_mastodon_blender_addon.zip" );
		Path pythonScript = tmpDir.resolve( "install_addon.py" );
		FileUtils.copyURLToFile( BlenderSetupUtils.class.getResource( "/mastodon_blender_view.zip" ), addonZip.toFile() );
		FileUtils.copyURLToFile( BlenderSetupUtils.class.getResource( "/blender-scripts/install_addon.py" ), pythonScript.toFile() );
		String output = runCommandGetOutput( blenderPath.toString(), //
				"--background", //
				"--python", pythonScript.toAbsolutePath().toString(), //
				"--", addonZip.toAbsolutePath().toString() ); //
		if ( !output.contains( "dependencies installed" ) )
			throw new RuntimeException( "Installation of the dependencies for the mastodon_blender_view addon failed:\n" + output );
		if ( !output.contains( "mastodon blender view addon installed" ) )
			throw new RuntimeException( "Installation of the mastodon_blender_view addon failed:\n" + output );
		if ( !output.contains( "google RPC code compiled" ))
			throw new RuntimeException( "Installation of the mastodon_blender_view addon failed:\n" + output );
		Files.delete( pythonScript );
		Files.delete( addonZip );
		Files.delete( tmpDir );
	}

	public static void uninstallAddon( Path blenderPath )
	{
		String python = "import bpy; bpy.ops.preferences.addon_remove(module='mastodon_blender_view')";
		String output = runPythonWithinBlender( blenderPath, python );
	}

	public static boolean isMastodonAddonInstalled( Path blenderPath )
	{
		String python = "import addon_utils; print([module.__name__ for module in addon_utils.modules()])";
		String output = runPythonWithinBlender( blenderPath, python );
		return output.contains( "'mastodon_blender_view'" );
	}

	public static void runAddonTest( Path blenderPath )
			throws IOException, InterruptedException
	{
		String script = "import mastodon_blender_view.mb_server as mb_server;" //
				+ "import time;"
				+ "mb_server.delayed_start_server();"
				+ "time.sleep(8)";
		int port = StartBlender.getFreePort();
		Process process = StartBlender.startBlender( blenderPath, //
				port,
				"--background", //
				"--python-expr", script );
		ViewServiceClient.closeBlender(port);
		process.waitFor();
	}

	private static String runPythonWithinBlender( Path blenderPath, String python )
	{
		return runCommandGetOutput( blenderPath.toString(), "--background", "--python-expr", python );
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

}

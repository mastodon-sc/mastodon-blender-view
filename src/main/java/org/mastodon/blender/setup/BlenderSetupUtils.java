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
package org.mastodon.blender.setup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class BlenderSetupUtils
{

	public static boolean verifyBlenderBinary( Path blenderPath )
	{
		try
		{
			String expectOutput = "Blender started from within Mastodon.";
			String python = "print('" + expectOutput + "')";
			return runPythonWithinBlender( blenderPath, python, expectOutput );
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
		Result result = runCommand( blenderPath.toString(), //
				"--background", //
				"--python", pythonScript.toAbsolutePath().toString(), //
				"--", addonZip.toAbsolutePath().toString() ); //

		if ( result.exitCode != 0 )
			throw new RuntimeException( "Installation failed.\n" + result );

		if ( !result.stdout.contains( "dependencies installed" ) )
			throw new RuntimeException( "Installation of the dependencies for the mastodon_blender_view addon failed:\n" + result );

		if ( !result.stdout.contains( "mastodon blender view addon installed" ) )
			throw new RuntimeException( "Installation of the mastodon_blender_view addon failed:\n" + result );

		if ( !result.stdout.contains( "google RPC code compiled" ) )
			throw new RuntimeException( "Installation of the mastodon_blender_view addon failed:\n" + result );

		Files.delete( pythonScript );
		Files.delete( addonZip );
		Files.delete( tmpDir );
	}

	public static void uninstallAddon( Path blenderPath )
	{
		String python = "import bpy; " + //
				"bpy.ops.preferences.addon_remove(module='mastodon_blender_view'); " + //
				"print('uninstall completed');";
		runPythonWithinBlender( blenderPath, python, "uninstall completed" );
	}

	public static boolean isMastodonAddonInstalled( Path blenderPath )
	{
		String python = "import addon_utils; " + //
                "print([module.__name__ for module in addon_utils.modules()])";
		String expectedOutput = "'mastodon_blender_view'";
		boolean success = runPythonWithinBlender( blenderPath, python, expectedOutput );
		return success;
	}

	public static void runAddonTest( Path blenderPath )
			throws IOException, InterruptedException
	{
		String script = "import mastodon_blender_view.mb_server as mb_server;" //
				+ "import time;"
				+ "import bidict;" // import packages that are not necessary for the test but required for correct plugin execution
				+ "import grpc;"
				+ "import google.protobuf;"
				+ "import pandas;"
				+ "mb_server.delayed_start_server();"
				+ "time.sleep(8)";
		int port = StartBlender.getFreePort();
		Process process = StartBlender.startBlender( blenderPath, //
				null, //
				port,
				"--background", //
				"--python-expr", script );
		process.waitFor();
	}

	private static boolean runPythonWithinBlender( Path blenderPath, String python, String expectedOutput )
	{
		Result result = runCommand( blenderPath.toString(), "--background", "--python-expr", python );
		return result.exitCode == 0 && result.stdout.contains( expectedOutput );
	}

	private static Result runCommand( String... command )
	{
		try
		{
			Process process = new ProcessBuilder( command ).start();
			process.waitFor();
			String output = IOUtils.toString( process.getInputStream(), StandardCharsets.UTF_8 );
			String error = IOUtils.toString( process.getErrorStream(), StandardCharsets.UTF_8 );
			int exitCode = process.exitValue();
			return new Result( Arrays.asList( command ), output, error, exitCode );
		}
		catch ( IOException | InterruptedException e )
		{
			throw new RuntimeException( e );
		}
	}

	public static class Result
	{

		private final List< String > command;

		private final String stdout;

		private final String stderr;

		private final int exitCode;

		public Result( List< String > command, String stdout, String stderr,
				int exitCode )
		{
			this.command = command;
			this.stdout = stdout;
			this.stderr = stderr;
			this.exitCode = exitCode;
		}

		@Override
		public String toString()
		{
			final StringBuilder s = new StringBuilder();
			s.append( "Command:\n    " ).append( command.get( 0 ) );
			for ( int i = 1; i < command.size(); i++ )
				s.append( " " ).append( addQuotes( command.get( i ) ) );
			s.append( "\n\n" );
            s.append( "Exit Code:\n    " ).append( exitCode ).append("\n\n");
			s.append( "Command Output:\n\n" ).append( stdout ).append("\n\n");
			s.append( "Commend Error:\n\n" ).append( stderr ).append("\n\n");
			return s.toString();
		}

        static String addQuotes(String value)
        {
            boolean simple = value.matches("[a-zA-Z0-9_-]+");
            return simple ? value : "'" + value + "'";
        }
	}
}

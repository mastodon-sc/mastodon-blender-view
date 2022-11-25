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
import org.apache.commons.lang3.ArrayUtils;
import org.mastodon.blender.ViewServiceClient;
import org.scijava.Context;
import org.scijava.prefs.PrefService;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartBlender
{
	public static final String ADDON_NAME = "mastodon_blender_view";

	public static final String BLENDER_PATH_ENTRY = "BLENDER_BINARY_PATH";

	private static String emptyBlenderProject = "";

	public static void startBlender( Context context, int port ) throws IOException
	{
		startBlender( getBlenderPath( context ), port );
	}

	public static Process startBlender( Path blenderPath, int port, String... args )
			throws IOException
	{
		List<String> command = new ArrayList<>();
		command.add( blenderPath.toString() );
		command.add( emptyBlenderProject() );
		command.add( "--addons" );
		command.add( ADDON_NAME );
		boolean background = ArrayUtils.contains( args, "--background" )
				|| ArrayUtils.contains( args, "-b" );
		if ( ! background )
			command.addAll( screenSize() );
		command.addAll( Arrays.asList(args) );
		command.add("--");
		command.add("--mastodon-port");
		command.add(Integer.toString( port ));
		Process process = new ProcessBuilder( command.toArray(new String[0]) ).start();
		ViewServiceClient.waitForConnection( port );
		return process;
	}

	private static List<String> screenSize()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		List<String> command = new ArrayList<>();
		command.add( "--window-geometry" );
		command.add( Integer.toString( screenSize.width / 4  ) );
		command.add( Integer.toString( screenSize.height / 4  ) );
		command.add( Integer.toString( screenSize.height * 3 / 4  ) );
		command.add( Integer.toString( screenSize.height / 2  ) );
		return command;
	}

	public static int getFreePort()
	{
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public static Path getBlenderPath( Context context )
	{
		try
		{
			PrefService service = context.service( PrefService.class );
			String blender_path = service.get( StartBlender.class, BLENDER_PATH_ENTRY );
			return Paths.get( blender_path );
		}
		catch ( Throwable ignore ) {
			return null;
		}
	}

	public static void setBlenderPath( Context context, Path blenderPath )
	{
		PrefService service = context.service( PrefService.class );
		service.put( StartBlender.class, BLENDER_PATH_ENTRY, blenderPath.toString() );
	}

	public static String emptyBlenderProject() {
		if(!emptyBlenderProject.isEmpty())
			return emptyBlenderProject;
		try
		{
			File tmp = File.createTempFile( "empty", ".blend" );
			URL source = StartBlender.class.getResource( "/blender-scripts/empty.blend" );
			FileUtils.copyURLToFile(source, tmp);
			emptyBlenderProject = tmp.getAbsolutePath();
			return emptyBlenderProject;
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return "";
		}
	}
}

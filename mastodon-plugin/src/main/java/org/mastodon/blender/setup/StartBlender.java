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

import org.apache.commons.lang3.ArrayUtils;
import org.mastodon.blender.Blender3dViewPlugin;
import org.mastodon.blender.ViewServiceClient;
import org.scijava.Context;
import org.scijava.prefs.PrefService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StartBlender
{
	public static final String ADDON_NAME = "mastodon_blender_view";

	public static final String BLENDER_PATH_ENTRY = "BLENDER_BINARY_PATH";

	public static void startBlender( Context context ) throws IOException
	{
		startBlender( getBlenderPath( context ) );
	}

	public static Process startBlender( Path blenderPath, String... args )
			throws IOException
	{
		String[] command = { blenderPath.toString(), "--addons", ADDON_NAME };
		Process process = new ProcessBuilder( ArrayUtils.addAll( command, args ) ).start();
		ViewServiceClient.waitForConnection();
		return process;
	}

	public static Path getBlenderPath( Context context )
	{
		try
		{
			PrefService service = context.service( PrefService.class );
			String blender_path = service.get( Blender3dViewPlugin.class, BLENDER_PATH_ENTRY );
			return Paths.get( blender_path );
		}
		catch ( Throwable ignore ) {
			return null;
		}
	}

	public static void setBlenderPath( Context context, Path blenderPath )
	{
		PrefService service = context.service( PrefService.class );
		service.put( Blender3dViewPlugin.class, BLENDER_PATH_ENTRY, blenderPath.toString() );
	}
}

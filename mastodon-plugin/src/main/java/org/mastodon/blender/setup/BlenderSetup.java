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
package org.mastodon.blender.setup;

import org.mastodon.blender.ExceptionDialog;
import org.scijava.Context;

import java.nio.file.Files;
import java.nio.file.Path;

public class BlenderSetup
{
	/**
	 * Shows the setup window for selecting the Blender binary path and
	 * installing the Mastdon-Blender-Addon.
	 */
	public static void showSetup( Context context ) {
		Path blenderPath = StartBlender.getBlenderPath( context );
		Path newBlenderPath = BlenderSetupController.showSetup( blenderPath );
		if( newBlenderPath != null )
			StartBlender.setBlenderPath( context, newBlenderPath );
	}

	/**
	 * Shows a message to tell the user what is wrong about the Blender
	 * configuration. And allows to start Blender setup window.
	 * successfully.
	 */
	public static void startSetupWithMessage( Context context, Throwable e )
	{
		Path blenderPath = StartBlender.getBlenderPath( context );
		String message = appropriateMessage( blenderPath );
		boolean ok = ExceptionDialog.showOkCancelDialog( null,
				"Problem Starting Blender",
				message,
				e,
				"Setup Blender", "Cancel");
		if( ! ok )
			return;
		showSetup( context );
	}

	private static String appropriateMessage( Path blenderPath )
	{
		// not setup
		if( blenderPath == null)
			return "Blender has not yet been configured to be used from Mastodon.\n" +
					"Would you like to setup Blender.";

		// blender not found
		if( !Files.exists(blenderPath) )
			return "Blender wasn't found in the expected location:\n" +
					blenderPath.toAbsolutePath().toString() + "\n" +
					"Would you like to setup Blender to be used with Mastodon.";

		// addon not installed
		if( !BlenderSetupUtils.isMastodonAddonInstalled( blenderPath ) )
			return "The Mastodon Addon for Blender has not yet been installed.\n"+
					"Would you like to setup Blender to be used with Mastodon.";

		// plugin not installed
		// different plugin version
		// different error
		return "Blender didn't start as expected.\n" +
				"Maybe it has not been setup correctly, or something else changed.\n" +
				"Would you like to setup Blender to be used with Mastodon.";
	}
}

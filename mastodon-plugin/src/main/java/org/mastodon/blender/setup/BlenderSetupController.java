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

import javax.swing.SwingUtilities;
import org.mastodon.blender.ExceptionDialog;

import java.nio.file.Files;
import java.nio.file.Path;

public class BlenderSetupController implements BlenderSetupView.Listener
{

	private final BlenderSetupView frame;

	private State state = State.PATH_NOT_SET;

	private Path blenderPath = null;

	private boolean finished = false;

	/**
	 * Show dialog that allows to setup Blender.
	 * @return blender binary path is Blender has been setup correctly;
	 */
	public static Path showSetup( Path blenderPath ) {
		BlenderSetupController controller = new BlenderSetupController( blenderPath );
		if(controller.finished )
			return controller.blenderPath;
		return null;
	}

	private BlenderSetupController( Path blenderPath ) {
		frame = new BlenderSetupView( this );
		setState( State.PATH_NOT_SET );
		setBlenderPath( blenderPath );
		frame.pack();
		frame.setVisible( true );
	}

	private void setState( State state )
	{
		this.state = state;
		frame.setPathFeedback( state.pathFeedback );
		frame.setInstallAddonEnabled( state.enableInstall );
		frame.setTestAddonEnabled( state.enableTest );
		frame.setAddonFeedback( state.addonFeedback );
		frame.setFinishEnabled( state.enableFinish );
	}

	@Override
	public void setBlenderPath( Path blenderPath )
	{
		this.blenderPath = blenderPath;
		frame.setBlenderPath( blenderPath );
		if ( this.blenderPath == null)
			setState( State.PATH_NOT_SET );
		else if ( Files.isDirectory( this.blenderPath ) )
			setState( State.WRONG_PATH_DIRECTORY );
		else if ( !Files.exists( this.blenderPath ) )
			setState( State.WRONG_PATH_NOT_FOUND );
		else if ( !BlenderSetupUtils.verifyBlenderBinary( this.blenderPath ) )
			setState( State.WRONG_PATH );
		else if ( !BlenderSetupUtils.isMastodonAddonInstalled( this.blenderPath ))
			setState( State.BLENDER_INSTALLED );
		else
			setState( State.ADDON_INSTALLED );
	}

	@Override
	public void installAddonClicked()
	{
		if(!state.enableInstall)
			return;
		setState( State.INSTALLING_ADDON );
		new Thread( this::installAddonThread ).start();
	}

	private void installAddonThread()
	{
		try
		{
			BlenderSetupUtils.installDependency( blenderPath );
			BlenderSetupUtils.copyAddon( blenderPath );
			SwingUtilities.invokeLater( () -> setState( State.ADDON_INSTALLED ) );
		}
		catch ( Exception e ) {
			SwingUtilities.invokeLater( () -> {
				setState( State.BLENDER_INSTALLED );
				ExceptionDialog.showOkDialog( frame,
						"Addon Installation Failed",
						"There was an exception during the installation:", e );
			} );
		}
	}

	@Override
	public void testAddonClicked()
	{
		if(!state.enableTest)
			return;
		setState( State.TESTING_ADDON );
		new Thread( this::testAddonThread ).start();
	}

	private void testAddonThread()
	{
		try {
			BlenderSetupUtils.runAddonTest( blenderPath );
			SwingUtilities.invokeLater( () -> setState( State.TEST_SUCCEEDED ) );
		}
		catch ( Exception e ) {
			SwingUtilities.invokeLater( () -> {
				setState( State.TEST_FAILED );
				ExceptionDialog.showOkDialog( frame,
						"Test Failed",
						"There was an exception during the test:", e );
			} );
		}
	}

	@Override
	public void finishClicked()
	{
		finished = true;
		frame.setVisible( false );
	}

	@Override
	public void cancelClicked()
	{
		frame.setVisible( false );
	}
}

/**
 * The Blender Setup, is always in a specific states. The possible states are
 * declared in this class {@link State}. Depending on the state the three
 * buttons "install addon", "test addon" and "finish" are enabled or disabled.
 * <p>
 * Additionally the setup also shows two feedback texts: The first is a feedback
 * on the correctness of the selected blender binary. And the second feedback
 * is about the status of the installed addon. The feedback tests declared in
 * this state class too.
 */
enum State
{

	PATH_NOT_SET(
			"please select",
			"",
			""
	),

	WRONG_PATH_DIRECTORY(
			"This is a directory, please select the executable Blender file",
			"",
			""
	),

	WRONG_PATH_NOT_FOUND(
			"file doesn't exits",
			"",
			""
	),

	WRONG_PATH(
			"doesn't seem to be a correct blender executable",
			"",
			""
	),

	BLENDER_INSTALLED(
			"looks good",
			"Please install addon!",
			"install"
	),

	INSTALLING_ADDON(
			"looks good",
			"Installing addon ...",
			"install"
	),

	ADDON_INSTALLED(
			"looks good",
			"Addon is installed. Please test it.",
			"install, test"
	),

	TESTING_ADDON(
			"looks good",
			"Testing addon ...",
			"install"
	),

	TEST_FAILED(
			"looks good",
			"Something went wrong. Maybe try to install the addon again!?",
			"install, test"
	),

	TEST_SUCCEEDED(
			"looks good",
			"Great! The addon works as expected. Click \"Finish\" the complete the setup.",
			"install, test, finish"
	);

	public final boolean enableInstall;

	public final boolean enableTest;

	public final boolean enableFinish;

	public final String pathFeedback;

	public final String addonFeedback;

	State( String pathFeedback, String addonFeedback, String enable )
	{
		this.pathFeedback = pathFeedback;
		this.addonFeedback = addonFeedback;
		this.enableInstall = enable.contains( "install" );
		this.enableTest = enable.contains( "test" );
		this.enableFinish = enable.contains( "finish" );
	}

}

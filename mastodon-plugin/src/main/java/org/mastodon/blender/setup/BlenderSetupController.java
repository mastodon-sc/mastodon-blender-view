package org.mastodon.blender.setup;

import javax.swing.SwingUtilities;

import org.jetbrains.annotations.NotNull;
import org.mastodon.blender.ExceptionDialog;

import java.nio.file.Files;
import java.nio.file.Path;

public class BlenderSetupController
{

	private final BlenderSetupView frame;

	private Path blenderPath = null;

	private State state = PATH_NOT_SET;

	private boolean finishClicked = false;

	private BlenderSetupController( Path blenderPath ) {

		frame = new BlenderSetupView( new BlenderSetupView.Listener()
		{
			@Override
			public void pathSelected( Path blenderPath )
			{
				setBlenderPath( blenderPath );
			}

			@Override
			public void installAddonClicked()
			{
				installAddon();
			}

			@Override
			public void testAddonClicked()
			{
				testAddon();
			}

			@Override
			public void finishClicked()
			{
				finishClicked = true;
				frame.setVisible( false );
			}

			@Override
			public void cancelClicked()
			{
				frame.setVisible( false );
			}
		} );
		setBlenderPath( blenderPath );
		frame.pack();
		frame.setVisible( true );
	}

	/**
	 * Show dialog that allows to setup Blender.
	 * @return blender binary path is Blender has been setup correctly;
	 */
	public static Path showSetup( Path blenderPath ) {
		BlenderSetupController controller = new BlenderSetupController( blenderPath );
		if(controller.finishClicked)
			return controller.blenderPath;
		return null;
	}

	private void setBlenderPath( Path blenderPath )
	{
		this.blenderPath = blenderPath;
		frame.setBlenderPath( blenderPath );
		setState( getNewPathInitialState() );
	}

	public void setState( State state )
	{
		this.state = state;
		frame.setPathFeedback( state.pathFeedback );
		frame.setInstallAddonEnabled( state.enableInstall );
		frame.setTestAddonEnabled( state.enableTest );
		frame.setAddonFeedback( state.addonFeedback );
		frame.setFinishEnabled( state.enableFinish );
	}

	@NotNull
	private State getNewPathInitialState()
	{
		if ( blenderPath == null)
			return PATH_NOT_SET;
		else if ( Files.isDirectory( blenderPath ) )
			return WRONG_PATH_DIRECTORY;
		else if ( !Files.exists( blenderPath ) )
			return WRONG_PATH_NOT_FOUND;
		else if ( !BlenderSetupUtils.verifyBlenderBinary( blenderPath ) )
			return WRONG_PATH;
		else if ( !BlenderSetupUtils.isMastodonAddonInstalled( blenderPath ))
			return BLENDER_INSTALLED;
		else
			return ADDON_INSTALLED;
	}

	private void installAddon()
	{
		if(!state.enableInstall)
			return;
		setState( INSTALLING_ADDON );
		new Thread( this::installAddonThread ).start();
	}

	private void installAddonThread()
	{
		try
		{
			BlenderSetupUtils.installDependency( blenderPath );
			BlenderSetupUtils.copyAddon( blenderPath );
			SwingUtilities.invokeLater( () -> setState( ADDON_INSTALLED ) );
		}
		catch ( Exception e ) {
			SwingUtilities.invokeLater( () -> {
				setState( BLENDER_INSTALLED );
				ExceptionDialog.show( frame,
						"Addon Installation Failed",
						"There was an exception during the installation:", e );
			} );
		}
	}

	private void testAddon()
	{
		if(!state.enableTest)
			return;
		setState( TESTING_ADDON );
		new Thread( this::testAddonThread ).start();
	}

	private void testAddonThread()
	{
		try {
			BlenderSetupUtils.runAddonTest( blenderPath );
			SwingUtilities.invokeLater( () -> setState( TEST_SUCCEEDED ) );
		}
		catch ( Exception e ) {
			SwingUtilities.invokeLater( () -> {
				setState( TEST_FAILED );
				ExceptionDialog.show( frame,
						"Test Failed",
						"There was an exception during the test:", e );
			} );
		}
	}

	private static class State {

		private final boolean enableInstall;

		private final boolean enableTest;

		private final boolean enableFinish;

		private final String pathFeedback;

		private final String addonFeedback;

		private State( String pathFeedback, String addonFeedback, boolean enableInstall, boolean enableTest, boolean enableFinish )
		{
			this.enableInstall = enableInstall;
			this.pathFeedback = pathFeedback;
			this.addonFeedback = addonFeedback;
			this.enableTest = enableTest;
			this.enableFinish = enableFinish;
		}

		private static State wrongPathState(String pathFeedback) {
			return new State( pathFeedback, "", false, false, false );
		}

		public static State correctPathState( String addonFeedback, String enable )
		{
			boolean enableTest = enable.contains("test");
			boolean enableFinish = enable.contains("finish");
			return new State( "looks good", addonFeedback, true, enableTest, enableFinish );
		}
	}

	private static final State PATH_NOT_SET = State.wrongPathState( "please select" );
	private static final State WRONG_PATH_DIRECTORY = State.wrongPathState( "This is a directory, please select the executable Blender file" );
	private static final State WRONG_PATH_NOT_FOUND = State.wrongPathState( "file doesn't exits" );
	private static final State WRONG_PATH = State.wrongPathState( "doesn't seem to be a correct blender executable" );
	private static final State BLENDER_INSTALLED = State.correctPathState( "please install addon", "" );
	private static final State INSTALLING_ADDON = State.correctPathState( "Installing addon ...", "" );
	private static final State ADDON_INSTALLED = State.correctPathState( "Addon installed, please test it.", "test" );
	private static final State TESTING_ADDON = State.correctPathState( "Testing addon ...", "" );
	private static final State TEST_FAILED = State.correctPathState( "Something went wrong. Maybe try to install the addon again!?", "test" );
	private static final State TEST_SUCCEEDED = State.correctPathState( "Great! The addon works as expected. Click \"Finish\" the complete the setup.", "test, finish" );
}

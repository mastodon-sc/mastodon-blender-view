package org.mastodon.blender.setup;

import javax.swing.SwingUtilities;

import org.mastodon.blender.ExceptionDialog;

import java.nio.file.Files;
import java.nio.file.Path;

public class BlenderSetupController implements BlenderSetupView.Listener
{

	private final BlenderSetupView frame;

	private BlenderSetupState state = BlenderSetupState.PATH_NOT_SET;

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
		setState( BlenderSetupState.PATH_NOT_SET );
		setBlenderPath( blenderPath );
		frame.pack();
		frame.setVisible( true );
	}

	private void setState( BlenderSetupState state )
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
			setState( BlenderSetupState.PATH_NOT_SET );
		else if ( Files.isDirectory( this.blenderPath ) )
			setState( BlenderSetupState.WRONG_PATH_DIRECTORY );
		else if ( !Files.exists( this.blenderPath ) )
			setState( BlenderSetupState.WRONG_PATH_NOT_FOUND );
		else if ( !BlenderSetupUtils.verifyBlenderBinary( this.blenderPath ) )
			setState( BlenderSetupState.WRONG_PATH );
		else if ( !BlenderSetupUtils.isMastodonAddonInstalled( this.blenderPath ))
			setState( BlenderSetupState.BLENDER_INSTALLED );
		else
			setState( BlenderSetupState.ADDON_INSTALLED );
	}

	@Override
	public void installAddonClicked()
	{
		if(!state.enableInstall)
			return;
		setState( BlenderSetupState.INSTALLING_ADDON );
		new Thread( this::installAddonThread ).start();
	}

	private void installAddonThread()
	{
		try
		{
			BlenderSetupUtils.installDependency( blenderPath );
			BlenderSetupUtils.copyAddon( blenderPath );
			SwingUtilities.invokeLater( () -> setState( BlenderSetupState.ADDON_INSTALLED ) );
		}
		catch ( Exception e ) {
			SwingUtilities.invokeLater( () -> {
				setState( BlenderSetupState.BLENDER_INSTALLED );
				ExceptionDialog.show( frame,
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
		setState( BlenderSetupState.TESTING_ADDON );
		new Thread( this::testAddonThread ).start();
	}

	private void testAddonThread()
	{
		try {
			BlenderSetupUtils.runAddonTest( blenderPath );
			SwingUtilities.invokeLater( () -> setState( BlenderSetupState.TEST_SUCCEEDED ) );
		}
		catch ( Exception e ) {
			SwingUtilities.invokeLater( () -> {
				setState( BlenderSetupState.TEST_FAILED );
				ExceptionDialog.show( frame,
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

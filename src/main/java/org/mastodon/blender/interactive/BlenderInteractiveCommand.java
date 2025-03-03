package org.mastodon.blender.interactive;

import org.mastodon.blender.BlenderController;
import org.mastodon.blender.StartBlenderException;
import org.mastodon.blender.setup.BlenderSetup;
import org.mastodon.mamut.ProjectModel;
import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.Initializable;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * This dialog allows the user to set the time scaling factor for Blender Interactive view.
 */
@Plugin( type = Command.class, name = "Set time scaling for Blender Interactive View" )
public class BlenderInteractiveCommand implements Command, Initializable, Cancelable
{

	@Parameter
	private Context context;

	@Parameter
	private ProjectModel projectModel;

	@Parameter( visibility = ItemVisibility.MESSAGE ) // Text that is displayed in the dialog and never changes.
	private final String description = "<html>"
			+ "\n<body width=10cm align=left>"
			+ "\nA time scaling factor of 1 means that there is no time scaling and the frame number in Big Data View / TrackScheme views matches the frame number in Blender.<br><br>"
			+ "\nA Time scaling factor larger than 1 means that interpolation between frames in the Blender view is computed, which leads to a smoother animation."
			+ "\nFrame numbers in Blender are then multiplied by the time scaling factor."
			+ "\n</body>"
			+ "\n</html>";

	@Parameter( label = "Time scaling factor", description = "1 means no time scaling. Larger means leads to time scaling and interpolation.", min = "1" )
	private int timeScalingFactor = 1;

	@Override
	public void run()
	{
		startBlenderView();
	}

	@Override
	public boolean isCanceled()
	{
		return false;
	}

	@Override
	public void cancel( String reason )
	{
		// The goal is to have a cancel button in the dialog. This method does not need to do anything to achieve that.
	}

	@Override
	public String getCancelReason()
	{
		return null;
	}

	private void startBlenderView()
	{
		if ( projectModel != null )
		{
			new Thread( () -> {
				try
				{
					new BlenderController( projectModel, timeScalingFactor );
				}
				catch ( StartBlenderException e )
				{
					BlenderSetup.startSetupWithMessage( context, e );
				}
			} ).start();
		}
	}
}

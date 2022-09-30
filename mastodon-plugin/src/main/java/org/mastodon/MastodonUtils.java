package org.mastodon;

import javax.swing.WindowConstants;
import mpicbg.spim.data.SpimDataException;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import java.io.IOException;

public class MastodonUtils
{
	private MastodonUtils() {
		// prevent from instantiation
	}

	static Model openMastodonModel( Context context, String projectPath )
	{
		try
		{
			MamutProject project = new MamutProjectIO().load( projectPath );
			final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
			final boolean isNewProject = project.getProjectRoot() == null;
			if ( !isNewProject )
			{
				try (final MamutProject.ProjectReader reader = project.openForReading())
				{
					final RawGraphIO.FileIdToGraphMap<Spot, Link> idmap = model.loadRaw( reader );
					// Load features.
					MamutRawFeatureModelIO.deserialize( context, model, idmap, reader );
				}
				catch ( final ClassNotFoundException e )
				{
					e.printStackTrace();
				}
			}
			return model;
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	public static MamutAppModel showGuiAndGetAppModel(String projectPath) {
		try {
			final WindowManager windowManager = new WindowManager( new Context() );
			windowManager.getProjectManager().open( new MamutProjectIO().load( projectPath ) );
			final MainWindow mainWindow = new MainWindow(windowManager);
			mainWindow.setVisible( true );
			mainWindow.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
			return windowManager.getAppModel();
		} catch (IOException | SpimDataException e) {
			throw new RuntimeException(e);
		}
	}
}

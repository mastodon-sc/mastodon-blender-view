package org.mastodon.blender;

import javax.swing.WindowConstants;
import mpicbg.spim.data.SpimDataException;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationListener;
import org.mastodon.model.TimepointModel;
import org.mastodon.model.tag.TagSetModel;
import org.scijava.Context;

import java.io.IOException;
import java.util.Arrays;

public class MastodonUtils
{

	private static final boolean LOG_STACK_TRACE = false;

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

	static void logMastodonEvents( MamutAppModel appModel )
	{
		GroupHandle groupHandle = appModel.getGroupManager().createGroupHandle();
		groupHandle.setGroupId( 0 );
		logNavigationHandle( groupHandle.getModel( appModel.NAVIGATION ) );
		logTimePointModel(groupHandle.getModel( appModel.TIMEPOINT ) );
		logFocusModel(appModel);
		logTagSetModel(appModel);
	}

	private static void logFocusModel( MamutAppModel appModel )
	{
		FocusModel<Spot, Link> focusModel = appModel.getFocusModel();
		ModelGraph graph = appModel.getModel().getGraph();
		focusModel.listeners().add(() -> {
			Spot ref = graph.vertexRef();
			Spot focusedSpot = focusModel.getFocusedVertex( ref );
			log( "FocusModel: focused vertex: " + focusedSpot );
			graph.releaseRef( ref );
		});
	}

	private static void logNavigationHandle( NavigationHandler<Spot, Link> navigationHandler )
	{
		navigationHandler.listeners().add( new NavigationListener<Spot, Link>()
		{
			@Override
			public void navigateToVertex( Spot vertex )
			{
				log( "NavigationHandler: navigate to vertex " + vertex );
			}

			@Override
			public void navigateToEdge( Link edge )
			{
				log( "NavigationHandler: navigate to edge " + edge );
			}
		} );
	}

	private static void logTimePointModel( TimepointModel model )
	{
		model.listeners().add( () -> log( "Time point changed: (to " + model.getTimepoint() + ")" ) );
	}

	private static void logTagSetModel( MamutAppModel appModel )
	{
		// TODO
		Model model = appModel.getModel();
		TagSetModel<Spot, Link> tagSetModel = model.getTagSetModel();
		tagSetModel.listeners().add( () -> log( "tag set changed" ) );
	}

	private static void log( String text )
	{
		System.out.println( text + " " + Thread.currentThread().getName() );
		if( LOG_STACK_TRACE )
			for ( StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace() )
				System.out.println( "   " + stackTraceElement );
	}
}

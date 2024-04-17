package org.mastodon.blender.setup;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;

/**
 * A SciJava service that stores the paths to the Blender templates used by Mastodon.
 */
@Plugin( type = SciJavaService.class )
public class BlenderSettingsService extends AbstractService implements SciJavaService
{

	public static final URL DEFAULT_INTERACTIVE_TEMPLATE = BlenderSettingsService.class.getResource( "/blender-scripts/empty.blend" );

	public static final URL DEFAULT_CSV_TEMPLATE = BlenderSettingsService.class.getResource( "/csv/empty_with_geometry_nodes.blend" );

	@Parameter
	private PrefService prefService;

	public void setInteractiveBlenderTemplate(String template) {
		prefService.put(BlenderSettingsService.class, "interactiveBlenderTemplate", template);
	}

	public String getInteractiveBlenderTemplate() {
		return prefService.get(BlenderSettingsService.class, "interactiveBlenderTemplate", "");
	}

	public void setCsvBlenderTemplate(String template) {
		prefService.put(BlenderSettingsService.class, "csvBlenderTemplate", template);
	}

	public String getCsvBlenderTemplate() {
		return prefService.get(BlenderSettingsService.class, "csvBlenderTemplate", "");
	}

	public File getCopyOfInteractiveBlenderTemplate() throws IOException
	{
		return getTemplateCopy( DEFAULT_INTERACTIVE_TEMPLATE, getInteractiveBlenderTemplate() );
	}

	public File getCopyOfCsvBlenderTemplate() throws IOException
	{
		return getTemplateCopy( DEFAULT_CSV_TEMPLATE, getCsvBlenderTemplate() );
	}

	private static File getTemplateCopy( URL defaultResource, String interactiveTemplate ) throws IOException
	{
		File tmp = File.createTempFile( "mastodon-bender", ".blend" );
		URL source = getUrl( defaultResource, interactiveTemplate );
		FileUtils.copyURLToFile(source, tmp);
		return tmp;
	}

	private static URL getUrl( URL defaultResource, String interactiveTemplate )
	{
		if ( interactiveTemplate == null || interactiveTemplate.isEmpty() )
			return defaultResource;
		File file = new File( interactiveTemplate );
		if ( !file.exists() )
			return defaultResource;
		try
		{
			return file.toURI().toURL();
		}
		catch ( MalformedURLException e )
		{
			throw new RuntimeException( e );
		}
	}
}

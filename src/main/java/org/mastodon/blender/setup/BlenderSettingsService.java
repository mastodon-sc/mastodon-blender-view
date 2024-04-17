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
		return getTemplateCopy( "mastodon-bender", "/blender-scripts/empty.blend", getInteractiveBlenderTemplate() );
	}

	public File getCopyOfCsvBlenderTemplate() throws IOException
	{
		return getTemplateCopy( "mastodon-bender", "/csv/empty_with_geometry_nodes.blend", getCsvBlenderTemplate() );
	}

	private static File getTemplateCopy( String filename, String defaultResource, String interactiveTemplate ) throws IOException
	{
		File tmp = File.createTempFile( filename, ".blend" );
		URL source = getUrl( defaultResource, interactiveTemplate );
		FileUtils.copyURLToFile(source, tmp);
		return tmp;
	}

	private static URL getUrl( String defaultResource, String interactiveTemplate )
	{
		URL defaultTemplate = BlenderSettingsService.class.getResource( defaultResource );
		if ( interactiveTemplate == null || interactiveTemplate.isEmpty() )
			return defaultTemplate;
		File file = new File( interactiveTemplate );
		if ( !file.exists() )
			return defaultTemplate;
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

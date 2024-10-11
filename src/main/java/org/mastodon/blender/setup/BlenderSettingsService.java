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
		File tmp = File.createTempFile( "mastodon-blender", ".blend" );
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

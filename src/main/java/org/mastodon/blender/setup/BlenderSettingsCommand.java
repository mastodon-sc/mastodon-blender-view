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
import java.net.URL;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.mastodon.blender.utils.BlenderModes;
import org.mastodon.ui.util.FileChooser;
import org.scijava.Cancelable;
import org.scijava.Initializable;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;

/**
 * This dialog allows the user to configure the Blender templates used by Mastodon.
 */
@Plugin( type = Command.class, name = "Configure Blender Templates" )
public class BlenderSettingsCommand implements Command, Initializable, Cancelable
{

	private static final String DEFAULT = "<default>";

	private static final File DEFAULT_FILE = new File( DEFAULT );

	@Parameter
	private BlenderSettingsService blenderSettingsService;

	@Parameter( visibility = ItemVisibility.MESSAGE ) // Text that is displayed in the dialog and never changes.
	private String interactiveDescription =
			"Blender file that is used as empty template for the \"" + BlenderModes.LINKED_TO_MASTODON + "\":";

	@Parameter( label = "Blender File", style = "open, extensions:blend", required = false, persist = false, callback = "customInteractiveTemplateChange" )
	private File interactiveTemplate = null;

	@Parameter( label = "Reset to Default", callback = "resetInteractiveTemplate" )
	private Button resetInteractiveTemplate;

	@Parameter( label = "Save Default Template As...", callback = "saveDefaultInteractiveTemplate" )
	private Button saveDefaultInteractiveTemplate;

	@Parameter( visibility = ItemVisibility.MESSAGE ) // Text that is displayed in the dialog and never changes.
	private String csvDescription = "Blender file that is used as empty template for \"" + BlenderModes.ADVANCED_VISUALS + "\":";

	@Parameter( label = "Blender File", style = "open, extensions:blend", required = false, persist = false, callback = "customCsvTemplateChange" )
	private File csvTemplate = null;

	@Parameter( label = "Reset to Default", callback = "resetCsvTemplate" )
	private Button resetCsvTemplate;

	@Parameter( label = "Save Default Template As...", callback = "saveDefaultCsvTemplate" )
	private Button saveDefaultCsvTemplate;

	@Override
	public void initialize()
	{
		String interactiveTemplateString = blenderSettingsService.getInteractiveBlenderTemplate();
		String csvTemplateString = blenderSettingsService.getCsvBlenderTemplate();
		this.interactiveTemplate = interactiveTemplateString.isEmpty() ? DEFAULT_FILE : new File( interactiveTemplateString );
		this.csvTemplate = csvTemplateString.isEmpty() ? DEFAULT_FILE : new File( csvTemplateString );
	}

	@Override
	public void run()
	{
		blenderSettingsService.setInteractiveBlenderTemplate( extracted( interactiveTemplate ) );
		blenderSettingsService.setCsvBlenderTemplate( extracted( csvTemplate ) );
	}

	private String extracted( File template )
	{
		if ( isDefault( template ) )
			return "";
		if ( ! template.exists() )
		{
			JOptionPane.showMessageDialog( null, "Template file does not exist:\n" + template, "Error", JOptionPane.ERROR_MESSAGE );
			return "";
		}
		return template.getAbsolutePath();
	}

	public boolean isDefault( File file )
	{
		return file == null || DEFAULT.equals( file.toString() );
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

	@SuppressWarnings( "unused" )
	private void resetInteractiveTemplate()
	{
		interactiveTemplate = DEFAULT_FILE;
	}

	@SuppressWarnings( "unused" )
	private void resetCsvTemplate()
	{
		csvTemplate = DEFAULT_FILE;
	}

	@SuppressWarnings( "unused" )
	private void saveDefaultInteractiveTemplate()
	{
		saveDefaultTempate( BlenderSettingsService.DEFAULT_INTERACTIVE_TEMPLATE,
				"Save Default " + BlenderModes.LINKED_TO_MASTODON + " Template", "interactive_template.blend" );
	}

	@SuppressWarnings( "unused" )
	private void saveDefaultCsvTemplate()
	{
		saveDefaultTempate( BlenderSettingsService.DEFAULT_CSV_TEMPLATE,
				"Save Default " + BlenderModes.ADVANCED_VISUALS + " Blender Template", "geometry_nodes_template.blend" );
	}

	private static void saveDefaultTempate( URL defaultTemplate, String title, String defaultFileName )
	{
		try
		{
			Objects.requireNonNull( defaultTemplate );
			File file = FileChooser.chooseFile( null, defaultFileName, new FileNameExtensionFilter( "Blender File", "blend" ), title, FileChooser.DialogType.SAVE );
			if ( file != null )
				FileUtils.copyURLToFile( defaultTemplate, file );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}
}

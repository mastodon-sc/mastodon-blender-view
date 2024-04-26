package org.mastodon.blender.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
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
	private String interactiveDescription = "Blender file that is used as empty template for the \"Interactive Blender Window\":";

	@Parameter( label = "Blender File", style = "open, extensions:blend", required = false, persist = false, callback = "customInteractiveTemplateChange" )
	private File interactiveTemplate = null;

	@Parameter( label = "Reset to Default", callback = "resetInteractiveTemplate" )
	private Button resetInteractiveTemplate;

	@Parameter( label = "Save Default Template As...", callback = "saveDefaultInteractiveTemplate" )
	private Button saveDefaultInteractiveTemplate;

	@Parameter( visibility = ItemVisibility.MESSAGE ) // Text that is displayed in the dialog and never changes.
	private String csvDescription = "Blender file that is used as empty template for \"Geometry Nodes Blender Window\":";

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
		saveDefaultTempate( BlenderSettingsService.DEFAULT_INTERACTIVE_TEMPLATE, "Save Default Interactive Blender Template", "interactive_template.blend" );
	}

	@SuppressWarnings( "unused" )
	private void saveDefaultCsvTemplate()
	{
		saveDefaultTempate( BlenderSettingsService.DEFAULT_CSV_TEMPLATE, "Save Default Geometry Nodes Blender Template", "geometry_nodes_template.blend" );
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

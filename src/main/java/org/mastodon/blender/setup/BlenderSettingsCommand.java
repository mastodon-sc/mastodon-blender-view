package org.mastodon.blender.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.scijava.Cancelable;
import org.scijava.Initializable;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;

/**
 * This is basically a dialog that allows the user to configure the Blender
 * templates used by Mastodon.
 */
@Plugin( type = Command.class, name = "Configure Blender Templates" )
public class BlenderSettingsCommand implements Command, Initializable, Cancelable
{

	private final String DEFAULT = "<default>";

	private final File DEFAULT_FILE = new File( DEFAULT );

	@Parameter( visibility = ItemVisibility.MESSAGE )
	private String INTERACTIVE_DESCRIPTION = "Blender file that is used as empty template for interactive 3D view:";

	@Parameter
	private BlenderSettingsService blenderSettingsService;

	@Parameter( label = "Blender File", style = "file, extensions:blend", required = false, persist = false, callback = "customInteractiveTemplateChange" )
	private File interactiveTemplate = null;

	@Parameter( label = "Reset to Default", callback = "resetInteractiveTemplate" )
	private Button resetInteractiveTemplate;

	@Parameter( label = "Save Default Template As...", callback = "saveDefaultInteractiveTemplate" )
	private Button saveDefaultInteractiveTemplate;

	@Parameter( visibility = ItemVisibility.MESSAGE )
	private String CSV_DESCRIPTION = "Blender file that is used as empty template for CSV Blender view:";

	@Parameter( label = "Blender File", style = "file, extensions:blend", required = false, persist = false, callback = "customCsvTemplateChange" )
	private File csvTemplate = null;

	@Parameter( label = "Reset to Default", callback = "resetCsvTemplate" )
	private Button resetCsvTemplate;

	@Parameter( label = "Save Default Template As...", callback = "saveDefaultCsvTemplate" )
	private Button saveDefaultCsvTemplate;

	@Override
	public void initialize()
	{
		String interactiveTemplate = blenderSettingsService.getInteractiveBlenderTemplate();
		String csvTemplate = blenderSettingsService.getCsvBlenderTemplate();
		this.interactiveTemplate = interactiveTemplate.isEmpty() ? DEFAULT_FILE : new File( interactiveTemplate );
		this.csvTemplate = csvTemplate.isEmpty() ? DEFAULT_FILE : new File( csvTemplate );
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
		saveDefaultTempate( BlenderSettingsService.DEFAULT_INTERACTIVE_TEMPLATE, "Save Default Interactive Template" );
	}

	@SuppressWarnings( "unused" )
	private void saveDefaultCsvTemplate()
	{
		saveDefaultTempate( BlenderSettingsService.DEFAULT_CSV_TEMPLATE, "Save Default CSV Template" );
	}

	private static void saveDefaultTempate( URL defaultTemplate, String title )
	{
		Objects.requireNonNull( defaultTemplate );
		JFileChooser fileChooser = new JFileChooser( );
		fileChooser.setSelectedFile( new File( "template.blend" ) );
		fileChooser.setDialogTitle( title );
		fileChooser.setFileFilter( new FileNameExtensionFilter( "Blender File", "blend" ) );
		boolean ok = fileChooser.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION;
		if ( !ok )
			return;
		try
		{
			FileUtils.copyURLToFile( defaultTemplate, fileChooser.getSelectedFile() );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}
}

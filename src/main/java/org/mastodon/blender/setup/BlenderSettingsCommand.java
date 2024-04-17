package org.mastodon.blender.setup;

import java.io.File;

import javax.swing.JOptionPane;

import org.scijava.Cancelable;
import org.scijava.Initializable;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * This is basically a dialog that allows the user to configure the Blender
 * templates used by Mastodon.
 */
@Plugin( type = Command.class, name = "Configure Blender Templates" )
public class BlenderSettingsCommand implements Command, Initializable, Cancelable
{

	@Parameter
	private BlenderSettingsService blenderSettingsService;

	private static final String DEFAULT = "Use default template";

	private static final String CUSTOM = "Use custom template";

	@Parameter( label = "Interactive Blender", choices = { DEFAULT, CUSTOM }, style="radioButtonHorizontal", persist = false )
	private String useInteractiveTemple = DEFAULT;

	@Parameter( label = "Custom *.blend Template", style = "file, extensions:blend", required = false, persist = false, callback = "customInteractiveTemplateChange" )
	private File interactiveTemplate = null;

	@Parameter( label = "CSV Blender", choices = { DEFAULT, CUSTOM }, style="radioButtonHorizontal", persist = false )
	private String useCsvTemplate = DEFAULT;

	@Parameter( label = "Custom *.blend Template", style = "file, extensions:blend", required = false, persist = false, callback = "customCsvTemplateChange" )
	private File csvTemplate = null;

	@Override
	public void initialize()
	{
		String interactiveTemplate = blenderSettingsService.getInteractiveBlenderTemplate();
		String csvTemplate = blenderSettingsService.getCsvBlenderTemplate();

		useInteractiveTemple = interactiveTemplate.isEmpty() ? DEFAULT : CUSTOM;
		useCsvTemplate = csvTemplate.isEmpty() ? DEFAULT : CUSTOM;

		this.interactiveTemplate = interactiveTemplate.isEmpty() ? null : new File( interactiveTemplate );
		this.csvTemplate = csvTemplate.isEmpty() ? null : new File( csvTemplate );
	}

	@Override
	public void run()
	{
		if ( useInteractiveTemple.equals( DEFAULT ) || interactiveTemplate == null )
			blenderSettingsService.setInteractiveBlenderTemplate( "" );
		else if ( interactiveTemplate.exists() )
			blenderSettingsService.setInteractiveBlenderTemplate( interactiveTemplate.getAbsolutePath() );
		else
		{
			blenderSettingsService.setInteractiveBlenderTemplate( "" );
			JOptionPane.showMessageDialog( null, "Template file does not exist:\n" + csvTemplate, "Error", JOptionPane.ERROR_MESSAGE );
		}

		if ( useCsvTemplate.equals( DEFAULT ) || csvTemplate == null )
			blenderSettingsService.setCsvBlenderTemplate( "" );
		else if ( csvTemplate.exists() )
			blenderSettingsService.setCsvBlenderTemplate( csvTemplate.getAbsolutePath() );
		else
		{
			blenderSettingsService.setCsvBlenderTemplate( "" );
			JOptionPane.showMessageDialog( null, "Template file does not exist:\n" + csvTemplate, "Error", JOptionPane.ERROR_MESSAGE );
		}
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
	private void customInteractiveTemplateChange()
	{
		useInteractiveTemple = CUSTOM;
	}

	@SuppressWarnings( "unused" )
	private void customCsvTemplateChange()
	{
		useCsvTemplate = CUSTOM;
	}
}

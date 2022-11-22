/*-
 * #%L
 * A Mastodon plugin data allows to show the embryo in Blender.
 * %%
 * Copyright (C) 2022 Matthias Arzt
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import net.miginfocom.swing.MigLayout;
import org.mastodon.ui.util.FileChooser;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;

public class BlenderSetupView extends JDialog
{

	private final Listener listener;

	private final JTextArea pathTextArea;

	private final JButton selectPathButton;

	private final JLabel pathFeedback;

	private final JButton installAddonButton;

	private final JLabel addonFeedback;

	private final JButton testAddonButton;

	private final JLabel testFeedback;

	private final JButton finishButton;

	private final JButton cancelButton;

	private Path blenderPath;

	BlenderSetupView( Listener listener ) {

		super( ( Frame ) null, "Setup Mastodon Blender Addon", true );
		this.setModalExclusionType( Dialog.ModalExclusionType.NO_EXCLUDE );
		this.listener = listener;

		setLayout( new MigLayout("insets dialog, fill", "[][grow]") );

		final String introText = "<html><body>"
				+ "Mastodon can use Blender to visualise cell trackings in 3D. "
				+ "In order to achieve this, it is required to install "
				+ "the \"mastodon_blender_view\" addon to Blender.<br><br>"
				+ "This dialog will guide you through the installation."
				+ "</body></html>";
		add( setFonStyle( new JLabel( introText ), Font.PLAIN ), "span, wrap, grow, width 0:0:pref" );

		add( new JLabel( "1."), "gaptop unrelated" );
		add( new JLabel("Install Blender:"), "wrap, wmin 0" );
		final String blenderInstallText = "<html><body>"
				+ "The first step is to install Blender. This needs to be done manually by you.<br>"
				+ "Mastodon only supports the portable version of Blender.<br>"
				+ "Go to the official blender webpage, and:<br>"
				+ "<br>"
				+ "Please download and install the <b>portable version of Blender</b>!"
				+ "</body></html>";
		add( setFonStyle( new JLabel( blenderInstallText ), Font.PLAIN ), "skip, wrap, grow, wmin 0" );
		add( initializeLinkButton(), "skip, wrap");
		add( new JLabel( "2."), "gaptop unrelated" );
		add( new JLabel("Select the path of your Blender installation:"), "wrap, wmin 0" );
		pathTextArea = new JTextArea( 2, 50 );
		pathTextArea.setBorder( BorderFactory.createLineBorder( Color.LIGHT_GRAY ) );
		pathTextArea.setEditable( false );
		add( pathTextArea, "skip, wrap, grow, wmin 0" );
		selectPathButton = new JButton( "select" );
		selectPathButton.addActionListener( ignore -> onSelectPathClicked() );
		add( selectPathButton, "skip, wrap");
		pathFeedback = new JLabel( "" );
		setFonStyle( pathFeedback, Font.ITALIC );
		add( pathFeedback, "skip, wrap, wmin 0" );

		add( new JLabel( "3."), "gaptop unrelated");
		add( new JLabel("Install / update Blender addon"), "wrap, wmin 0" );
		installAddonButton = new JButton( "install addon" );
		installAddonButton.addActionListener( ignore -> listener.installAddonClicked() );
		add( installAddonButton, "skip, wrap");
		addonFeedback = new JLabel( "" );
		setFonStyle( addonFeedback, Font.ITALIC );
		add( addonFeedback, "skip, wrap, wmin 0" );

		add( new JLabel( "4."), "gaptop unrelated");
		add( new JLabel("Test Blender addon"), "wrap, wmin 0" );
		testAddonButton = new JButton("test addon");
		testAddonButton.addActionListener( ignore -> listener.testAddonClicked() );
		add( testAddonButton, "skip, wrap" );
		testFeedback = new JLabel( "" );
		setFonStyle( testFeedback, Font.ITALIC );
		add( testFeedback, "skip, wrap, wmin 0" );

		add( new JLabel(""), "push, wrap");

		finishButton = new JButton( "Finish" );
		finishButton.addActionListener( ignore -> listener.finishClicked() );
		add( finishButton, "span, split 2, pushx, align right" );
		cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( ignore -> listener.cancelClicked() );
		add( cancelButton );
	}

	private JButton initializeLinkButton()
	{
		JButton linkButton = new JButton( "<html><bode><a href=\"dummy\">https://blender.org/download</a></body></html>" );
		setFonStyle( linkButton, Font.PLAIN );
		linkButton.addActionListener( ignore -> onLinkClicked() );
		linkButton.setBackground( new Color( 0, true ) );
		linkButton.setBorder( BorderFactory.createEmptyBorder() );
		linkButton.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
		linkButton.setOpaque( false );
		return linkButton;
	}

	private void onLinkClicked()
	{
		try
		{
			Desktop.getDesktop().browse( URI.create( "https://blender.org/download" ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	private JComponent setFonStyle( JComponent component, int style )
	{
		Font boldFont = component.getFont();
		Font normalFont = boldFont.deriveFont( style );
		component.setFont( normalFont );
		return component;
	}

	private void onSelectPathClicked()
	{
		String selectedFile = blenderPath == null ? null : blenderPath.toString();
		FileFilter filenameFilter = new FileFilter()
		{
			@Override
			public boolean accept( File f )
			{
				if ( f.isDirectory() )
					return true;
				String name = f.getName();
				return name.equals( "blender" )
						|| name.equals( "blender-softwaregl" )
						|| name.equals( "blender.exe" )
						|| name.equals( "Blender" );
			}

			@Override
			public String getDescription()
			{
				return "Blender Binary (blender.exe etc.)";
			}
		};
		File file = FileChooser.chooseFile( this,
				selectedFile,
				filenameFilter,
				"Select Blender Installation",
				FileChooser.DialogType.LOAD,
				FileChooser.SelectionMode.FILES_AND_DIRECTORIES );
		if(file == null)
			return;
		listener.setBlenderPath( file.toPath() );
	}

	public void setBlenderPath(Path blenderBinaryPath) {
		blenderPath = blenderBinaryPath;
		pathTextArea.setText( blenderBinaryPath == null ? "" :
				blenderBinaryPath.toString() );
	}

	public void setPathFeedback(String text) {
		pathFeedback.setText( formatFeedback( text ) );
	}

	public void setAddonFeedback(String text) {
		addonFeedback.setText( formatFeedback( text ) );
	}

	public void setTestFeedback(String text) {
		testFeedback.setText( formatFeedback( text ) );
	}

	private String formatFeedback( String text )
	{
		if(text.startsWith( "ok:" ))
			return formatFeedback( "green", "✔", text );
		if(text.startsWith( "todo:" ))
			return formatFeedback( "orange", "➜", text );
		if(text.startsWith( "failed:" ))
			return formatFeedback( "red", "❌", text );
		return text;
	}

	private String formatFeedback( String color, String symbol, String text )
	{
		int index = text.indexOf( ":" );
		return "<html><body><p style=\"color:" + color + "\">"
				+ symbol + " "
				+ text.substring( index + 1 )
				+ "</html><body>";
	}

	public void setInstallAddonEnabled( boolean enabled )
	{
		installAddonButton.setEnabled( enabled );
	}

	public void setTestAddonEnabled( boolean enabled )
	{
		testAddonButton.setEnabled( enabled );
	}


	public void setFinishEnabled( boolean enableFinish )
	{
		finishButton.setEnabled( enableFinish );
	}

	public interface Listener {

		void setBlenderPath( Path blenderPath );

		void installAddonClicked();

		void finishClicked();

		void cancelClicked();

		void testAddonClicked();
	}
}

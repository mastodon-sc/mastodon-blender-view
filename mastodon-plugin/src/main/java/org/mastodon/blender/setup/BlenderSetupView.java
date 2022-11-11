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
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import net.miginfocom.swing.MigLayout;
import org.mastodon.ui.util.FileChooser;

import java.awt.Color;
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

	private final JButton testAddonButton;

	private final JLabel addonFeedback;

	private final JButton finishButton;

	private final JButton cancelButton;

	private Path blenderPath;

	BlenderSetupView( Listener listener ) {

		super( ( Frame ) null, "Setup the Mastodon Blender Addon", true );
		this.setModalExclusionType( Dialog.ModalExclusionType.NO_EXCLUDE );
		this.listener = listener;

		setLayout( new MigLayout("insets dialog, fill", "[][grow]") );

		final String intro = "Mastodon can use Blender to visualise cell trackings in 3D."
				+ "But in order to achieve this, it is required to install "
				+ "a \"mastodon_blender_view\" addon to Blender. This dialog can "
				+ "guide you through the process of installing the addon.";

		final JTextArea introTextArea = new JTextArea( intro, 3, 30 );
		introTextArea.setLineWrap( true );
		introTextArea.setWrapStyleWord( true );
		introTextArea.setBackground( getBackground() );
		introTextArea.setEditable( false );
		add( introTextArea, "span, wrap, grow, wmin 0" );

		add( new JLabel( "1."), "gaptop unrelated" );
		add( new JLabel("Install Blender:"), "wrap, wmin 0" );
		final String blenderInstallText = "<html><bode>"
				+ "The first step is installing Blender. This needs to be done manually, by you.<br>"
				+ "Download Blender from the official web page: "
		        + "<a href=\"https://blender.org/download\">https://blender.org/download</a><br>"
				+ "and install it on your computer.<br>"
				+ "<p>Please chose \"portable\" version of Blender when possible.<br>"
				+ "(The \"mastodon_blender_view\" addon only supports portable Blender installations.)<br>"
				+ "<p>Continue with the step two when your are done."
				+ "</html></body>";
		JEditorPane html = new JEditorPane( "text/html", blenderInstallText );
		html.setEditable( false );
		html.addHyperlinkListener( e -> {
			if(e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
				return;
			onHyperlinkClicked();
		} );
		add( html, "skip, wrap" );
		add( new JLabel( "2."), "gaptop unrelated" );
		add( new JLabel("Select The Path Of Your Blender Installation:"), "wrap, wmin 0" );
		pathTextArea = new JTextArea( 2, 50 );
		pathTextArea.setBorder( BorderFactory.createLineBorder( Color.LIGHT_GRAY ) );
		pathTextArea.setEditable( false );
		add( pathTextArea, "skip, wrap, grow, wmin 0" );
		selectPathButton = new JButton( "select" );
		selectPathButton.addActionListener( ignore -> onSelectPathClicked() );
		add( selectPathButton, "skip, wrap");
		pathFeedback = new JLabel( "" );
		setFontToNormal( pathFeedback );
		add( pathFeedback, "skip, wrap, wmin 0" );

		add( new JLabel( "2."), "gaptop unrelated");
		add( new JLabel("Install / Update Blender Addon"), "wrap, wmin 0" );
		installAddonButton = new JButton( "install addon" );
		installAddonButton.addActionListener( ignore -> listener.installAddonClicked() );
		testAddonButton = new JButton("test addon");
		testAddonButton.addActionListener( ignore -> listener.testAddonClicked() );
		add( installAddonButton, "skip, split 2");
		add( testAddonButton, "wrap" );
		addonFeedback = new JLabel( "" );
		setFontToNormal( addonFeedback );
		add( addonFeedback, "skip, wrap, wmin 0" );
		add( new JLabel(""), "push, wrap");

		finishButton = new JButton( "Finish" );
		finishButton.addActionListener( ignore -> listener.finishClicked() );
		add( finishButton, "span, split 2, pushx, align right" );
		cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( ignore -> listener.cancelClicked() );
		add( cancelButton );
	}

	private void onHyperlinkClicked()
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

	private void setFontToNormal( JLabel label )
	{
		Font boldFont = label.getFont();
		Font normalFont = boldFont.deriveFont( boldFont.getStyle() & ~Font.BOLD );
		label.setFont( normalFont );
	}

	private void onSelectPathClicked()
	{
		String selectedFile = blenderPath == null ? null : blenderPath.toString();
		File file = FileChooser.chooseFile( this, selectedFile, FileChooser.DialogType.LOAD );
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
		pathFeedback.setText( text );
	}

	public void setAddonFeedback(String text) {
		addonFeedback.setText( text );
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

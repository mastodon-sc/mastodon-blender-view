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
package org.mastodon.blender;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import org.mastodon.ui.util.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;

public class SetupBlenderFrame extends JFrame
{

	private enum VerificationStatus
	{
		NOTHING,
		PENDING,
		FAILED,
		IN_PROCESS,
		OK
	};

	private Path blenderPath;

	private VerificationStatus verification;

	private final JButton choosePathButton;

	private final JButton verifyBlenderButton;

	private final JButton installAddonButton;

	private SetupBlenderFrame() {
		setLayout( new MigLayout() );
		add(new JLabel("Blender Path:"), "wrap");
		choosePathButton = new JButton( "<choose>" );
		choosePathButton.addActionListener( ignore -> choosePath() );
		add( choosePathButton, "wrap, grow");
		verifyBlenderButton = new JButton( "verify blender" );
		verifyBlenderButton.addActionListener( ignore -> (( Runnable ) this::startBlender).run() );
		add( verifyBlenderButton, "wrap, grow");
		installAddonButton = new JButton( "install Mastodon Blender View" );
		installAddonButton.addActionListener( ignore -> (( Runnable ) this::installBlenderAddon ).run() );
		add( installAddonButton, "wrap, grow");
		setVerification( VerificationStatus.NOTHING );
	}

	private void choosePath()
	{
		String selectedFile = blenderPath == null ? null : blenderPath.toString();
		File file = FileChooser.chooseFile( this, selectedFile, FileChooser.DialogType.LOAD );
		if(file == null)
			return;
		setBlenderPath( file.toPath() );
	}

	private void setBlenderPath( Path blenderPath )
	{
		this.blenderPath = blenderPath;
		setVerification( VerificationStatus.PENDING );
		choosePathButton.setText( this.blenderPath.toString() );
	}

	private void startBlender()
	{
		try
		{
			setVerification( VerificationStatus.IN_PROCESS );
			boolean verified = SetupBlender.verifyBlenderBinary(blenderPath);
			setVerification( verified ? VerificationStatus.OK : VerificationStatus.FAILED);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			setVerification( VerificationStatus.FAILED );
		}
	}

	private void setVerification( VerificationStatus verification )
	{
		this.verification = verification;
		EnumSet<VerificationStatus> verifyPossible = EnumSet.of( VerificationStatus.PENDING, VerificationStatus.FAILED );
		verifyBlenderButton.setEnabled( verifyPossible.contains( verification ) );
		verifyBlenderButton.setText( verifyButtonText( verification ) );
		installAddonButton.setEnabled( verification == VerificationStatus.OK );
	}

	private static String verifyButtonText( VerificationStatus verification )
	{
		switch ( verification ) {
		case OK:
			return "verify blender (ok)";
		case FAILED:
			return "verify blender (failed)";
		default:
			return "verify blender";
		}
	}

	private void installBlenderAddon()
	{
		try
		{
			SetupBlender.installDependency( blenderPath );
			SetupBlender.copyAddon( blenderPath );
			boolean works = SetupBlender.verifyAddonWorks( blenderPath );
			if( works )
				installAddonButton.setText( "completed" );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		// * select path
		// * allow to verify if blender can be started
		// * allow to install dependencies
		// * allow to install plugin
		// * allow to verify if plugin is working
		JFrame frame = new SetupBlenderFrame();
		frame.pack();
		frame.setVisible( true );
		frame.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
	}
}

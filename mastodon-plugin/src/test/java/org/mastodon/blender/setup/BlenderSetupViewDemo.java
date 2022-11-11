package org.mastodon.blender.setup;

import javax.swing.WindowConstants;

import java.nio.file.Path;

public class BlenderSetupViewDemo
{
	public static void main(String... args) {
		BlenderSetupView frame = new BlenderSetupView( new BlenderSetupView.Listener()
		{
			@Override
			public void setBlenderPath( Path blenderPath ) { }

			@Override
			public void installAddonClicked() { }

			@Override
			public void finishClicked() { }

			@Override
			public void cancelClicked() { }

			@Override
			public void testAddonClicked() { }
		} );
		frame.pack();
		frame.setVisible( true );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
	}
}

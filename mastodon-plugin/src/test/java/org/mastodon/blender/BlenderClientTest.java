package org.mastodon.blender;

import org.scijava.Context;

public class BlenderClientTest
{
	public static void main(String... args) {
		try (Context context = new Context())
		{
			BlenderClient client = new BlenderClient( context );
			client.setTimepoint(22);
		}
	}
}

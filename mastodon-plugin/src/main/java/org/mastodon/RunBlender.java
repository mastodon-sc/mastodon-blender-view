package org.mastodon;

import java.io.IOException;

public class RunBlender
{
	public static void main( String... args )
			throws Exception
	{
		start_blender();
		Thread.sleep( 2000 );
		HelloWorldClient.main();
	}

	private static void start_blender() throws IOException
	{
		new ProcessBuilder( "bash", "blender-addon/start_blender.sh" )
				.redirectOutput( ProcessBuilder.Redirect.INHERIT)
				.redirectError( ProcessBuilder.Redirect.INHERIT)
				.start();
	}
}

package org.mastodon.blender;

public class StartBlenderException extends RuntimeException
{
	public StartBlenderException( Throwable throwable )
	{
		super( "Failed to start Blender.", throwable );
	}
}

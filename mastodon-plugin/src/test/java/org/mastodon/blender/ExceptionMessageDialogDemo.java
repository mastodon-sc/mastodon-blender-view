package org.mastodon.blender;

public class ExceptionMessageDialogDemo
{
	public static void main(String... args) {
		Exception e = getSomeExceptions();
		ExceptionDialog.show( null, "Some Error Occured", "This is what went wrong", e );
	}

	private static Exception getSomeExceptions()
	{
		try
		{
			throw new RuntimeException( "Hello World" );
		}
		catch (Exception e) {
			return e;
		}
	}
}

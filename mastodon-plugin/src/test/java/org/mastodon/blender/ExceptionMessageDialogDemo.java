package org.mastodon.blender;

public class ExceptionMessageDialogDemo
{
	public static void main(String... args) {
		Exception e = getSomeExceptions();
		System.out.println(ExceptionDialog.showOkCancelDialog( null, "Some Error Occured", "This is what went wrong", e, "Hello", "Cancel" ));
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

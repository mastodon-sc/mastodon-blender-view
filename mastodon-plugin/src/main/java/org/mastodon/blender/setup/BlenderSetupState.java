package org.mastodon.blender.setup;

enum BlenderSetupState
{

	PATH_NOT_SET(
		 "please select",
		"",
		false,
		false,
		false
	),

	WRONG_PATH_DIRECTORY(
		 "This is a directory, please select the executable Blender file",
		"",
		false,
		false,
		false
	),

	WRONG_PATH_NOT_FOUND(
		 "file doesn't exits",
		"",
		false,
		false,
		false
	),

	WRONG_PATH(
		 "doesn't seem to be a correct blender executable",
		"",
		false,
		false,
		false
	),

	BLENDER_INSTALLED(
		 "looks good",
		"Please install addon!",
		true,
		false,
		false
	),

	INSTALLING_ADDON(
		 "looks good",
		"Installing addon ...",
		true,
		false,
		false
	),

	ADDON_INSTALLED(
		 "looks good",
		"Addon is installed. Please test it.",
		true,
		true,
		false
	),

	TESTING_ADDON(
		 "looks good",
		"Testing addon ...",
		true,
		false,
		false
	),

	TEST_FAILED(
		 "looks good",
		"Something went wrong. Maybe try to install the addon again!?",
		true,
		true,
		false
	),

	TEST_SUCCEEDED(
		 "looks good",
		"Great! The addon works as expected. Click \"Finish\" the complete the setup.",
		true,
		true,
		true
	);

	public final boolean enableInstall;

	public final boolean enableTest;

	public final boolean enableFinish;

	public final String pathFeedback;

	public final String addonFeedback;

	BlenderSetupState( String pathFeedback, String addonFeedback, boolean enableInstall, boolean enableTest, boolean enableFinish )
	{
		this.enableInstall = enableInstall;
		this.pathFeedback = pathFeedback;
		this.addonFeedback = addonFeedback;
		this.enableTest = enableTest;
		this.enableFinish = enableFinish;
	}

}

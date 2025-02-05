# Release

* [ ] Merge Open Pull Requests
* [ ] Update license information
    * [ ] Run "mvn license:update-file-header" and commit
* [ ] Optional: Copy SNAPSHOT to Fiji with active Mastodon-Dev update site and check menus
* [ ] Create jar file locally with mvn clean package (compile with Java 8)
    * [ ] Set version number in pom.xml to release version before creating jar file
* [ ] Run release Script (de-snapshots, sets a tag and sets the next snapshot version, generates javadoc, runs unit
  tests)
    * [x] Check, if github action is installed in this repo, which copies the release to maven.scijava.org
    * [ ] For reference: Release script https://github.com/scijava/scijava-scripts/blob/main/release-version.sh
    * [ ] Clone https://github.com/scijava/scijava-scripts repo
    * [ ] Ensure that one of the git remotes has the name "origin"
    * [ ] Close IntelliJ, if open (to avoid conflicts)
    * [ ] Run sh /path/to/release-version.sh from the mastodon-blender-view root directory
    * [ ] Confirm version number
    * [ ] The release script pushes to master on github.com
* [ ] Copy Jar-File to local Fiji installation
    * [ ] Delete jar-file from last release version from local Fiji installation path
    * [ ] Copy jar file of the new version to local Fiji installation path
    * [ ] Test, if Fiji starts successfully
    * [ ] Test new functionalities of released version in Fiji
* [ ] Copy Jar-File to Mastodon-Tomancak Update-Site using Fiji/ImageJ Updaters (Fiji > Help > Update...)
    * [ ] Set Updater to Advanced Mode
        * [ ] If needed, add `webdav:username_for_update_site` as `Host` under `Manage Update Sites > Mastodon-Tomancak`
    * [ ] Upload mastodon-blender-view-release-version.jar
    * [ ] Check Upload success: https://sites.imagej.net/Mastodon-Tomancak/jars/

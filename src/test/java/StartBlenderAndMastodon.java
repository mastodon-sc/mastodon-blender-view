/*-
 * #%L
 * A Mastodon plugin data allows to show the embryo in Blender.
 * %%
 * Copyright (C) 2022 - 2024 Matthias Arzt
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.mastodon.blender.BlenderController;
import org.mastodon.blender.MastodonUtils;
import org.mastodon.mamut.ProjectModel;

public class StartBlenderAndMastodon
{
	public static void main( String... args ) throws IOException
	{
		copyAddonFromRepoToBlender();
		ProjectModel projectModel = MastodonUtils.showGui( "/home/arzt/Datasets/DeepLineage/Johannes/2022-07-27_NL46xNL22_eachview_2023-07-04_cleaned.mastodon" );
		new BlenderController( projectModel );
	}

	private static void copyAddonFromRepoToBlender() throws IOException
	{
		List<String> files = Arrays.asList("__init__.py", "mb_scene.py", "mb_panel.py", "mb_server.py", "mb_utils.py");
		for ( String filename : files )
			copyFromIdeToBlender( filename );
	}

	private static void copyFromIdeToBlender( String filename ) throws IOException
	{
		Path repo = Paths.get( "/home/arzt/devel/mastodon-blender-view/blender-addon/" );
		Path addonFolder = Paths.get( "/home/arzt/.config/blender/3.3/scripts/addons/mastodon_blender_view/" );
		Files.copy( repo.resolve( filename ),
				addonFolder.resolve( filename ),
				StandardCopyOption.REPLACE_EXISTING );
	}
}

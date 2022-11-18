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
package org.mastodon.blender.setup;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore
public class BlenderSetupUtilsTest
{
	// TODO, improve tests

	private final Path blenderBinaryPath = Paths.get("/home/arzt/Applications/blender-3.3.1-linux-x64/blender");

	@Test
	public void testInstallDependencies()
			throws IOException, InterruptedException
	{
		BlenderSetupUtils.installDependency( blenderBinaryPath );
	}

	@Test
	public void testFindAddonsFolder() {
		Path addonsPath = BlenderSetupUtils.findMyAddonFolder(blenderBinaryPath);
		assertEquals(blenderBinaryPath.getParent().resolve( "3.3/scripts/addons/mastodon_blender_view" ), addonsPath);
	}

	@Test
	public void testInstallAddon() throws IOException
	{
		BlenderSetupUtils.uninstallAddon(blenderBinaryPath);
		assertFalse( BlenderSetupUtils.isMastodonAddonInstalled( blenderBinaryPath ) );
		BlenderSetupUtils.installAddon(blenderBinaryPath);
		assertTrue( BlenderSetupUtils.isMastodonAddonInstalled( blenderBinaryPath ) );
	}

	@Test
	public void testAddon()
			throws IOException, InterruptedException
	{
		BlenderSetupUtils.installAddon( blenderBinaryPath );
		BlenderSetupUtils.runAddonTest( blenderBinaryPath );
	}
}
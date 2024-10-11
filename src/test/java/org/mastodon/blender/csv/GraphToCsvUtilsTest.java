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
package org.mastodon.blender.csv;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.type.numeric.real.FloatType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagHelper;
import org.mastodon.util.TagSetUtils;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import ij.ImagePlus;

public class GraphToCsvUtilsTest
{
	@Test
	public void testWriteCsv() throws IOException
	{
		// setup
		ProjectModel projectModel = initializeExampleProjectModel();
		File csvFile = File.createTempFile( "mastodon-graph", ".csv" );
		// process
		GraphToCsvUtils.writeCsv( projectModel, csvFile.getAbsolutePath() );
		// test
		String content = FileUtils.readFileToString( csvFile, StandardCharsets.UTF_8 );
		String newLine = System.lineSeparator();
		String expected =
				"id, label, timepoint, x, y, z, radius, parent_id, \"tagset, with comma, and \"\"double quotes\"\"\", \"Number of links\""
						+ newLine
						+ "0, \"spotA\", 0, 1.0, 2.0, 3.0, 1.0, , #AABBCC, #352A87" + newLine
						+ "1, \"spot,B\", 1, 3.0, 3.0, 4.0, 2.0, , #112233, #352A87" + newLine;
		assertEquals( expected, content );
	}

	private static ProjectModel initializeExampleProjectModel() throws IOException
	{
		Model model = new Model();
		Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
		File mastodonFile = File.createTempFile( "test", ".mastodon" );
		try (Context context = new Context())
		{
			ProjectModel projectModel = wrapAsAppModel( image, model, context, mastodonFile );
			TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, "tagset, with comma, and \"double quotes\"",
					Arrays.asList(
							Pair.of( "A", 0xaabbcc ),
							Pair.of( "B", 0x112233 )
					) );
			TagHelper a = new TagHelper( model, tagSet, "A" );
			TagHelper b = new TagHelper( model, tagSet, "B" );
			ModelGraph graph = model.getGraph();
			Spot spotA = graph.addVertex().init( 0, new double[] { 1, 2, 3 }, 1 );
			Spot spotB = graph.addVertex().init( 1, new double[] { 3, 3, 4 }, 2 );
			spotA.setLabel( "spotA" );
			spotB.setLabel( "spot,B" );
			a.tagSpot( spotA );
			b.tagSpot( spotB );
			return projectModel;
		}
	}

	// TODO: can be removed after https://github.com/mastodon-sc/mastodon/pull/325/ is merged and beta-31 is released
	private static ProjectModel wrapAsAppModel( final Img< FloatType > image, final Model model, final Context context, final File file )
			throws IOException
	{
		final SharedBigDataViewerData sharedBigDataViewerData = asSharedBdvDataXyz( image );
		MamutProject mamutProject = new MamutProject( file );
		File datasetXmlFile = File.createTempFile( "test", ".xml" );
		mamutProject.setDatasetXmlFile( datasetXmlFile );
		return ProjectModel.create( context, model, sharedBigDataViewerData, mamutProject );
	}

	// TODO: can be removed after https://github.com/mastodon-sc/mastodon/pull/325/ is merged and beta-31 is released
	private static SharedBigDataViewerData asSharedBdvDataXyz( final Img< FloatType > image1 )
	{
		final ImagePlus image =
				ImgToVirtualStack.wrap( new ImgPlus<>( image1, "image", new AxisType[] { Axes.X, Axes.Y, Axes.Z, Axes.TIME } ) );
		return Objects.requireNonNull( SharedBigDataViewerData.fromImagePlus( image ) );
	}
}

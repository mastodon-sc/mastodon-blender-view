package org.mastodon.blender.csv;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagHelper;
import org.mastodon.util.TagSetUtils;

public class GraphToCsvUtilsTest
{
	@Test
	public void testWriteCsv() throws IOException
	{
		// setup
		Model model = initializeExampleModel();
		File csvFile = File.createTempFile( "mastodon-graph", ".csv" );
		// process
		GraphToCsvUtils.writeCsv( model, csvFile.getAbsolutePath() );
		// test
		String content = FileUtils.readFileToString( csvFile, StandardCharsets.UTF_8 );
		String newLine = System.lineSeparator();
		String expected = "id, label, timepoint, x, y, z, radius, parent_id, \"tagset, with comma, and \"\"double quotes\"\"\"" + newLine
				+ "0, \"spotA\", 0, 1.0, 2.0, 3.0, 1.0, , #AABBCC" + newLine
				+ "1, \"spot,B\", 1, 3.0, 3.0, 4.0, 2.0, , #112233" + newLine;
		assertEquals( expected, content );
	}

	private static Model initializeExampleModel()
	{
		Model model = new Model();
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, "tagset, with comma, and \"double quotes\"", Arrays.asList(
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
		return model;
	}
}

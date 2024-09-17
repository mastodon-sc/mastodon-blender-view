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

import org.mastodon.graph.ref.IncomingEdges;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Utility class to export a {@link Model} to a CSV file.
 */
public class GraphToCsvUtils
{
	/**
	 * Write the specified {@link Model} to the specified CSV file.
	 */
	public static void writeCsv( ProjectModel projectModel, String filename )
	{
		Model model = projectModel.getModel();
		ModelGraph graph = model.getGraph();
		ReentrantReadWriteLock.ReadLock lock = graph.getLock().readLock();
		lock.lock();
		try (BufferedWriter writer = new BufferedWriter( new FileWriter( filename ) ))
		{
			List< ColorFunction > colorFunctions = getColorFunctions( projectModel );
			writeHeader( writer, colorFunctions );
			Spot ref = graph.vertexRef();
			for ( Spot spot : graph.vertices() )
				writeSpot( writer, spot, colorFunctions, ref );
			graph.releaseRef( ref );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
		finally
		{
			lock.unlock();
		}
	}

	private static void writeHeader( BufferedWriter writer, List< ColorFunction > colorFunctions )
			throws IOException
	{
		writer.write( "id, label, timepoint, x, y, z, radius, parent_id" );
		for ( ColorFunction colorFunction : colorFunctions )
			writer.write( ", " + encodeString( colorFunction.toString() ) );
		writer.newLine();
		writer.flush();
	}

	private static void writeSpot( BufferedWriter writer, Spot spot, List< ColorFunction > colorFunctions, Spot ref ) throws IOException
	{
		writer.write( spot.getInternalPoolIndex() + ", " );
		writer.write( encodeString( spot.getLabel() ) + ", " );
		writer.write( spot.getTimepoint() + ", " );
		writer.write( spot.getDoublePosition( 0 ) + ", " );
		writer.write( spot.getDoublePosition( 1 ) + ", " );
		writer.write( spot.getDoublePosition( 2 ) + ", " );
		writer.write( Math.sqrt( spot.getBoundingSphereRadiusSquared() ) + ", " );
		writer.write( getParentId( spot, ref ) );
		writeColors( writer, spot, colorFunctions );
		writer.newLine();
		writer.flush();
	}

	private static String encodeString( String value )
	{
		return "\"" + value.replace( "\"", "\"\"" ) + "\"";
	}

	private static void writeColors( BufferedWriter writer, Spot spot, List< ColorFunction > colorFunctions ) throws IOException
	{
		for ( ColorFunction colorFunction : colorFunctions )
			writer.write( ", " + colorFunction.colorAsString( spot ) );
	}

	private static String getParentId( Spot spot, Spot ref )
	{
		IncomingEdges< Link > links = spot.incomingEdges();
		if ( links.isEmpty() )
			return "";
		return "" + links.iterator().next().getSource( ref ).getInternalPoolIndex();
	}


	static ColoringModelMain< Spot, Link, BranchSpot, BranchLink > createColoringModel( ProjectModel projectModel )
	{
		final FeatureColorModeManager featureColorModeManager = projectModel.getWindowManager().getManager( FeatureColorModeManager.class );
		final Model model = projectModel.getModel();
		return new ColoringModelMain<>( model.getTagSetModel(), featureColorModeManager, model.getFeatureModel(),
				model.getBranchGraph() );
	}

	static List< ColorFunction > getColorFunctions( ProjectModel projectModel )
	{
		List< ColorFunction > colorFunctions = getTagSetColorFunctions( projectModel );
		colorFunctions.addAll( getFeatureColorFunctions( projectModel ) );
		return colorFunctions;
	}

	static List< ColorFunction > getTagSetColorFunctions( ProjectModel projectModel )
	{
		TagSetModel< Spot, Link > tagSetModel = projectModel.getModel().getTagSetModel();
		return tagSetModel.getTagSetStructure().getTagSets().stream()
				.map( tagSet -> new TagSetColorFunction( tagSet, tagSetModel.getVertexTags().tags( tagSet ) ) )
				.collect( Collectors.toList() );

	}

	static List< ColorFunction > getFeatureColorFunctions( ProjectModel projectModel )
	{
		ColoringModelMain< Spot, Link, BranchSpot, BranchLink > coloringModel = createColoringModel( projectModel );
		FeatureColorModeManager colorModeManager = coloringModel.getFeatureColorModeManager();
		List< FeatureColorMode > colorModes = new ArrayList<>();
		colorModes.addAll( colorModeManager.getUserStyles() );
		colorModes.addAll( colorModeManager.getBuiltinStyles() );
		return colorModes.stream().filter( coloringModel::isValid )
				.map( colorMode -> new FeatureColorFunction( coloringModel, colorMode ) )
				.collect( Collectors.toList() );
	}
}

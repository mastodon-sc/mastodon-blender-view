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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.mastodon.graph.ref.IncomingEdges;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure;

/**
 * Utility class to export a {@link Model} to a CSV file.
 */
public class GraphToCsvUtils
{
	/**
	 * Write the specified {@link Model} to the specified CSV file.
	 */
	public static void writeCsv( Model model, String filename )
	{
		ModelGraph graph = model.getGraph();
		ReentrantReadWriteLock.ReadLock lock = graph.getLock().readLock();
		lock.lock();
		try (BufferedWriter writer = new BufferedWriter( new FileWriter( filename ) ))
		{
			List< TagSetStructure.TagSet > tagSets = model.getTagSetModel().getTagSetStructure().getTagSets();
			writeHeader( writer, tagSets );
			List< ObjTagMap< Spot, TagSetStructure.Tag > > tagMaps = tagSets.stream().map( model.getTagSetModel().getVertexTags()::tags ).collect( Collectors.toList() );
			Spot ref = graph.vertexRef();
			for ( Spot spot : graph.vertices() )
				writeSpot( writer, spot, tagMaps, ref );
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

	private static void writeHeader( BufferedWriter writer, List< TagSetStructure.TagSet > tagSets ) throws IOException
	{
		writer.write( "id, label, timepoint, x, y, z, radius, parent_id" );
		for ( TagSetStructure.TagSet tagSet : tagSets )
			writer.write( ", " + encodeString( tagSet.getName() ) );
		writer.newLine();
		writer.flush();
	}

	private static void writeSpot( BufferedWriter writer, Spot spot, List< ObjTagMap< Spot, TagSetStructure.Tag > > tagMaps, Spot ref ) throws IOException
	{
		writer.write( spot.getInternalPoolIndex() + ", " );
		writer.write( encodeString( spot.getLabel() ) + ", " );
		writer.write( spot.getTimepoint() + ", " );
		writer.write( spot.getDoublePosition( 0 ) + ", " );
		writer.write( spot.getDoublePosition( 1 ) + ", " );
		writer.write( spot.getDoublePosition( 2 ) + ", " );
		writer.write( Math.sqrt( spot.getBoundingSphereRadiusSquared() ) + ", " );
		writer.write( getParentId( spot, ref ) );
		writeColors( writer, spot, tagMaps );
		writer.newLine();
		writer.flush();
	}

	private static String encodeString( String value )
	{
		return "\"" + value.replace( "\"", "\"\"" ) + "\"";
	}

	private static void writeColors( BufferedWriter writer, Spot spot, List< ObjTagMap< Spot, TagSetStructure.Tag > > tagMaps ) throws IOException
	{
		for ( ObjTagMap< Spot, TagSetStructure.Tag > tagMap : tagMaps )
			writer.write( ", " + colorAsString( tagMap.get( spot ) ) );
	}

	private static String getParentId( Spot spot, Spot ref )
	{
		IncomingEdges< Link > links = spot.incomingEdges();
		if ( links.isEmpty() )
			return "";
		return "" + links.iterator().next().getSource( ref ).getInternalPoolIndex();
	}

	private static String colorAsString( TagSetStructure.Tag tag )
	{
		if ( tag == null )
			return "";
		return String.format( "#%06X", ( 0xffffff & tag.color() ) );
	}
}

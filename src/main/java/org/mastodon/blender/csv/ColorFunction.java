package org.mastodon.blender.csv;

import org.mastodon.mamut.model.Spot;

import java.util.function.Function;

interface ColorFunction extends Function< Spot, Integer >
{
	enum Group
	{
		TAG_SET,
		FEATURE_COLOR_MODE
	}

	Group getGroup();

	default String colorAsString( Spot spot )
	{
		Integer color = apply( spot );
		return color == null ? "" : String.format( "#%06X", ( 0xffffff & color ) );
	}
}

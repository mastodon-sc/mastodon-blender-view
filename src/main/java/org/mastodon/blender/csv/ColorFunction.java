package org.mastodon.blender.csv;

import java.util.function.Function;

import org.mastodon.mamut.model.Spot;

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

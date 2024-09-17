package org.mastodon.blender.csv;

import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure;

class TagSetColorFunction implements ColorFunction
{
	private final TagSetStructure.TagSet tagSet;

	private final ObjTagMap< Spot, TagSetStructure.Tag > tagMap;

	TagSetColorFunction( TagSetStructure.TagSet tagSet, ObjTagMap< Spot, TagSetStructure.Tag > tagMap )
	{
		this.tagSet = tagSet;
		this.tagMap = tagMap;
	}

	@Override
	public String toString()
	{
		return tagSet.getName();
	}

	@Override
	public Group getGroup()
	{
		return Group.TAG_SET;
	}

	@Override
	public Integer apply( final Spot spot )
	{
		TagSetStructure.Tag tag = tagMap.get( spot );
		return tag == null ? null : tag.color();
	}
}

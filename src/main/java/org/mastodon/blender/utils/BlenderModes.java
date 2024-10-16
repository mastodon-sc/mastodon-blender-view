package org.mastodon.blender.utils;

public enum BlenderModes
{
	ADVANCED_VISUALS( "Advanced Visuals" ),
	LINKED_TO_MASTODON( "Linked to Mastodon" );

	private final String label;

	BlenderModes( final String label )
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}

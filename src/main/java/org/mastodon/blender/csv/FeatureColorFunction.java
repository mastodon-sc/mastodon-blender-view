package org.mastodon.blender.csv;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.coloring.feature.FeatureColorMode;

class FeatureColorFunction implements ColorFunction
{
	private final GraphColorGenerator< Spot, Link > colorGenerator;

	private final String name;

	FeatureColorFunction( ColoringModelMain< Spot, Link, BranchSpot, BranchLink > coloringModel,
			FeatureColorMode featureColorMode )
	{
		coloringModel.colorByFeature( featureColorMode );
		colorGenerator = coloringModel.getFeatureGraphColorGenerator();
		name = featureColorMode.getName();
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public Group getGroup()
	{
		return Group.FEATURE_COLOR_MODE;
	}

	@Override
	public Integer apply( final Spot spot )
	{
		return colorGenerator.color( spot );
	}
}

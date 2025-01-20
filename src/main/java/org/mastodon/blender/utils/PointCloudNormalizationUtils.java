/*-
 * #%L
 * A Mastodon plugin data allows to show the embryo in Blender.
 * %%
 * Copyright (C) 2022 - 2025 Matthias Arzt
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
package org.mastodon.blender.utils;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.mastodon.mamut.model.Spot;

import java.util.Collection;

public class PointCloudNormalizationUtils
{
	public static AffineTransform3D getNormalizingTransform( Collection<Spot> spots )
	{
		RealLocalizable mean = getMean( spots );
		double variance = getVariance( spots, mean );

		double s = 1 / Math.sqrt( variance );
		AffineTransform3D transform = new AffineTransform3D();
		transform.translate( - mean.getDoublePosition( 0 ),
				- mean.getDoublePosition( 1 ),
				- mean.getDoublePosition( 2 ) );
		transform.scale( s );
		return transform;
	}

	private static RealLocalizable getMean( Collection<Spot> spots )
	{
		double x = 0;
		double y = 0;
		double z = 0;
		for ( Spot spot : spots )
		{
			x += spot.getDoublePosition( 0 );
			y += spot.getDoublePosition( 1 );
			z += spot.getDoublePosition( 2 );
		}
		int n = spots.size();
		return RealPoint.wrap( new double[] { x / n, y / n, z / n } );
	}

	private static double getVariance( Collection<Spot> spots, RealLocalizable mean )
	{
		double variance = 0;
		for ( Spot spot : spots )
		{
			variance += sqr( spot.getDoublePosition( 0 ) - mean.getDoublePosition( 0 ) );
			variance += sqr( spot.getDoublePosition( 1 ) - mean.getDoublePosition( 1 ) );
			variance += sqr( spot.getDoublePosition( 2 ) - mean.getDoublePosition( 2 ) );
		}
		variance /= ( spots.size() - 1 );
		return variance;
	}

	private static double sqr( double v )
	{
		return v * v;
	}
}

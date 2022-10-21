package org.mastodon;

import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class BranchGraphUtils
{
	private BranchGraphUtils() {
		// prevent instantiation
	}

	static Spot getBranchStart( Spot spot, Spot ref )
	{
		Spot s = spot;
		while(s.incomingEdges().size() == 1)
		{
			Link edge = s.incomingEdges().iterator().next();
			s = edge.getSource( ref );
			if(s.outgoingEdges().size() != 1)
				return edge.getTarget( ref );
		}
		return s;
	}

	private static Spot getBranchEnd( Spot spot, Spot ref )
	{
		Spot s = spot;
		while(s.outgoingEdges().size() == 1)
		{
			Link edge = s.outgoingEdges().iterator().next();
			s = edge.getTarget( ref );
			if(s.incomingEdges().size() != 1)
				return edge.getSource( ref );
		}
		return s;
	}

	static Spot findVertexForTimePoint( Spot branchStart, int timePoint, Spot ref )
	{
		Spot spot = branchStart;
		if(spot.getTimepoint() >= timePoint)
			return spot;
		while(spot.outgoingEdges().size() == 1) {
			spot = spot.outgoingEdges().iterator().next().getTarget(ref);
			if(spot.getTimepoint() >= timePoint)
				return spot;
		}
		return spot;
	}

	static Pair<RefList<Spot>, RefList<Link>> getBranchSpotsAndLinks( ModelGraph graph, Spot branchStart )
	{
		RefList<Link> links = new RefArrayList<>( graph.edges().getRefPool() );
		RefList<Spot> spots = new RefArrayList<>( graph.vertices().getRefPool() );
		spots.add( branchStart );
		Spot ref = graph.vertexRef();
		Spot spot = branchStart;
		while(spot.outgoingEdges().size() == 1) {
			Link link = spot.outgoingEdges().iterator().next();
			spot = link.getTarget( ref );
			if(spot.incomingEdges().size() != 1)
				break;
			links.add( link );
			spots.add( spot );
		}
		graph.releaseRef( ref );
		return new ValuePair<>( spots, links );
	}

	static RefSet<Spot> getAllBranchStarts( ModelGraph graph )
	{
		Spot ref = graph.vertexRef();
		try {
			RefSet<Spot> set = new RefSetImp<>( graph.vertices().getRefPool() );
			for ( Spot spot : graph.vertices() )
			{
				if ( spot.incomingEdges().size() != 1 )
					set.add( spot );
				if ( spot.outgoingEdges().size() > 1 )
					for ( Link link : spot.outgoingEdges() )
						set.add(link.getTarget( ref ) );
			}
			return set;
		}
		finally {
			graph.releaseRef( ref );
		}
	}
}

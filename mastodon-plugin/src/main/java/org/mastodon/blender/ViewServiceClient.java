/*-
 * #%L
 * A Mastodon plugin data allows to show the embryo in Blender.
 * %%
 * Copyright (C) 2022 - 2023 Matthias Arzt
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
package org.mastodon.blender;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import org.mastodon.AddSpotRequest;
import org.mastodon.ChangeMessage;
import org.mastodon.Empty;
import org.mastodon.SetActiveSpotRequest;
import org.mastodon.SetSelectionRequest;
import org.mastodon.SetSpotColorsRequest;
import org.mastodon.SetSpotPositionRequest;
import org.mastodon.SetTagSetListRequest;
import org.mastodon.SetTimePointRequest;
import org.mastodon.ViewServiceGrpc;
import org.mastodon.blender.setup.StartBlender;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure;
import org.scijava.Context;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;

public class ViewServiceClient
{

	public static final String URL = "localhost:";

	private final ViewServiceGrpc.ViewServiceBlockingStub blockingStub;

	private final ViewServiceGrpc.ViewServiceStub nonBlockingStub;

	private final Listener listener;

	public static void waitForConnection( int port )
	{
		ManagedChannel channel = ManagedChannelBuilder.forTarget( URL + port ).usePlaintext().build();
		try
		{
			String version = ViewServiceGrpc
					.newBlockingStub( channel )
					.withWaitForReady()
					.withDeadlineAfter( 10, TimeUnit.SECONDS )
					.getVersion( Empty.newBuilder().build() )
					.getVersion();
			if ( !version.equals( "0.1.0" ) )
				throw new RuntimeException( "Version of Mastodon plugin does not match." );
		}
		finally
		{
			channel.shutdown();
		}
	}

	public static void closeBlender( int port )
	{
		ManagedChannel channel = ManagedChannelBuilder.forTarget( URL + port ).usePlaintext().build();
		try
		{
			ViewServiceGrpc
					.newBlockingStub( channel )
					.closeAll( Empty.newBuilder().build() );
		}
		finally
		{
			channel.shutdown();
		}
	}

	public ViewServiceClient( final Context context, final Listener listener )
	{
		this.listener = listener;
		int port = StartBlender.getFreePort();
		try {
			StartBlender.startBlender( context, port );
		}
		catch( Throwable throwable ) {
			throw new StartBlenderException(throwable);
		}
		ManagedChannel channel = ManagedChannelBuilder.forTarget( URL + port ).usePlaintext().build();
		Runtime.getRuntime().addShutdownHook( new Thread( channel::shutdown ) );
		blockingStub = ViewServiceGrpc.newBlockingStub( channel );
		nonBlockingStub = ViewServiceGrpc.newStub( channel );
	}

	// getters

	public int receiveSyncGroupIndex()
	{
		return blockingStub.getSelectedSyncGroup( Empty.newBuilder().build() ).getIndex();
	}

	public int receiveTimepoint()
	{
		return blockingStub.getTimePoint( Empty.newBuilder().build() ).getTimePoint();
	}

	public int receiveActiveSpotId()
	{
		return blockingStub.getActiveSpot( Empty.newBuilder().build() ).getId();
	}

	public TagSetStructure.TagSet receiveTagSet(TagSetStructure tagSetStructure)
	{
		int index = blockingStub.getSelectedTagSet( Empty.newBuilder().build() ).getIndex();
		List<TagSetStructure.TagSet> tagSets = tagSetStructure.getTagSets();
		return index >= 0 && index < tagSets.size() ? tagSets.get( index ) : null;
	}

	// setters

	public void sendActiveSpotId( int id )
	{
		blockingStub.setActiveSpot( SetActiveSpotRequest.newBuilder().setId( id ).build() );
	}

	public void sendTagSetList( List<TagSetStructure.TagSet> tagSetList )
	{
		SetTagSetListRequest.Builder request = SetTagSetListRequest.newBuilder();
		for(TagSetStructure.TagSet tagSet : tagSetList )
			request.addTagSetNames(tagSet.getName());
		blockingStub.setTagSetList(request.build());
	}

	public void sendTimepoint( int timePoint )
	{
		//System.out.println("Mastodon -> Blender: set time point to " + timePoint);
		blockingStub.setTimePoint( SetTimePointRequest.newBuilder()
				.setTimepoint(timePoint)
				.build());
	}

	public void sendCoordinates( ModelGraph graph )
	{
		AffineTransform3D transform = PointCloudNormalizationUtils.getNormalizingTransform( graph.vertices() );
		RealPoint point = new RealPoint( 3 );
		for ( Spot spot : BranchGraphUtils.getAllBranchStarts( graph ) )
		{
			AddSpotRequest.Builder request = AddSpotRequest.newBuilder();
			request.setId( spot.getInternalPoolIndex() );
			request.setLabel( spot.getLabel() );
			transform.apply( spot, point );
			request.addCoordinates( point.getFloatPosition( 0 ) );
			request.addCoordinates( point.getFloatPosition( 1 ) );
			request.addCoordinates( point.getFloatPosition( 2 ) );
			blockingStub.addSpot( request.build() );
		}
	}

	public void sendColors( ModelGraph graph, ToIntFunction<Spot> spotToColor )
	{
		SetSpotColorsRequest.Builder request = SetSpotColorsRequest.newBuilder();
		RefSet<Spot> trackletStarts = BranchGraphUtils.getAllBranchStarts( graph );
		for ( Spot spot : trackletStarts ) {
			request.addIds( spot.getInternalPoolIndex() );
			request.addColors( spotToColor.applyAsInt( spot ) );
		}
		blockingStub.setSpotColors( request.build() );
	}

	// callback

	public void subscribeToChangeEvents()
	{

		nonBlockingStub.subscribeToChange( Empty.newBuilder().build(), new StreamObserver<ChangeMessage>()
		{
			@Override
			public void onNext( ChangeMessage changeMessage )
			{
				processChangeMessage( changeMessage );
			}

			@Override
			public void onError( Throwable throwable )
			{
				if( isUnavailableException( throwable ) )
				{
					System.out.println( "Connection to Blender is lost." );
					listener.onConnectionLost();
				}
				else
					throwable.printStackTrace();
			}

			private boolean isUnavailableException( Throwable throwable )
			{
				return throwable instanceof StatusRuntimeException
						&& Status.Code.UNAVAILABLE == ( ( StatusRuntimeException ) throwable ).getStatus().getCode();
			}

			@Override
			public void onCompleted()
			{

			}
		} );
	}

	private void processChangeMessage( ChangeMessage changeMessage )
	{
		switch ( changeMessage.getId() ) {
		case TIME_POINT:
			listener.onTimepointChanged();
			break;
		case ACTIVE_SPOT:
			listener.onActiveSpotChanged();
			break;
		case UPDATE_COLORS_REQUEST:
			listener.onUpdateColorsRequest();
			break;
		case SELECTED_TAG_SET:
			listener.onSelectedTagSetChanged();
			break;
		case SYNC_GROUP:
			listener.onSyncGroupChanged();
			break;
		default:
			System.err.println("Unexpected event received from blender mastodon plugin.");
		}
	}

	public void sendSpotVisibilityAndPosition( SetSpotPositionRequest request )
	{
		blockingStub.setSpotVisibilityAndPosition( request );
	}

	public void sendSelection( SetSelectionRequest request )
	{
		blockingStub.setSelection( request );
	}

	public interface Listener {

		void onSyncGroupChanged();

		void onTimepointChanged();

		void onActiveSpotChanged();

		void onUpdateColorsRequest();

		void onSelectedTagSetChanged();

		void onConnectionLost();
	}
}

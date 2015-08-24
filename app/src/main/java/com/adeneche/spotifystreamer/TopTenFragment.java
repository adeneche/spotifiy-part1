package com.adeneche.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adeneche.spotifystreamer.parcels.ArtistParcel;
import com.adeneche.spotifystreamer.parcels.TrackParcel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TopTenFragment extends Fragment {
    private final String LOG_TAG = TopTenFragment.class.getSimpleName();

    private final String SAVE_TRACKS = "tracks";
    private final String SAVE_ARTIST = "artistName";
    private final static String PLAYER_TAG = "playerDialog";
    public final static String ARGUMENT_ARTIST = "ARTIST";

    private ArrayList<TrackParcel> mTracks;
    private String mArtistName;
    private TracksListAdapter mTopTenAdapter;
    private final SpotifyService mSpotifyService;

    public TopTenFragment() {
        final SpotifyApi spotifyApi = new SpotifyApi();
        mSpotifyService = spotifyApi.getService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mTracks != null && !mTracks.isEmpty()) {
            outState.putParcelableArrayList(SAVE_TRACKS, mTracks);
            outState.putString(SAVE_ARTIST, mArtistName);
        }
        super.onSaveInstanceState(outState);
    }

    private void showPlaybackDialog(int position) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(PLAYER_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        PlaybackFragment fragment = PlaybackFragment.newInstance(mTracks, position);
        fragment.show(fm, PLAYER_TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_top_ten, container, false);

        final ListView listView = (ListView) rootView.findViewById(R.id.listview_topten);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPlaybackDialog(position);
            }
        });

        mTopTenAdapter = new TracksListAdapter(getActivity(), new ArrayList<TrackParcel>());
        listView.setAdapter(mTopTenAdapter);

        initList(savedInstanceState);

        ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(mArtistName);

        return rootView;
    }

    private void initList(final Bundle savedInstanceState) {
        // first check if we have any saved state
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_TRACKS)) {
            mTracks = savedInstanceState.getParcelableArrayList(SAVE_TRACKS);
            mArtistName = savedInstanceState.getString(SAVE_ARTIST);
            mTopTenAdapter.addAll(mTracks);
            return;
        }

        // check if we have any passed arguments
        final Bundle arguments = getArguments();
        if (arguments != null && arguments.getParcelable(ARGUMENT_ARTIST) != null) {
            final ArtistParcel artist = arguments.getParcelable(ARGUMENT_ARTIST);
            mArtistName = artist.name;
            searchTrack(artist.id);
            return;
        }

        // finally, check if we have any parcelable passed with the intent
        final Intent intent = getActivity().getIntent();
        if (intent != null && intent.getExtras() != null) {
            final Bundle extras = intent.getExtras();
            final ArtistParcel artist = extras.getParcelable(ARGUMENT_ARTIST);
            mArtistName = artist.name;
            searchTrack(artist.id);
        }
    }

    private void searchTrack(String spotifyID) {
        if (spotifyID == null || spotifyID.isEmpty()) {
            Log.w(LOG_TAG, "empty or null spotifyID, this shouldn't happen");
        } else {
            mSpotifyService.getArtistTopTrack(spotifyID,
                    Collections.<String, Object>singletonMap("country", "us"),
                    new Callback<Tracks>() {
                        @Override
                        public void success(Tracks result, Response response) {
                            if (result.tracks.isEmpty()) {
                                Utils.displayToast(getActivity(), "No Tracks found!");
                            } else {
                                final ArrayList<TrackParcel> parcels =
                                        TrackParcel.toParcelArrayList(result.tracks);
                                // update mSearchAdapter
                                mTopTenAdapter.clear();
                                mTopTenAdapter.addAll(parcels);
                                // store results locally
                                mTracks = parcels;
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.e(LOG_TAG, "error accessing Spotify API!", error);
                            Utils.displayToast(getActivity(), "Error accessing Spotify API!");
                        }
                    });
        }
    }

    private class TracksListAdapter extends ArrayAdapter<TrackParcel> {

        public TracksListAdapter(Context context, List<TrackParcel> tracks) {
            super(context, -1, tracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_topten, null);
            }

            TrackParcel track = getItem(position);

            if (track != null) {
                TextView albumTxt = (TextView) convertView.findViewById(R.id.item_top10_album);
                albumTxt.setText(track.albumName);
                TextView trackTxt = (TextView) convertView.findViewById(R.id.item_top10_track);
                trackTxt.setText(track.trackName);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.item_top10_thumbnail);
                if (!track.thumbnailUrl.isEmpty()) {
                    Picasso.with(getContext()).load(track.thumbnailUrl)
                        .resize(200, 200).centerCrop().into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.no_image_available);
                }
            }

            return convertView;
        }
    }

}

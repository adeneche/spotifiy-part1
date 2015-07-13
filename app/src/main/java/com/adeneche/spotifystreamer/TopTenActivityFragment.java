package com.adeneche.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adeneche.spotifystreamer.parcels.TrackParcel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopTenActivityFragment extends Fragment {

    private final String SAVE_KEY = "tracks";

    private ArrayList<TrackParcel> tracks;

    private TracksListAdapter mTopTenAdapter;
    private final SpotifyService spotifyService;

    public TopTenActivityFragment() {
        final SpotifyApi spotifyApi = new SpotifyApi();
        spotifyService = spotifyApi.getService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (tracks != null && !tracks.isEmpty()) {
            outState.putParcelableArrayList(SAVE_KEY, tracks);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_top_ten, container, false);

        mTopTenAdapter = new TracksListAdapter(getActivity(), new ArrayList<TrackParcel>());
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_topten);
        listView.setAdapter(mTopTenAdapter);

        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVE_KEY)) {
            final Bundle extras = getActivity().getIntent().getExtras();

            final String spotifyID = extras.getString("id");
            searchTrack(spotifyID);

            final String artistName = extras.getString("name");
            ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(artistName);
        } else {
            tracks = savedInstanceState.getParcelableArrayList(SAVE_KEY);
            mTopTenAdapter.addAll(tracks);
        }

        return rootView;
    }

    private void searchTrack(String spotifyID) {
        if (spotifyID == null || spotifyID.isEmpty()) {
            displayToast("Empty Spotify ID!");
        } else {
            new SearchTop10Task().execute(spotifyID);
        }
    }

    private void displayToast(final String message) {
        final Context context = getActivity();
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private class SearchTop10Task extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected void onPostExecute(List<Track> tracks) {
            if (tracks != null) {
                final ArrayList<TrackParcel> parcels = TrackParcel.toParcelArrayList(tracks);
                // update mSearchAdapter
                mTopTenAdapter.clear();
                mTopTenAdapter.addAll(parcels);
                // store results locally
                TopTenActivityFragment.this.tracks = parcels;
            } else {
                displayToast("No Tracks found!");
            }
        }

        @Override
        protected List<Track> doInBackground(String... params) {
            final String artist = params[0];
            Tracks results = spotifyService.getArtistTopTrack(
                artist, Collections.<String, Object>singletonMap("country", "us"));

            if (results.tracks.isEmpty()) {
                return null;
            }

            return results.tracks;
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

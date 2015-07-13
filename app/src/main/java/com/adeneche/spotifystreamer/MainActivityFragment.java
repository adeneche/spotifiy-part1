package com.adeneche.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adeneche.spotifystreamer.parcels.ArtistParcel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

public class MainActivityFragment extends Fragment {

    private final String SAVE_KEY = "artists";

    private ArtistListAdapter mSearchAdapter;
    private ListView searchListView;

    private final SpotifyService spotifyService;

    private ArrayList<ArtistParcel> artists;

    public MainActivityFragment() {
        final SpotifyApi spotifyApi = new SpotifyApi();
        spotifyService = spotifyApi.getService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (artists != null && !artists.isEmpty()) {
            outState.putParcelableArrayList(SAVE_KEY, artists);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // load saved artists, if any
        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVE_KEY)) {
            artists = new ArrayList<>();
        } else {
            artists = savedInstanceState.getParcelableArrayList(SAVE_KEY);
        }

        mSearchAdapter = new ArtistListAdapter(getActivity(), artists);

        final View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        searchListView = (ListView) rootView.findViewById(R.id.listview_search);
        searchListView.setAdapter(mSearchAdapter);

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ArtistParcel parcel = mSearchAdapter.getItem(position);
                Bundle extras = new Bundle();
                extras.putString("name", parcel.name);
                extras.putString("id", parcel.id);
                Intent detailIntent = new Intent(getActivity(), TopTenActivity.class).putExtras(extras);
                startActivity(detailIntent);
            }
        });

        final EditText editText = (EditText) rootView.findViewById(R.id.edittext_artist);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchArtist(editText.getText().toString());
                }
                return false; // return false to allow keyboard to close (default handling)
            }
        });

        return rootView;
    }

    private void searchArtist(String artistName) {
        if (artistName == null || artistName.isEmpty()) {
            displayToast("Empty artist name!");
        } else {
            new SearchArtistsTask().execute(artistName);
        }
    }

    private void displayToast(final String message) {
        final Context context = getActivity();
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private class SearchArtistsTask extends AsyncTask<String, Void, Pager<Artist>> {

        @Override
        protected void onPostExecute(Pager<Artist> artists) {
            if (artists != null) {
                final ArrayList<ArtistParcel> parcels = ArtistParcel.toParcelArrayList(artists.items);
                // update mSearchAdapter
                mSearchAdapter.clear();
                mSearchAdapter.addAll(parcels);
                // move to beginning of listview
                searchListView.smoothScrollToPosition(0);
                // store query results locally
                MainActivityFragment.this.artists = parcels;
            } else {
                displayToast("No artist found!");
            }
        }

        @Override
        protected Pager<Artist> doInBackground(String... params) {
            final String artist = params[0];
            ArtistsPager results = spotifyService.searchArtists(artist);

            if (results.artists.total == 0) {
                return null;
            }

            return results.artists;
        }
    }

    private class ArtistListAdapter extends ArrayAdapter<ArtistParcel> {

        public ArtistListAdapter(Context context, List<ArtistParcel> artists) {
            super(context, -1, artists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_search, null);
            }

            ArtistParcel artist = getItem(position);

            if (artist != null) {
                TextView nameTxt = (TextView) convertView.findViewById(R.id.item_search_name);
                nameTxt.setText(artist.name);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.item_search_thumbnail);
                if (!artist.thumbnailUrl.isEmpty()) {
                    Picasso.with(getContext()).load(artist.thumbnailUrl).resize(200, 200)
                        .centerCrop().into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.no_image_available);
                }
            }

            return convertView;
        }
    }
}

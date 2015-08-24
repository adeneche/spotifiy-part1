package com.adeneche.spotifystreamer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivityFragment extends Fragment {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public interface Callback {
        void onItemSelected(final ArtistParcel artist);
    }

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
            Log.i(LOG_TAG, "saving " + artists.size() + " artists");
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
            Log.i(LOG_TAG, "Loaded " + artists.size() + " artists");
        }

        mSearchAdapter = new ArtistListAdapter(getActivity(), artists);

        final View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        searchListView = (ListView) rootView.findViewById(R.id.listview_search);
        searchListView.setAdapter(mSearchAdapter);

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ArtistParcel parcel = mSearchAdapter.getItem(position);
                ((Callback)getActivity()).onItemSelected(parcel);
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

    private void searchArtist(final String artistName) {
        if (artistName == null || artistName.isEmpty()) {
            displayToast("Empty artist name!");
        } else {
            spotifyService.searchArtists(artistName, new retrofit.Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    if (artistsPager.artists.total == 0) {
                        displayToast("No artist found!");
                    } else {
                        final ArrayList<ArtistParcel> parcels =
                                ArtistParcel.toParcelArrayList(artistsPager.artists.items);
                        // update mSearchAdapter
                        mSearchAdapter.clear();
                        mSearchAdapter.addAll(parcels);
                        // move to beginning of listview
                        searchListView.smoothScrollToPosition(0);
                        // store query results locally
                        artists = parcels;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(LOG_TAG, "error searching artist "+artistName, error);
                    displayToast("Error accessing Spotify");
                }
            });
        }
    }

    private void displayToast(final String message) {
        final Context context = getActivity();
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
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

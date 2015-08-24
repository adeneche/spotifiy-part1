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

import com.adeneche.spotifystreamer.parcels.ArtistParcel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ArtistSearchFragment extends Fragment {
    private final String LOG_TAG = ArtistSearchFragment.class.getSimpleName();

    public interface Callback {
        void onItemSelected(final ArtistParcel artist);
    }

    private final String SAVE_KEY = "artists";

    private ArtistListAdapter mSearchAdapter;
    private ListView mSearchListView;
    private final SpotifyService mSpotifyService;
    private ArrayList<ArtistParcel> mArtists;

    public ArtistSearchFragment() {
        final SpotifyApi spotifyApi = new SpotifyApi();
        mSpotifyService = spotifyApi.getService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mArtists != null && !mArtists.isEmpty()) {
            outState.putParcelableArrayList(SAVE_KEY, mArtists);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // load saved mArtists, if any
        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVE_KEY)) {
            mArtists = new ArrayList<>();
        } else {
            mArtists = savedInstanceState.getParcelableArrayList(SAVE_KEY);
        }

        mSearchAdapter = new ArtistListAdapter(getActivity(), mArtists);

        final View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        mSearchListView = (ListView) rootView.findViewById(R.id.listview_search);
        mSearchListView.setAdapter(mSearchAdapter);
        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ArtistParcel parcel = mSearchAdapter.getItem(position);
                ((Callback) getActivity()).onItemSelected(parcel);
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
            Utils.displayToast(getActivity(), "Empty artist name!");
        } else {
            mSpotifyService.searchArtists(artistName, new retrofit.Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    if (artistsPager.artists.total == 0) {
                        Utils.displayToast(getActivity(), "No artist found!");
                    } else {
                        final ArrayList<ArtistParcel> parcels =
                                ArtistParcel.toParcelArrayList(artistsPager.artists.items);
                        // update mSearchAdapter
                        mSearchAdapter.clear();
                        mSearchAdapter.addAll(parcels);
                        // move to beginning of listview
                        mSearchListView.smoothScrollToPosition(0);
                        // store query results locally
                        mArtists = parcels;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(LOG_TAG, "error searching artist " + artistName, error);
                    Utils.displayToast(getActivity(), "Error accessing Spotify");
                }
            });
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

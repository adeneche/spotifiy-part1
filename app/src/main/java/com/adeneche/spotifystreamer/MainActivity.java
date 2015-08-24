package com.adeneche.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.adeneche.spotifystreamer.parcels.ArtistParcel;


public class MainActivity extends ActionBarActivity implements ArtistSearchFragment.Callback {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.topten_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.topten_detail_container, new TopTenFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(final ArtistParcel artist) {
        Log.i(LOG_TAG, "Selected artist " + artist.name);
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(TopTenFragment.ARGUMENT_ARTIST, artist);

            TopTenFragment fragment = new TopTenFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.topten_detail_container, fragment)
                    .commit();
        } else {
            Bundle extras = new Bundle();
            extras.putParcelable(TopTenFragment.ARGUMENT_ARTIST, artist);
            Intent detailIntent = new Intent(this, TopTenActivity.class).putExtras(extras);
            startActivity(detailIntent);
        }
    }
}

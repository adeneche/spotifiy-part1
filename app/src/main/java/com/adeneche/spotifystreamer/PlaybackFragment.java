package com.adeneche.spotifystreamer;

import android.app.Application;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.adeneche.spotifystreamer.parcels.TrackParcel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlaybackFragment extends DialogFragment implements PlayerService.OnPreparedCallback {
    private static final String LOG_TAG = PlaybackFragment.class.getSimpleName();

    private final static String ARG_TRACKS = "tracks";
    private final static String ARG_SELECTED = "selected";
    private ArrayList<TrackParcel> mTracks;
    protected int mSelected;
    private TrackHolder mHolder;

    private Handler mSeekHandler = new Handler();

    private PlayerService mPlayerService;
    private Intent mPlayIntent;
    private boolean mPlayerBound;
    private boolean mPlaying;

    private ServiceConnection mPlayerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            // get service
            mPlayerService = binder.getService();
            mPlayerService.setOnPreparedCallback(PlaybackFragment.this);
            mPlayerBound = true;
            // start playing selected song
            playSelectedTrack();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayerBound = false;
        }
    };

    static PlaybackFragment newInstance(final ArrayList<TrackParcel> tracks, final int selected) {
        final PlaybackFragment dialog = new PlaybackFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_TRACKS, tracks);
        bundle.putInt(ARG_SELECTED, selected);
        dialog.setArguments(bundle);
        return dialog;
    }

    private ArrayList<TrackParcel> getTracks() {
        return (ArrayList<TrackParcel>) getArguments().get(ARG_TRACKS);
    }

    private int getSelected() {
        return getArguments().getInt(ARG_SELECTED);
    }

    private void updateUI(TrackParcel track) {

        if (!track.thumbnailUrl.isEmpty()) {
            Picasso.with(getActivity()).load(track.thumbnailUrl)
                    .resize(200, 200).centerCrop().into(mHolder.thumbnailImage);
        } else {
            mHolder.thumbnailImage.setImageResource(R.drawable.no_image_available);
        }

        mHolder.seekBar.setProgress(0);

        mHolder.artistTxt.setText(track.artistName);
        mHolder.albumText.setText(track.albumName);
        mHolder.trackText.setText(track.trackName);

        mHolder.playBtn.setImageResource(android.R.drawable.ic_media_play);
        mHolder.prevBtn.setAlpha(mSelected == 0 ? .35f : 1f);
        mHolder.nextBtn.setAlpha(mSelected == mTracks.size() - 1 ? .35f : 1f);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            final Application application = getActivity().getApplication();
            mPlayIntent = new Intent(application, PlayerService.class);
            application.bindService(mPlayIntent, mPlayerConnection, Context.BIND_AUTO_CREATE);
            application.startService(mPlayIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_SELECTED, mSelected);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.player_fragment, container, false);

        mTracks = getTracks();
        mSelected = getSelected();

        mHolder = new TrackHolder(view);
        mHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mPlayerBound) {
                    mPlayerService.getPlayer().seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mHolder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic();
            }
        });

        mHolder.prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected > 0) {
                    mSelected--;
                    playSelectedTrack();
                }
            }
        });

        mHolder.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected + 1 < mTracks.size()) {
                    mSelected++;
                    playSelectedTrack();
                }
            }
        });

        if (savedInstanceState != null) {
            mSelected = savedInstanceState.getInt(ARG_SELECTED);
            playSelectedTrack();
        }
        return view;
    }

    @Override
    public void OnPrepared() {
        int duration = mPlayerService.getPlayer().getDuration() / 1000;
        mHolder.elapsedText.setText(String.format("%d:%02d", duration / 60, duration % 60));

        mHolder.playBtn.setImageResource(android.R.drawable.ic_media_pause);
        mHolder.playBtn.setAlpha(1f);
        mPlaying = true;
        seekUpdate();
    }

    private void playSelectedTrack() {
        final TrackParcel track = mTracks.get(mSelected);
        updateUI(track);

        mPlayerService.playSong(track.previewUrl);
        mPlaying = false;
        mHolder.playBtn.setAlpha(.35f);
    }

    private void playMusic() {
        if (!mPlayerBound) {
            return;
        }

        final MediaPlayer player = mPlayerService.getPlayer();
        if (player.isPlaying()) {
            player.pause();
            mHolder.playBtn.setImageResource(android.R.drawable.ic_media_play);
        } else {
            player.start();
            mHolder.playBtn.setImageResource(android.R.drawable.ic_media_pause);
            seekUpdate();
        }
    }

    private final Runnable mSeekRunnable = new Runnable() {
        @Override
        public void run() {
            seekUpdate();
        }
    };

    private void seekUpdate() {
        if (!mPlayerBound || !mPlaying) {
            return;
        }

        final MediaPlayer player = mPlayerService.getPlayer();
        int pos = player.getCurrentPosition();
        mHolder.seekBar.setMax(player.getDuration());
        mHolder.seekBar.setProgress(pos);
        pos /= 1000;
        mHolder.elapsedText.setText(String.format("%d:%02d", pos / 60, pos % 60));
        mSeekHandler.postDelayed(mSeekRunnable, 100);
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}

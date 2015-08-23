package com.adeneche.spotifystreamer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.adeneche.spotifystreamer.parcels.TrackParcel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class PlayerDialog extends DialogFragment {
    private static final String LOG_TAG = PlayerDialog.class.getSimpleName();

    private ArrayList<TrackParcel> mTracks;
    protected int mSelected;

    private MediaPlayer mMediaPlayer;
    private Handler mSeekHandler = new Handler();

    private TrackHolder holder;

    private boolean started;

    static PlayerDialog newInstance(final ArrayList<TrackParcel> tracks, final int selected) {
        final PlayerDialog dialog = new PlayerDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("tracks", tracks);
        bundle.putInt("selected", selected);
        dialog.setArguments(bundle);
        return dialog;
    }

    private ArrayList<TrackParcel> getTracks() {
        return (ArrayList<TrackParcel>) getArguments().get("tracks");
    }

    private int getSelected() {
        return getArguments().getInt("selected");
    }

    private void updateUI(TrackParcel track) {

        if (!track.thumbnailUrl.isEmpty()) {
            Picasso.with(getActivity()).load(track.thumbnailUrl)
                    .resize(200, 200).centerCrop().into(holder.thumbnailImage);
        } else {
            holder.thumbnailImage.setImageResource(R.drawable.no_image_available);
        }

        holder.seekBar.setProgress(0);

        holder.artistTxt.setText(track.artistName);
        holder.albumText.setText(track.albumName);
        holder.trackText.setText(track.trackName);

        if (!track.thumbnailUrl.isEmpty()) {
            Picasso.with(getActivity()).load(track.thumbnailUrl)
                    .resize(200, 200).centerCrop().into(holder.thumbnailImage);
        } else {
            holder.thumbnailImage.setImageResource(R.drawable.no_image_available);
        }

        holder.playBtn.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.player_fragment, container, false);

        mTracks = getTracks();
        mSelected = getSelected();

        holder = new TrackHolder(view);

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && started) {
                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        holder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic();
            }
        });

        holder.prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected > 0) {
                    mSelected--;
                    playSelectedTrack();
                }
            }
        });

        holder.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected + 1 < mTracks.size()) {
                    mSelected++;
                    playSelectedTrack();
                }
            }
        });

        playSelectedTrack();

        return view;
    }

    private void playSelectedTrack() {
        //TODO what happens if I select another track while current one is still preparing ?
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            started = false;
        } else {
            mMediaPlayer = new MediaPlayer();
        }

        final TrackParcel track = mTracks.get(mSelected);

        updateUI(track);

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(track.previewUrl);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    started = true;
                }
            });
            mMediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            Log.e(LOG_TAG, "Couldn't prepare media player", e);
            mMediaPlayer = null;
        }
    }

    private void playMusic() {
        if (!started) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            holder.playBtn.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mMediaPlayer.start();
            holder.playBtn.setImageResource(android.R.drawable.ic_media_pause);
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
        if (started) {
            final int pos = mMediaPlayer.getCurrentPosition();
            holder.seekBar.setMax(mMediaPlayer.getDuration());
            holder.seekBar.setProgress(pos);
            mSeekHandler.postDelayed(mSeekRunnable, 1000);
        }
    }

    @Override
    public void onStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            started = false;
        }
        super.onStop();
    }
}

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

public class PlaybackFragment extends DialogFragment {
    private static final String LOG_TAG = PlaybackFragment.class.getSimpleName();

    private final static String SAVE_TRACKS = "tracks";
    private final static String SAVE_SELECTED = "selected";
    private ArrayList<TrackParcel> mTracks;
    protected int mSelected;
    private MediaPlayer mMediaPlayer;
    private TrackHolder mHolder;
    private boolean mStarted;

    private Handler mSeekHandler = new Handler();

    static PlaybackFragment newInstance(final ArrayList<TrackParcel> tracks, final int selected) {
        final PlaybackFragment dialog = new PlaybackFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(SAVE_TRACKS, tracks);
        bundle.putInt(SAVE_SELECTED, selected);
        dialog.setArguments(bundle);
        return dialog;
    }

    private ArrayList<TrackParcel> getTracks() {
        return (ArrayList<TrackParcel>) getArguments().get(SAVE_TRACKS);
    }

    private int getSelected() {
        return getArguments().getInt(SAVE_SELECTED);
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
        mHolder.nextBtn.setAlpha(mSelected == mTracks.size() - 1 ? .35f : 1f );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
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
                if (fromUser && mStarted) {
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

        playSelectedTrack();

        return view;
    }

    private void playSelectedTrack() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mStarted = false;
        } else {
            mMediaPlayer = new MediaPlayer();
        }

        final TrackParcel track = mTracks.get(mSelected);

        updateUI(track);

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(track.previewUrl);
            mHolder.playBtn.setAlpha(.35f);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mHolder.playBtn.setAlpha(1f);
                    mStarted = true;
                }
            });
            mMediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            Log.e(LOG_TAG, "Couldn't prepare media player", e);
            mMediaPlayer = null;
        }
    }

    private void playMusic() {
        if (!mStarted) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mHolder.playBtn.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mMediaPlayer.start();
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
        if (mStarted) {
            final int pos = mMediaPlayer.getCurrentPosition();
            mHolder.seekBar.setMax(mMediaPlayer.getDuration());
            mHolder.seekBar.setProgress(pos);
            mSeekHandler.postDelayed(mSeekRunnable, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mStarted = false;
        }
        dismissAllowingStateLoss();
    }
}

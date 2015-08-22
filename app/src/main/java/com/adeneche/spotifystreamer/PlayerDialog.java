package com.adeneche.spotifystreamer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.adeneche.spotifystreamer.parcels.TrackParcel;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PlayerDialog extends DialogFragment {
    private static final String LOG_TAG = PlayerDialog.class.getSimpleName();

    private MediaPlayer mMediaPlayer;

    static PlayerDialog newInstance(final TrackParcel track) {
        final PlayerDialog dialog = new PlayerDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("track", track);
        dialog.setArguments(bundle);
        return dialog;
    }

    private TrackParcel getTrack() {
        return (TrackParcel) getArguments().get("track");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.player_fragment, container, false);
        final TrackParcel track = getTrack();

        TextView albumTxt = (TextView) view.findViewById(R.id.player_album);
        albumTxt.setText(track.albumName);
        TextView trackTxt = (TextView) view.findViewById(R.id.player_track);
        trackTxt.setText(track.trackName);

        ImageView imageView = (ImageView) view.findViewById(R.id.player_thumbnail);
        if (!track.thumbnailUrl.isEmpty()) {
            Picasso.with(getActivity()).load(track.thumbnailUrl)
                    .resize(200, 200).centerCrop().into(imageView);
        } else {
            imageView.setImageResource(R.drawable.no_image_available);
        }

        ImageButton playBtn = (ImageButton) view.findViewById(R.id.player_play);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic(track.previewUrl);
            }
        });

        return view;
    }

    private void playMusic(final String url) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepare(); // might take long! (for buffering, etc)
                mMediaPlayer.start();
            } catch (IOException e) {
            }
        } else {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.start();
            }
        }
    }

    @Override
    public void onStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onStop();
    }
}

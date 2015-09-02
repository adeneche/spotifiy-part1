package com.adeneche.spotifystreamer;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class TrackHolder {
    public final TextView artistTxt;
    public final TextView albumText;
    public final TextView trackText;
    public final ImageView thumbnailImage;
    public final SeekBar seekBar;
    public final ImageButton prevBtn;
    public final ImageButton playBtn;
    public final ImageButton nextBtn;
    public final TextView elapsedText;
    public final TextView durationText;

    public TrackHolder(final View view) {
        artistTxt = (TextView) view.findViewById(R.id.player_artist);
        albumText = (TextView) view.findViewById(R.id.player_album);
        trackText = (TextView) view.findViewById(R.id.player_track);
        thumbnailImage = (ImageView) view.findViewById(R.id.player_thumbnail);
        seekBar = (SeekBar) view.findViewById(R.id.player_scrubbar);
        prevBtn = (ImageButton) view.findViewById(R.id.player_prev);
        playBtn = (ImageButton) view.findViewById(R.id.player_play);
        nextBtn = (ImageButton) view.findViewById(R.id.player_next);
        elapsedText = (TextView) view.findViewById(R.id.player_elapsed);
        durationText = (TextView) view.findViewById(R.id.player_duration);
    }
}

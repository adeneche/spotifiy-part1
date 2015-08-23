package com.adeneche.spotifystreamer.parcels;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.ParcelableSpan;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class TrackParcel implements Parcelable {

    public final String artistName;
    public final String albumName;
    public final String trackName;
    public final String thumbnailUrl;
    public final String previewUrl;

    public TrackParcel(Track track) {
        artistName = track.artists.get(0).name;
        albumName = track.album.name;
        trackName = track.name;
        thumbnailUrl = Utils.selectImage(track.album.images, 200).url;
        previewUrl = track.preview_url;
    }

    private TrackParcel(Parcel source) {
        artistName = source.readString();
        albumName = source.readString();
        trackName = source.readString();
        thumbnailUrl = source.readString();
        previewUrl = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistName);
        dest.writeString(albumName);
        dest.writeString(trackName);
        dest.writeString(thumbnailUrl);
        dest.writeString(previewUrl);
    }

    public static ArrayList<TrackParcel> toParcelArrayList(final List<Track> tracks) {
        final ArrayList<TrackParcel> parcels = new ArrayList<>();
        for (final Track track : tracks) {
            parcels.add(new TrackParcel(track));
        }
        return parcels;
    }

    public static final ParcelableSpan.Creator<TrackParcel> CREATOR = new ParcelableSpan.Creator<TrackParcel>() {
        @Override
        public TrackParcel createFromParcel(Parcel source) {
            return new TrackParcel(source);
        }

        @Override
        public TrackParcel[] newArray(int size) {
            return new TrackParcel[size];
        }
    };
}

package com.adeneche.spotifystreamer.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.adeneche.spotifystreamer.Utils;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

public class ArtistParcel implements Parcelable {
    // artist name
    public final String name;
    // sportifyID
    public final String id;
    // artist thumbnail
    public final String thumbnailUrl;

    public ArtistParcel(Artist artist) {
        name = artist.name;
        id = artist.id;
        if (artist.images.isEmpty()) {
            thumbnailUrl = "";
        } else {
            thumbnailUrl = Utils.selectImage(artist.images, 200).url;
        }
    }

    private ArtistParcel(Parcel source) {
        name = source.readString();
        id = source.readString();
        thumbnailUrl = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(thumbnailUrl);
    }

    public static ArrayList<ArtistParcel> toParcelArrayList(final List<Artist> artists) {
        final ArrayList<ArtistParcel> parcels = new ArrayList<>();
        for (final Artist artist : artists) {
            parcels.add(new ArtistParcel(artist));
        }
        return parcels;
    }

    public final static Parcelable.Creator<ArtistParcel> CREATOR = new Parcelable.Creator<ArtistParcel>() {

        @Override
        public ArtistParcel createFromParcel(Parcel source) {
            return new ArtistParcel(source);
        }

        @Override
        public ArtistParcel[] newArray(int size) {
            return new ArtistParcel[size];
        }
    };
}

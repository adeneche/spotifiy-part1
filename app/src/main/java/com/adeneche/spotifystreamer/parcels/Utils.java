package com.adeneche.spotifystreamer.parcels;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

public class Utils {

    public static Image selectImage(List<Image> images, int preferredSize) {
        Image closest = null;
        int minDist = Integer.MAX_VALUE;
        for (final Image image : images) {
            final int dist = Math.abs(image.width - preferredSize) + Math.abs(image.height - preferredSize);
            if (dist < minDist) {
                closest = image;
                minDist = dist;
            }
        }
        return closest;
    }
}

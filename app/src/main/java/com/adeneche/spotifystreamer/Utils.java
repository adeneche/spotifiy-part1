package com.adeneche.spotifystreamer;

import android.content.Context;
import android.widget.Toast;

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

    public static void displayToast(final Context context, final String message) {
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}

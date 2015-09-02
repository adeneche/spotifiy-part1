package com.adeneche.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    interface OnPreparedCallback {
        void OnPrepared();
    }

    public static final String LOG_TAG = PlayerService.class.getSimpleName();

    public static final String URL_KEY = "url";

    private MediaPlayer mPlayer;
    private final IBinder mPlayBinder = new PlayerBinder();
    private String currentTrack;

    private OnPreparedCallback onPreparedCallback;

    public void setOnPreparedCallback(OnPreparedCallback callback) {
        onPreparedCallback = callback;
    }

    @Override
    public void onCreate() {
        // create the service
        mPlayer = new MediaPlayer();
        initMusicPlayer();
        super.onCreate();
    }

    private void initMusicPlayer() {
        // set player properties
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);
    }

    public void playSong(String previewUrl) {
        if (mPlayer.isPlaying() && currentTrack != null && currentTrack.equals(previewUrl)) {
            Log.i("PlayerService", "track already playing");
            // keep playing the same track
            if (onPreparedCallback != null) {
                onPreparedCallback.OnPrepared();
            }
            return;
        }

        currentTrack = previewUrl;

        // play a song
        mPlayer.reset();
        try {
            mPlayer.setDataSource(previewUrl);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error setting data source", e);
        }
        mPlayer.prepareAsync();
    }

    public void stopSong() {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
    }

    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPlayBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mPlayer.stop();
        mPlayer.release();
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand with url = " + intent.getStringExtra(URL_KEY));
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // start playback
        mPlayer.start();

        if (onPreparedCallback != null) {
            onPreparedCallback.OnPrepared();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
}

package com.example.krishnateja.spotifystreamer.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.example.krishnateja.spotifystreamer.models.AppConstants;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by krishnateja on 6/6/2015.
 */
public class PlayMusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private final IBinder mBinder = new LocalBinder();
    private Handler mHandler;
    private Timer mTimer;
    private boolean mIsPaused;
    private int mCurrentPosition;


    @Override
    public void onCompletion(MediaPlayer mp) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putBoolean(AppConstants.BundleExtras.IS_PLAYING, false);
        message.setData(bundle);
        if (mHandler != null) {
            mHandler.sendMessage(message);
        }
        stopUpdatingUI();
    }

    public void changeSongPosition(int progress, boolean isPaused) {
        if ((mMediaPlayer != null && isPaused && !mMediaPlayer.isPlaying()) || (mMediaPlayer != null && mMediaPlayer.isPlaying())) {
            Log.d(TAG, "here in seekTo");
            mMediaPlayer.seekTo(progress * 1000);
            Log.d(TAG, "current position->" + (mMediaPlayer.getCurrentPosition() / 1000));
            if (mMediaPlayer.isPlaying()) {
                setTimer();
            }
        }
    }

    public int pauseSong() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mIsPaused = true;
            stopUpdatingUI();
            return mMediaPlayer.getCurrentPosition() / 1000;
        } else {
            return 0;
        }
    }

    public void playSong(int currentPosition) {
        mCurrentPosition = currentPosition;
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
        }
        initMediaPlayer();

        mIsPaused = false;

    }

    public void stopUpdatingUI() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
    }


    public class LocalBinder extends Binder {
        public PlayMusicService getService() {
            return PlayMusicService.this;
        }
    }

    private static final String TAG = PlayMusicService.class.getSimpleName();
    MediaPlayer mMediaPlayer;
    String mStreamURL;

    @Override
    public void onPrepared(final MediaPlayer player) {
        player.start();
        if (mCurrentPosition != 0) {
            player.seekTo(mCurrentPosition * 1000);
        }
        setTimer();
    }

    public void sendMessage(MediaPlayer player) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle = setTrackTimes(bundle, player);
        message.setData(bundle);
        if (mHandler != null) {
            mHandler.sendMessage(message);
        }
    }

    public void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mStreamURL);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(AppConstants.BundleExtras.PREVIEW_URL)) {
            String streamURL = intent.getStringExtra(AppConstants.BundleExtras.PREVIEW_URL);
            if (streamURL != null) {
                setStreamURL(intent.getStringExtra(AppConstants.BundleExtras.PREVIEW_URL));
                playSong(0);
            }

        }
        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mTimer != null) {
            stopUpdatingUI();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mHandler != null) {
            mHandler = null;
        }
    }

    public void setStreamURL(String url) {
        mStreamURL = url;
    }

    public void setHandle(Handler handler) {
        mHandler = handler;
        Message message = new Message();
        Bundle bundle = new Bundle();
        if (mMediaPlayer != null && (mMediaPlayer.isPlaying() || mIsPaused)) {
            bundle = setTrackTimes(bundle, mMediaPlayer);
            if (!mIsPaused) {
                setTimer();
            }
        } else {
            bundle.putBoolean(AppConstants.BundleExtras.IS_PLAYING, false);
        }
        message.setData(bundle);
        if (mHandler != null) {
            mHandler.sendMessage(message);
        }
    }

    public int getTrackDuration() {
        if (mMediaPlayer != null && (mMediaPlayer.isPlaying() || mIsPaused)) {
            return mMediaPlayer.getDuration() / 1000;
        } else {
            return 0;
        }
    }

    public Bundle setTrackTimes(Bundle bundle, MediaPlayer player) {
        if (player != null && (player.isPlaying() || mIsPaused)) {
            bundle.putBoolean(AppConstants.BundleExtras.IS_PLAYING, true);
            int start = (int) Math.ceil((double) player.getCurrentPosition() / 1000);
            bundle.putInt(AppConstants.BundleExtras.TRACK_POSITION, start);
        }
        return bundle;
    }

    public void setTimer() {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendMessage(mMediaPlayer);
            }
        }, 0, 1000);
    }
}

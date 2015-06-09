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
import android.widget.Toast;

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


    @Override
    public void onCompletion(MediaPlayer mp) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        Log.d(TAG, "onCompletion");
        bundle.putBoolean(AppConstants.BundleExtras.IS_PLAYING, false);
        message.setData(bundle);
        if(mHandler!=null) {
            mHandler.sendMessage(message);
        }
        mTimer.cancel();
        stopSelf();
    }

    public void changeSongPosition(int progress, boolean isPaused) {
        if ((mMediaPlayer != null && isPaused && !mMediaPlayer.isPlaying()) || mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(progress * 1000);
        }
    }

    public void pauseSong() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void playSong(boolean isPaused) {
        if (isPaused) {
            mMediaPlayer.start();
        } else {
            if (mMediaPlayer == null) {
                initMediaPlayer();
            } else {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    initMediaPlayer();
                } else {
                    mMediaPlayer.reset();
                    initMediaPlayer();
                }
            }
        }

    }

    public void stopUpdatingUI() {
        if (mTimer != null) {
            mTimer.cancel();
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
        //player.seekTo(mStartPosition);
        setTimer();
    }

    public void sendMessage(MediaPlayer player) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle = setTrackTimes(bundle, player);
        message.setData(bundle);
        if(mHandler!=null) {
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
        setStreamURL(intent.getStringExtra(AppConstants.BundleExtras.PREVIEW_URL));
        playSong(false);
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
        Log.d(TAG,"onDestroy");
        Toast.makeText(this, "destroyed", Toast.LENGTH_SHORT).show();
        mTimer.cancel();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public void setStreamURL(String url) {
        mStreamURL = url;
    }

    public void setHandle(Handler handler) {
        mHandler = handler;
        Message message = new Message();
        Bundle bundle = new Bundle();
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            bundle = setTrackTimes(bundle, mMediaPlayer);
            setTimer();
        } else {
            bundle.putBoolean(AppConstants.BundleExtras.IS_PLAYING, false);
        }
        message.setData(bundle);
        if(mHandler!=null) {
            mHandler.sendMessage(message);
        }
    }

    public Bundle setTrackTimes(Bundle bundle, MediaPlayer player) {
        Log.d(TAG,"here in setTrackTimes");
        if(player!=null && player.isPlaying()) {
            bundle.putBoolean(AppConstants.BundleExtras.IS_PLAYING, true);
            int start = (int) Math.ceil((double) player.getCurrentPosition() / 1000);
            int total = player.getDuration() / 1000;
            int end = total - start;
            String timeElapsed = "00:" + start;
            String timeRemaining = "00:" + end;
            bundle.putString(AppConstants.BundleExtras.TRACK_CURRENT_TIME, timeElapsed);
            bundle.putString(AppConstants.BundleExtras.TRACK_REMAINING_TIME, timeRemaining);
            bundle.putInt(AppConstants.BundleExtras.TRACK_POSITION, start);
            bundle.putInt(AppConstants.BundleExtras.TRACK_DURATION, total);
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

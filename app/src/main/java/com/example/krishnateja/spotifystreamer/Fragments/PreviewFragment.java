package com.example.krishnateja.spotifystreamer.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.krishnateja.spotifystreamer.R;
import com.example.krishnateja.spotifystreamer.models.AppConstants;
import com.example.krishnateja.spotifystreamer.models.TrackModel;
import com.example.krishnateja.spotifystreamer.services.PlayMusicService;
import com.example.krishnateja.spotifystreamer.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by krishnateja on 6/6/2015.
 */
public class PreviewFragment extends DialogFragment implements View.OnClickListener {

    int mPlayId = android.R.drawable.ic_media_play;
    int mPauseId = android.R.drawable.ic_media_pause;
    int mCurrentId;
    ImageView mPlayImageView;
    ArrayList<TrackModel> mTrackModelArrayList;
    int mTrackPosition;
    PlayMusicService mPlayMusicService;
    boolean mIsBound = false;
    TextView mTimeElapsedTextView, mTimeRemainingTextView;
    private static final String TAG = PreviewFragment.class.getSimpleName();
    private SeekBar mSeekBar;
    View mView;
    ImageView mPreviewImageView;
    TextView mSongTextView;
    boolean mIsPaused = false;
    private NowPlaying mNowPlaying;
    private String mArtistName;
    private boolean mIsPlaying;
    private TextView mTotalTimeTextView;
    private int mCurrentPosition;
    private int mTrackDuration = 0;
    private int mDeviceFlag;
    TextView mAlbumNameTextView;

    public interface NowPlaying {
        void removeInfoBar();

        void updateInfoBar(ArrayList<TrackModel> TrackModel, int position, String artistName, boolean isPaused, int currentPosition);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mNowPlaying = (NowPlaying) activity;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage() + " MainActivtiy should implement NowPlaying interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_preview, container, false);
        mNowPlaying.removeInfoBar();
        if (savedInstanceState == null) {
            setPreviewViews(getArguments());
        } else {
            setPreviewViews(savedInstanceState);
        }
        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSavedInstanceState");
        outState.putParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA, mTrackModelArrayList);
        outState.putString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA, mArtistName);
        outState.putInt(AppConstants.BundleExtras.TRACK_POSITION, mTrackPosition);
        outState.putBoolean(AppConstants.BundleExtras.IS_PLAYING, mIsPlaying);
        outState.putBoolean(AppConstants.BundleExtras.IS_PAUSED, mIsPaused);
        outState.putBoolean(AppConstants.BundleExtras.IS_BOUND, mIsBound);
        outState.putInt(AppConstants.BundleExtras.CURRENT_DEVICE, mDeviceFlag);
        super.onSaveInstanceState(outState);
    }

    private void setPreviewViews(Bundle arguments) {
        TextView artistNameTextView = (TextView) mView.findViewById(R.id.artist_name);
        mAlbumNameTextView = (TextView) mView.findViewById(R.id.album_name);
        mPreviewImageView = (ImageView) mView.findViewById(R.id.track_preview_image);
        mSongTextView = (TextView) mView.findViewById(R.id.song_name);
        mTimeElapsedTextView = (TextView) mView.findViewById(R.id.time_elapsed);
        mTimeRemainingTextView = (TextView) mView.findViewById(R.id.time_remaining);
        mTotalTimeTextView = (TextView) mView.findViewById(R.id.total_time);
        mTrackModelArrayList = arguments.getParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA);
        mTrackPosition = arguments.getInt(AppConstants.BundleExtras.TRACK_POSITION);
        mPlayImageView = (ImageView) mView.findViewById(R.id.play);
        ImageView previousImageView = (ImageView) mView.findViewById(R.id.previous);
        ImageView nextImageView = (ImageView) mView.findViewById(R.id.next);
        mSeekBar = (SeekBar) mView.findViewById(R.id.song_seek_bar);
        mPlayImageView.setOnClickListener(this);
        previousImageView.setOnClickListener(this);
        nextImageView.setOnClickListener(this);
        mArtistName = arguments.getString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA);
        artistNameTextView.setText(mArtistName);
        mAlbumNameTextView.setText(mTrackModelArrayList.get(mTrackPosition).getAlbumName());
        Picasso.with(getActivity()).load(mTrackModelArrayList.get(mTrackPosition).getLargeImage()).into(mPreviewImageView);
        mSongTextView.setText(mTrackModelArrayList.get(mTrackPosition).getTrackName());
        mIsPlaying = arguments.getBoolean(AppConstants.BundleExtras.IS_PLAYING);
        mIsPaused = arguments.getBoolean(AppConstants.BundleExtras.IS_PAUSED);
        if (mIsPaused) {
            mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
            mCurrentId = android.R.drawable.ic_media_play;
        } else {
            mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
            mCurrentId = android.R.drawable.ic_media_pause;
        }
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTimeElapsedTextView.setText(formatPlayTime(progress));
                mTimeRemainingTextView.setText(formatPlayTime(mTrackDuration - progress));
                mCurrentPosition = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mIsPlaying) {
                    mPlayMusicService.stopUpdatingUI();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCurrentPosition = seekBar.getProgress();
                if (mPlayMusicService != null) {
                    mPlayMusicService.changeSongPosition(mCurrentPosition, mIsPaused);
                }
            }
        });
        mDeviceFlag = arguments.getInt(AppConstants.BundleExtras.DEVICE);
        manipulateActionBar(arguments.getInt(AppConstants.BundleExtras.DEVICE));
        Intent intent = new Intent(getActivity(), PlayMusicService.class);
        intent.putExtra(AppConstants.BundleExtras.PREVIEW_URL, mTrackModelArrayList.get(mTrackPosition).getPreview());
        if (Utils.isMyServiceRunning(PlayMusicService.class, getActivity()) && !mIsPlaying) {
            mCurrentPosition = 0;
            getActivity().getApplicationContext().stopService(intent);
            getActivity().getApplicationContext().startService(intent);
        } else if (!Utils.isMyServiceRunning(PlayMusicService.class, getActivity())) {
            mCurrentPosition = 0;
            getActivity().getApplicationContext().startService(intent);
        }
        if (mPlayMusicService == null) {
            getActivity().getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void manipulateActionBar(int deviceFlag) {
        if (deviceFlag == AppConstants.FLAGS.PHONE_FLAG) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(getString(R.string.now_playing));
                actionBar.setSubtitle("");
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayMusicService != null) {
            mPlayMusicService.stopUpdatingUI();
            if (mIsBound) {
                getActivity().getApplicationContext().unbindService(mConnection);
                mIsBound = false;
            }
            if (mIsPlaying) {
                mNowPlaying.updateInfoBar(mTrackModelArrayList, mTrackPosition, mArtistName, mIsPaused, mCurrentPosition);
            }
        }
        if (!mIsPaused && !mIsPlaying) {
            getActivity().getApplicationContext().stopService(new Intent(getActivity(), PlayMusicService.class));
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.play:
                if (mCurrentId == mPlayId) {
                    mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
                    mCurrentId = mPauseId;
                    mPlayMusicService.playSong(mCurrentPosition);
                    mIsPaused = false;
                    mIsPlaying = true;
                } else {
                    mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
                    mCurrentId = mPlayId;
                    mPlayMusicService.pauseSong();
                    mIsPaused = true;
                    mIsPlaying = true;
                }
                break;
            case R.id.next:
                mTrackPosition = (mTrackPosition + 1) % mTrackModelArrayList.size();
                nextSong();
                break;
            case R.id.previous:
                mTrackPosition = (mTrackPosition - 1);
                if (mTrackPosition < 0) {
                    mTrackPosition = mTrackModelArrayList.size() - 1;
                }
                nextSong();
                break;
            default:
                break;
        }
    }

    public void nextSong() {
        mIsPlaying = true;
        mIsPaused = false;
        mSongTextView.setText(mTrackModelArrayList.get(mTrackPosition).getTrackName());
        mAlbumNameTextView.setText(mTrackModelArrayList.get(mTrackPosition).getAlbumName());
        Picasso.with(getActivity()).load(mTrackModelArrayList.get(mTrackPosition).getLargeImage()).into(mPreviewImageView);
        mPlayMusicService.setStreamURL(mTrackModelArrayList.get(mTrackPosition).getPreview());
        mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
        mCurrentId = android.R.drawable.ic_media_pause;
        mPlayMusicService.stopUpdatingUI();
        mPlayMusicService.playSong(0);
        resetSeekBar();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayMusicService.LocalBinder binder = (PlayMusicService.LocalBinder) service;
            mPlayMusicService = binder.getService();
            mIsBound = true;
            if (!mIsPlaying) {
                mIsPlaying = true;
            }
            setUpSeekBarMax();
            mPlayMusicService.setHandle(handler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mIsBound = false;
        }
    };


    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mTrackDuration == 0) {
                setUpSeekBarMax();
            }
            int trackPosition = msg.getData().getInt(AppConstants.BundleExtras.TRACK_POSITION);
            mCurrentPosition = trackPosition;
            mTimeElapsedTextView.setText(formatPlayTime(mCurrentPosition));
            mTimeRemainingTextView.setText(formatPlayTime(mTrackDuration - mCurrentPosition));
            mSeekBar.setProgress(trackPosition);
            if (mCurrentPosition == mTrackDuration && mCurrentPosition != 0) {
                mIsPlaying = false;
                mIsPaused = false;
                mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
                mCurrentId = android.R.drawable.ic_media_play;
                mCurrentPosition = 0;
            }
        }

    };

    public void setUpSeekBarMax() {
        if (mPlayMusicService != null) {
            mTrackDuration = mPlayMusicService.getTrackDuration();
            mSeekBar.setMax(mTrackDuration);
            mTotalTimeTextView.setText("\\" + formatPlayTime(mTrackDuration));
        }
    }

    public String formatPlayTime(int time) {
        return "00:" + String.format("%02d", time);

    }

    public void resetSeekBar() {
        mSeekBar.setProgress(0);
        mTotalTimeTextView.setText("");
        mTimeElapsedTextView.setText("");
        mTimeRemainingTextView.setText("");
        mTrackDuration = 0;
    }

}

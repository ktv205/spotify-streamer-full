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
    int mSelectedItem;
    PlayMusicService mPlayMusicService;
    boolean mBound = false;
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
    private int mCurrentPostition;

    public interface NowPlaying {
        public void removeNowPlaying();

        public void updateNowPlaying(ArrayList<TrackModel> TrackModel, int position, String artistName, boolean isPaused,int currentPosition);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNowPlaying = (NowPlaying) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_preview, container, false);
        mNowPlaying.removeNowPlaying();
        if (savedInstanceState == null) {
            setPreviewViews(getArguments());
        }
        return mView;
    }

    private void setPreviewViews(Bundle arguments) {
        Log.d(TAG, "mIsPlaying->" + mIsPlaying);
        manipulateActionBar(arguments.getInt(AppConstants.BundleExtras.DEVICE));
        mIsPlaying = arguments.getBoolean(AppConstants.BundleExtras.IS_PLAYING);
        mIsPaused = arguments.getBoolean(AppConstants.BundleExtras.IS_PAUSED);
        TextView artistNameTextView = (TextView) mView.findViewById(R.id.artist_name);
        TextView albumNameTextView = (TextView) mView.findViewById(R.id.album_name);
        mPreviewImageView = (ImageView) mView.findViewById(R.id.track_preview_image);
        mSongTextView = (TextView) mView.findViewById(R.id.song_name);
        mTimeElapsedTextView = (TextView) mView.findViewById(R.id.time_elapsed);
        mTimeRemainingTextView = (TextView) mView.findViewById(R.id.time_remaining);
        mArtistName = arguments.getString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA);
        artistNameTextView.setText(mArtistName);
        mTrackModelArrayList = arguments.getParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA);
        mSelectedItem = arguments.getInt(AppConstants.BundleExtras.TRACK_POSITION);
        albumNameTextView.setText(mTrackModelArrayList.get(mSelectedItem).getAlbumName());
        Picasso.with(getActivity()).load(mTrackModelArrayList.get(mSelectedItem).getLargeImage()).into(mPreviewImageView);
        mSongTextView.setText(mTrackModelArrayList.get(mSelectedItem).getTrackName());
        mPlayImageView = (ImageView) mView.findViewById(R.id.play);
        if (mIsPaused) {
            mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
            mCurrentId = android.R.drawable.ic_media_play;
        } else {
            mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
            mCurrentId = android.R.drawable.ic_media_pause;
        }
        ImageView previousImageView = (ImageView) mView.findViewById(R.id.previous);
        ImageView nextImageView = (ImageView) mView.findViewById(R.id.next);
        mSeekBar = (SeekBar) mView.findViewById(R.id.song_seek_bar);
        mPlayImageView.setOnClickListener(this);
        previousImageView.setOnClickListener(this);
        nextImageView.setOnClickListener(this);
        mSeekBar.setMax(30);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCurrentPostition=seekBar.getProgress();
                if (mPlayMusicService != null) {
                    mPlayMusicService.changeSongPosition(seekBar.getProgress(), mIsPaused);
                }
            }
        });
        Intent intent = new Intent(getActivity(), PlayMusicService.class);
        intent.putExtra(AppConstants.BundleExtras.PREVIEW_URL, mTrackModelArrayList.get(mSelectedItem).getPreview());
        if (Utils.isMyServiceRunning(PlayMusicService.class, getActivity()) && !mIsPlaying) {
            mCurrentPostition=0;
            Log.d(TAG, "service is running and mIsPlaying is false");
            getActivity().getApplicationContext().stopService(intent);
            getActivity().getApplicationContext().startService(intent);
        } else if (!Utils.isMyServiceRunning(PlayMusicService.class, getActivity())) {
            mCurrentPostition=0;
            Log.d(TAG, "service is not running");
            getActivity().getApplicationContext().startService(intent);
        }
        if (mPlayMusicService == null) {
            getActivity().getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }


    }

    private void manipulateActionBar(int flag) {
        if (flag == AppConstants.FLAGS.PHONE_FLAG) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.now_playing));
            actionBar.setSubtitle("");
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
                    mPlayMusicService.playSong(mIsPaused,mCurrentPostition);
                    mIsPaused = false;
                    Log.d(TAG, "mIsplaying in playbutton");
                    mIsPlaying = true;
                } else {
                    mPlayMusicService.pauseSong();
                    mIsPaused = true;
                    mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
                    mCurrentId = mPlayId;

                }
                break;
            case R.id.next:
                mSelectedItem = (mSelectedItem + 1) % mTrackModelArrayList.size();
                nextSong();
                break;
            case R.id.previous:
                Log.d(TAG, mSelectedItem + "");
                mSelectedItem = (mSelectedItem - 1);
                if (mSelectedItem < 0) {
                    mSelectedItem = mTrackModelArrayList.size() - 1;
                }
                nextSong();
                break;
            default:
                break;
        }
    }

    public void nextSong() {
        mSongTextView.setText(mTrackModelArrayList.get(mSelectedItem).getTrackName());
        Picasso.with(getActivity()).load(mTrackModelArrayList.get(mSelectedItem).getLargeImage()).into(mPreviewImageView);
        mPlayMusicService.setStreamURL(mTrackModelArrayList.get(mSelectedItem).getPreview());
        mIsPaused = false;
        mSeekBar.setProgress(0);
        mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
        mCurrentId = android.R.drawable.ic_media_pause;
        mPlayMusicService.stopUpdatingUI();
        mPlayMusicService.playSong(mIsPaused,0);
        mIsPlaying = true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayMusicService.LocalBinder binder = (PlayMusicService.LocalBinder) service;
            mPlayMusicService = binder.getService();
            mBound = true;
            if (!mIsPlaying) {
                mIsPlaying = true;
            }
            mPlayMusicService.setHandle(handler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        if (mPlayMusicService != null) {
            mPlayMusicService.stopUpdatingUI();
            if (mBound) {
                getActivity().getApplicationContext().unbindService(mConnection);
                mBound = false;
            }
            if (mIsPlaying) {
                mNowPlaying.updateNowPlaying(mTrackModelArrayList, mSelectedItem, mArtistName, mIsPaused,mCurrentPostition);
            }
        }
        if (!mIsPaused && !mIsPlaying) {
            getActivity().getApplicationContext().stopService(new Intent(getActivity(), PlayMusicService.class));
        }
    }

    final android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int trackPosition = msg.getData().getInt(AppConstants.BundleExtras.TRACK_POSITION);
            int trackDuration = msg.getData().getInt(AppConstants.BundleExtras.TRACK_DURATION);
            if (trackPosition <= trackDuration) {
                mCurrentPostition=trackPosition;
                mTimeElapsedTextView.setText(msg.getData().getString(AppConstants.BundleExtras.TRACK_CURRENT_TIME));
                mTimeRemainingTextView.setText(msg.getData().getString(AppConstants.BundleExtras.TRACK_REMAINING_TIME));
                mSeekBar.setProgress(trackPosition);
                if ((trackPosition ==
                        trackDuration) && trackDuration != 0) {
                    mIsPlaying = false;
                    mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
                    mCurrentId = android.R.drawable.ic_media_play;
                    mCurrentPostition=0;
                }
            }
        }
    };
}

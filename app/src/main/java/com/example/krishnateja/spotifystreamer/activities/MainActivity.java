package com.example.krishnateja.spotifystreamer.activities;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.krishnateja.spotifystreamer.Fragments.ArtistsFragment;
import com.example.krishnateja.spotifystreamer.Fragments.PreviewFragment;
import com.example.krishnateja.spotifystreamer.Fragments.TracksFragment;
import com.example.krishnateja.spotifystreamer.R;
import com.example.krishnateja.spotifystreamer.models.AppConstants;
import com.example.krishnateja.spotifystreamer.models.ArtistModel;
import com.example.krishnateja.spotifystreamer.models.TrackModel;
import com.example.krishnateja.spotifystreamer.services.PlayMusicService;
import com.example.krishnateja.spotifystreamer.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ArtistsFragment.PassArtistData, TracksFragment.PassTracksData, PreviewFragment.NowPlaying {

    private static final String ARTIST_TAG = "artist";
    private static final String TRACKS_TAG = "tracks";
    private static final String PREVIEW_TAG = "preview";
    private LinearLayout mLinearLayout;
    private ImageView mPreviewImageView;
    private TextView mAlbumTextView, mTrackTextView;
    private ImageView mPlayImageView;
    private PlayMusicService mPlayMusicService;
    private boolean mIsBound;
    private int mCurrentPlayButtonId;
    private boolean mIsPaused = false;
    private int mCurrentTrack;
    private int mCurrentDevice = -1;
    private String mCurrentArtistId;
    private int mCurrentPosition;
    private ArrayList<TrackModel> mTrackModelArrayList;
    private String mArtistName;
    private String TAG = MainActivity.class.getSimpleName();
    private boolean mIsSavedInstanceCalled = false;
    private ArrayList<ArtistModel> mArtistModelArrayList;
    private int mArtistSelected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIsSavedInstanceCalled = false;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initViewObjects();
        if (savedInstanceState != null) {
            mCurrentArtistId = savedInstanceState.getString(AppConstants.BundleExtras.ARTIST_ID_EXTRA);
            mCurrentDevice = savedInstanceState.getInt(AppConstants.BundleExtras.CURRENT_DEVICE);
            mIsBound = savedInstanceState.getBoolean(AppConstants.BundleExtras.IS_BOUND);
            mArtistModelArrayList = savedInstanceState.getParcelableArrayList(AppConstants.BundleExtras.ARTISTS_EXTRA);
            mArtistSelected = savedInstanceState.getInt(AppConstants.BundleExtras.CURRENT_ARTIST);
            if (mIsBound) {
                mTrackModelArrayList = savedInstanceState.getParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA);
                mCurrentTrack = savedInstanceState.getInt(AppConstants.BundleExtras.CURRENT_TRACK);
                mCurrentPosition = savedInstanceState.getInt(AppConstants.BundleExtras.TRACK_CURRENT_TIME);
                mIsPaused = savedInstanceState.getBoolean(AppConstants.BundleExtras.IS_PAUSED);
                mArtistName = savedInstanceState.getString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA);
                updateInfoBar(mTrackModelArrayList, mCurrentTrack, mArtistName, mIsPaused, mCurrentPosition);
            }
        } else {
            ArtistsFragment artistsFragment = (ArtistsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_artists);
            if (artistsFragment == null) {
                mCurrentDevice = AppConstants.FLAGS.PHONE_FLAG;
                artistsFragment = new ArtistsFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                        .beginTransaction();
                fragmentTransaction.replace(R.id.container_frame_layout, artistsFragment, ARTIST_TAG)
                        .commit();
            } else {
                mCurrentDevice = AppConstants.FLAGS.TABLET_FLAG;
            }
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                TracksFragment tracksFragment = getTracksFragment(mCurrentDevice);
                ArtistsFragment artistsFragment = getArtistsFragment(mCurrentDevice);
                if (tracksFragment != null && tracksFragment.isVisible()) {
                    if (mTrackModelArrayList != null) {
                        tracksFragment.restoreState(mTrackModelArrayList, mCurrentTrack, mArtistName);
                    }
                    tracksFragment.changeCurrentTrack(mCurrentTrack, true);
                    tracksFragment.manipulateActionBar(AppConstants.FLAGS.PHONE_FLAG);

                }
                if (artistsFragment != null && artistsFragment.isVisible()) {
                    artistsFragment.manipulateActionBar();
                    if (mArtistModelArrayList != null) {
                        artistsFragment.restoreState(mArtistModelArrayList, mArtistSelected);
                    }
                }
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mIsSavedInstanceCalled = true;
        Log.d(TAG, "onSavedInstanceState");
        outState.putBoolean(AppConstants.BundleExtras.IS_BOUND, mIsBound);
        outState.putInt(AppConstants.BundleExtras.CURRENT_TRACK, mCurrentTrack);
        outState.putInt(AppConstants.BundleExtras.TRACK_CURRENT_TIME, mCurrentPosition);
        outState.putInt(AppConstants.BundleExtras.CURRENT_PLAY_BUTTON_ID, mCurrentPlayButtonId);
        outState.putInt(AppConstants.BundleExtras.CURRENT_DEVICE, mCurrentDevice);
        outState.putString(AppConstants.BundleExtras.ARTIST_ID_EXTRA, mCurrentArtistId);
        outState.putBoolean(AppConstants.BundleExtras.IS_PAUSED, mIsPaused);
        outState.putString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA, mArtistName);
        outState.putParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA, mTrackModelArrayList);
        outState.putParcelableArrayList(AppConstants.BundleExtras.ARTISTS_EXTRA, mArtistModelArrayList);
        outState.putInt(AppConstants.BundleExtras.CURRENT_ARTIST, mArtistSelected);
        super.onSaveInstanceState(outState);
    }

    private void initViewObjects() {
        mPreviewImageView = (ImageView) findViewById(R.id.track_image_view);
        mAlbumTextView = (TextView) findViewById(R.id.album_name_text_view);
        mTrackTextView = (TextView) findViewById(R.id.track_name_text_view);
        mLinearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        mPlayImageView = (ImageView) findViewById(R.id.play);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void getArtistIdAndName(String id, String name) {
        ArtistsFragment artistsFragment = getArtistsFragment(mCurrentDevice);
        if (artistsFragment != null) {
            mArtistModelArrayList = artistsFragment.getArtists();
            mArtistSelected = artistsFragment.getArtistSelected();
        }
        if (id == null && name == null) {
            mCurrentArtistId = null;
        } else {
            TracksFragment tracksFragment = getTracksFragment(mCurrentDevice);
            if (tracksFragment == null) {
                tracksFragment = new TracksFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                        .beginTransaction();
                fragmentTransaction.replace(R.id.container_frame_layout, tracksFragment, TRACKS_TAG);
                fragmentTransaction.addToBackStack(ARTIST_TAG);
                fragmentTransaction.commit();
                getSupportFragmentManager().executePendingTransactions();
            }
            if (mCurrentArtistId == null
                    || !mCurrentArtistId.equals(id)) {
                tracksFragment.onLoadData(id, name, mCurrentDevice);
            } else if (mCurrentArtistId != null
                    && mCurrentArtistId.equals(id)
                    && mCurrentDevice == AppConstants.FLAGS.PHONE_FLAG) {
                if (mTrackModelArrayList != null) {
                    Log.d(TAG, "tracks not null");
                    tracksFragment.restoreState(mTrackModelArrayList, mCurrentTrack, mArtistName);
                }else{
                    tracksFragment.onLoadData(id,name,mCurrentDevice);
                }
            }
            mCurrentArtistId = id;
        }

    }

    @Override
    public void searchAgain() {
        TracksFragment tracksFragment = getTracksFragment(mCurrentDevice);
        mCurrentArtistId = "";
        if (mCurrentDevice == AppConstants.FLAGS.TABLET_FLAG) {
            tracksFragment.emptyTheList();
        }
    }

    @Override
    public void setTracksAndArtistName(ArrayList<TrackModel> trackModelArrayList, String artistName, int position) {
        if (trackModelArrayList == null && artistName == null && position == -1) {
            getSupportFragmentManager().popBackStack();
        } else {
            setUpPreviewFragment(trackModelArrayList, artistName, position, false, false);
        }
    }


    private void setUpPreviewFragment(ArrayList<TrackModel> trackModelArrayList, String artistName, int position, boolean isPlaying, boolean isPaused) {
        mTrackModelArrayList=trackModelArrayList;
        mArtistName=artistName;
        mCurrentTrack=position;
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA, trackModelArrayList);
        bundle.putString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA, artistName);
        bundle.putInt(AppConstants.BundleExtras.TRACK_POSITION, position);
        bundle.putBoolean(AppConstants.BundleExtras.IS_PLAYING, isPlaying);
        bundle.putBoolean(AppConstants.BundleExtras.IS_PAUSED, isPaused);
        FragmentManager fragmentManager = getSupportFragmentManager();
        PreviewFragment previewFragment = new PreviewFragment();
        if (mCurrentDevice == AppConstants.FLAGS.TABLET_FLAG) {
            bundle.putInt(AppConstants.BundleExtras.DEVICE, AppConstants.FLAGS.TABLET_FLAG);
            previewFragment.setArguments(bundle);
            previewFragment.show(fragmentManager, PREVIEW_TAG);
        } else {
            bundle.putInt(AppConstants.BundleExtras.DEVICE, AppConstants.FLAGS.PHONE_FLAG);
            previewFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_frame_layout, previewFragment, PREVIEW_TAG);
            fragmentTransaction.addToBackStack(TRACKS_TAG);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void removeInfoBar() {
        mLinearLayout.setVisibility(View.GONE);
        if (mIsBound) {
            getApplicationContext().unbindService(mServiceConnection);
            getApplicationContext().stopService(new Intent(this, PlayMusicService.class));
            mIsBound = false;
        }
    }

    @Override
    public void updateInfoBar(final ArrayList<TrackModel> trackModelArrayList, final int track, final String artistName, boolean isPaused, int currentPosition) {
        mCurrentPosition = currentPosition;
        mArtistName = artistName;
        mTrackModelArrayList = trackModelArrayList;
        TracksFragment tracksFragment = getTracksFragment(mCurrentDevice);
        if (tracksFragment != null) {
            tracksFragment.changeCurrentTrack(track, true);
        }
        mCurrentTrack = track;
        mIsPaused = isPaused;
        mAlbumTextView.setText(mTrackModelArrayList.get(track).getAlbumName());
        mTrackTextView.setText(mTrackModelArrayList.get(track).getTrackName());
        if (mIsPaused) {
            mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
            mCurrentPlayButtonId = android.R.drawable.ic_media_play;
        } else {
            mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
            mCurrentPlayButtonId = android.R.drawable.ic_media_pause;
        }
        Picasso.with(this).load(mTrackModelArrayList.get(track).getSmallImage()).into(mPreviewImageView);
        if (Utils.isMyServiceRunning(PlayMusicService.class, this) && !mIsSavedInstanceCalled) {
            getApplicationContext().bindService(new Intent(this, PlayMusicService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
        mLinearLayout.setVisibility(View.VISIBLE);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayMusicService != null) {
                    mPlayMusicService.stopUpdatingUI();
                }
                if (mIsBound) {
                    getApplicationContext().unbindService(mServiceConnection);
                    mIsBound = false;
                }
                mLinearLayout.setVisibility(View.GONE);
                setUpPreviewFragment(mTrackModelArrayList, mArtistName, track, true, mIsPaused);
            }
        });
        mPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPlayButtonId == android.R.drawable.ic_media_pause) {
                    mCurrentPlayButtonId = android.R.drawable.ic_media_play;
                    mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
                    mCurrentPosition = mPlayMusicService.pauseSong();
                    mPlayMusicService.stopUpdatingUI();
                    mIsPaused = true;
                } else {
                    mCurrentPlayButtonId = android.R.drawable.ic_media_pause;
                    mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
                    mPlayMusicService.playSong(mCurrentPosition);
                    mIsPaused = false;
                }

            }
        });
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!msg.getData().getBoolean(AppConstants.BundleExtras.IS_PLAYING) && !mIsPaused) {
                mLinearLayout.setVisibility(View.GONE);
                TracksFragment tracksFragment = getTracksFragment(mCurrentDevice);
                if (tracksFragment != null && tracksFragment.isVisible()) {
                    tracksFragment.changeCurrentTrack(-1, false);
                }
                if (mIsBound) {
                    getApplicationContext().unbindService(mServiceConnection);
                    mIsBound = false;
                }
                getApplicationContext().stopService(new Intent(MainActivity.this, PlayMusicService.class));

            }
        }
    };


    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayMusicService.LocalBinder binder = (PlayMusicService.LocalBinder) service;
            mPlayMusicService = binder.getService();
            mIsBound = true;
            mPlayMusicService.setHandle(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsBound) {
            getApplicationContext().unbindService(mServiceConnection);
            mIsBound = false;
        }
        if (mPlayMusicService != null) {
            mPlayMusicService.stopUpdatingUI();
        }
        if (isFinishing()) {
            if (Utils.isMyServiceRunning(PlayMusicService.class, this)) {
                stopService(new Intent(this, PlayMusicService.class));
            }
        }
    }

    public TracksFragment getTracksFragment(int currentDevice) {
        TracksFragment tracksFragment;
        if (currentDevice == AppConstants.FLAGS.PHONE_FLAG) {
            tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentByTag(TRACKS_TAG);
        } else {
            tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
        }
        return tracksFragment;
    }

    public ArtistsFragment getArtistsFragment(int currentDevice) {
        ArtistsFragment artistsFragment;
        if (currentDevice == AppConstants.FLAGS.PHONE_FLAG) {
            artistsFragment = (ArtistsFragment) getSupportFragmentManager().findFragmentByTag(ARTIST_TAG);
        } else {
            artistsFragment = (ArtistsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_artists);
        }
        return artistsFragment;
    }


}

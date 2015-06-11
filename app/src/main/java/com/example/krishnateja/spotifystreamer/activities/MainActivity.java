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
import android.support.v7.app.ActionBar;
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
import com.example.krishnateja.spotifystreamer.models.TrackModel;
import com.example.krishnateja.spotifystreamer.services.PlayMusicService;
import com.example.krishnateja.spotifystreamer.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ArtistsFragment.PassArtistData, TracksFragment.PassTracksData, PreviewFragment.NowPlaying {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ARTIST_TAG = "artist";
    private static final String TRACKS_TAG = "tracks";
    private static final String PREVIEW_TAG = "preview";
    private LinearLayout mLinearLayout;
    private ImageView mPreviewImageView;
    private TextView mAlbumTextView, mTrackTextView;
    private ImageView mPlayImageView;
    private PlayMusicService mPlayMusicService;
    private boolean mBound;
    private int mCurrentId;
    private boolean mIsPaused = false;
    private int mCurrentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initViewObjects();
        if (savedInstanceState == null) {
            ArtistsFragment artistsFragment = (ArtistsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_artists);
            if (artistsFragment == null) {
                artistsFragment = new ArtistsFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                        .beginTransaction();
                fragmentTransaction.replace(R.id.container_frame_layout, artistsFragment, ARTIST_TAG)
                        .commit();
            }
        }
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                TracksFragment tracksFragment=(TracksFragment)getSupportFragmentManager().findFragmentByTag(TRACKS_TAG);
                ArtistsFragment artistsFragment=(ArtistsFragment)getSupportFragmentManager().findFragmentByTag(ARTIST_TAG);
                if(tracksFragment!=null && tracksFragment.isVisible()){
                    tracksFragment.manipulateActionBar(AppConstants.FLAGS.PHONE_FLAG);
                }
                if(artistsFragment!=null && artistsFragment.isVisible()){
                    artistsFragment.manipulateActionBar();
                }

            }
        });
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

    public void manipulateActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setSubtitle("");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void getArtistIdAndName(String id, String name) {
        TracksFragment tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
        int deviceFlag;
        if (tracksFragment == null) {
            deviceFlag = AppConstants.FLAGS.PHONE_FLAG;
            tracksFragment = new TracksFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();
            fragmentTransaction.replace(R.id.container_frame_layout, tracksFragment, TRACKS_TAG);
            fragmentTransaction.addToBackStack(ARTIST_TAG);
            fragmentTransaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        } else {
            deviceFlag = AppConstants.FLAGS.TABLET_FLAG;
        }
        tracksFragment.onLoadData(id, name, deviceFlag);
    }

    @Override
    public void searchAgain() {
        TracksFragment tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
        if (tracksFragment != null) {
            tracksFragment.emptyTheList();
        }
        if (mBound) {
            removeTheInfoBar();
        }
    }

    private void removeTheInfoBar() {
        getApplicationContext().unbindService(mServiceConnection);
        mBound = false;
        getApplicationContext().stopService(new Intent(this, PlayMusicService.class));
        mLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public void getTracksAndArtistName(ArrayList<TrackModel> trackModelArrayList, String artistName, int position) {
        setUpPreviewFragment(trackModelArrayList, artistName, position, false, false);

    }

    private void setUpPreviewFragment(ArrayList<TrackModel> trackModelArrayList, String artistName, int position, boolean isPlaying, boolean isPaused) {
        TracksFragment tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA, trackModelArrayList);
        bundle.putString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA, artistName);
        bundle.putInt(AppConstants.BundleExtras.TRACK_POSITION, position);
        bundle.putBoolean(AppConstants.BundleExtras.IS_PLAYING, isPlaying);
        bundle.putBoolean(AppConstants.BundleExtras.IS_PAUSED, isPaused);
        FragmentManager fragmentManager = getSupportFragmentManager();
        PreviewFragment previewFragment = new PreviewFragment();
        if (tracksFragment != null) {
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
    public void removeNowPlaying() {
        Log.d(TAG,"removeNowPlaying->"+mBound);
        if (mBound) {
            getApplicationContext().unbindService(mServiceConnection);
            getApplicationContext().stopService(new Intent(this, PlayMusicService.class));
            mLinearLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateNowPlaying(final ArrayList<TrackModel> mTrackModelArrayList, final int position, final String artistName, boolean isPaused,int currentPosition) {
        mIsPaused = isPaused;
        mCurrentPosition=currentPosition;
        mAlbumTextView.setText(mTrackModelArrayList.get(position).getAlbumName());
        mTrackTextView.setText(mTrackModelArrayList.get(position).getTrackName());
        if (mIsPaused) {
            mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
            mCurrentId = android.R.drawable.ic_media_play;
        } else {
            mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
            mCurrentId = android.R.drawable.ic_media_pause;
        }
        Picasso.with(this).load(mTrackModelArrayList.get(position).getSmallImage()).into(mPreviewImageView);
        if (Utils.isMyServiceRunning(PlayMusicService.class, this)) {
            getApplicationContext().bindService(new Intent(this, PlayMusicService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
        mLinearLayout.setVisibility(View.VISIBLE);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    getApplicationContext().unbindService(mServiceConnection);
                    mBound = false;
                }
                mLinearLayout.setVisibility(View.GONE);
                setUpPreviewFragment(mTrackModelArrayList, artistName, position, true, mIsPaused);
            }
        });
        mPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentId == android.R.drawable.ic_media_pause) {
                    mCurrentId = android.R.drawable.ic_media_play;
                    mCurrentPosition=mPlayMusicService.pauseSong();
                    mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
                    mIsPaused = true;
                } else {
                    mCurrentId = android.R.drawable.ic_media_pause;
                    mPlayMusicService.playSong(true,mCurrentPosition);
                    mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
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
                if (mBound) {
                    getApplicationContext().unbindService(mServiceConnection);
                    mBound = false;
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
            mBound = true;
            mPlayMusicService.setHandle(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisConnected");
            mBound = false;
        }
    };


}

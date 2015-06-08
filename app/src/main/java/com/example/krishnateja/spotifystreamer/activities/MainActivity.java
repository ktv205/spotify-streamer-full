package com.example.krishnateja.spotifystreamer.activities;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import com.example.krishnateja.spotifystreamer.Fragments.ArtistsFragment;
import com.example.krishnateja.spotifystreamer.Fragments.PreviewFragment;
import com.example.krishnateja.spotifystreamer.Fragments.TracksFragment;
import com.example.krishnateja.spotifystreamer.R;
import com.example.krishnateja.spotifystreamer.models.AppConstants;
import com.example.krishnateja.spotifystreamer.models.TrackModel;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ArtistsFragment.PassArtistData,TracksFragment.PassTracksData {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ARTIST_TAG = "artist";
    private static final String TRACKS_TAG = "tracks";
    private static final String PREVIEW_TAG="preview";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                Log.d(TAG,"backstack changed");
                Log.d(TAG,"backstackcount->"+getSupportFragmentManager().getBackStackEntryCount());
                if(getSupportFragmentManager().getBackStackEntryCount()==0){
                    manipulateActionBar();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        Log.d(TAG,"id getId->"+id);
        //Log.d(TAG,"id R.id.HomeAsHome->"+android.R.id.homeAsUp);
        Log.d(TAG,"R.id.Home->"+android.R.id.home);
        Log.d(TAG, "onOptionsItemSelected");
        if(id==android.R.id.home){
            Log.d(TAG,"options menu home");
            getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    public void manipulateActionBar(){
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setSubtitle("");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void getArtistIdAndName(String id, String name) {
        TracksFragment tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
        Log.d(TAG, "getArtistIdANdName");
        int deviceFlag;
        if (tracksFragment == null) {
            deviceFlag= AppConstants.FLAGS.PHONE_FLAG;
            Log.d(TAG, "in null in getArtistIdAndName");
            tracksFragment = new TracksFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();
            fragmentTransaction.replace(R.id.container_frame_layout, tracksFragment, TRACKS_TAG);
            fragmentTransaction.addToBackStack(ARTIST_TAG);
            fragmentTransaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }else{
            deviceFlag=AppConstants.FLAGS.TABLET_FLAG;
        }
        tracksFragment.onLoadData(id, name, deviceFlag);
    }

    @Override
    public void searchAgain() {
        TracksFragment tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
        if(tracksFragment!=null){
             tracksFragment.emptyTheList();
        }
    }

    @Override
    public void getTracksAndArtistName(ArrayList<TrackModel> trackModelArrayList, String artistName,int position) {
        TracksFragment tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
        Bundle bundle=new Bundle();
        bundle.putParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA,trackModelArrayList);
        bundle.putString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA, artistName);
        bundle.putInt(AppConstants.BundleExtras.TRACK_POSITION,position);
        FragmentManager fragmentManager=getSupportFragmentManager();
        PreviewFragment previewFragment=new PreviewFragment();
        previewFragment.setArguments(bundle);
        if(tracksFragment!=null){
               previewFragment.show(fragmentManager,PREVIEW_TAG);
        }else{
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_frame_layout,previewFragment,TRACKS_TAG);
            fragmentTransaction.addToBackStack(TRACKS_TAG);
            fragmentTransaction.commit();
        }

    }


}

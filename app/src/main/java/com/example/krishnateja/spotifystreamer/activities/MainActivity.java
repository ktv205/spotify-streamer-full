package com.example.krishnateja.spotifystreamer.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.krishnateja.spotifystreamer.Fragments.ArtistsFragment;
import com.example.krishnateja.spotifystreamer.Fragments.LoadingFragment;
import com.example.krishnateja.spotifystreamer.Fragments.WelcomeFragment;
import com.example.krishnateja.spotifystreamer.R;
import com.example.krishnateja.spotifystreamer.models.AppConstants;
import com.example.krishnateja.spotifystreamer.models.ArtistModel;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String LOADING_TAG = "loading";
    private static final String WELCOME_TAG = "welcome";
    private static final String ARTIST_TAG = "artist";
    private EditText mSearchEditText;
    private String mSearchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mSearchQuery = mSearchEditText.getText().toString();
                    if (mSearchQuery.isEmpty()) {
                        mSearchEditText.setFocusable(true);
                        imm.showSoftInput(mSearchEditText, InputMethodManager.SHOW_IMPLICIT);
                    } else {
                        imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
                        searchForArtist();
                        addLoadingFragment();
                    }
                    return true;

                }
                return false;
            }
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_linear_layout, new WelcomeFragment(), WELCOME_TAG)
                    .commit();
        } else {
            mSearchEditText.setText(savedInstanceState.getString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mSearchEditText!=null) {
            outState.putString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA, mSearchQuery);
        }
        super.onSaveInstanceState(outState);
    }

    private void addLoadingFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container_linear_layout, new LoadingFragment(), LOADING_TAG).commit();
    }

    private void searchForArtist() {
        new SearchArtistAsyncTask().execute(mSearchQuery);

    }

    public class SearchArtistAsyncTask extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... params) {
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService apiService = spotifyApi.getService();
            ArtistsPager results = apiService.searchArtists(params[0]);
            return results;
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            List<Artist> artists = artistsPager.artists.items;
            if (artists.isEmpty()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container_linear_layout, new WelcomeFragment(), WELCOME_TAG)
                        .commit();
                mSearchEditText.setText("");
                Toast.makeText(MainActivity.this, "no artist found named " + mSearchQuery, Toast.LENGTH_SHORT).show();
            } else {
                ArrayList<ArtistModel> artistModelArrayList = new ArrayList<>();
                for (Artist artist : artists) {
                    ArtistModel artistModel = new ArtistModel();
                    Log.d(TAG, artist.uri);
                    Log.d(TAG, artist.name);
                    if (artist.images.size() > 0) {
                        artistModel.setImage(artist.images.get(1).url);
                    }else{
                        artistModel.setImage(null);
                    }
                    artistModel.setName(artist.name);
                    artistModel.setId(artist.id);
                    artistModelArrayList.add(artistModel);
                }
                ArtistsFragment artistFragment = new ArtistsFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(AppConstants.BundleExtras.ARTISTS_EXTRA, artistModelArrayList);
                artistFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.container_linear_layout, artistFragment, ARTIST_TAG).commit();
            }
        }
    }
}

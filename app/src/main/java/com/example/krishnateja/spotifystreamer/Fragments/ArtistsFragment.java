package com.example.krishnateja.spotifystreamer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.krishnateja.spotifystreamer.R;
import com.example.krishnateja.spotifystreamer.models.AppConstants;
import com.example.krishnateja.spotifystreamer.models.ArtistModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by krishnateja on 6/1/2015.
 */
public class ArtistsFragment extends Fragment {
    private static final String TAG = ArtistsFragment.class.getSimpleName();
    private ArrayList<ArtistModel> mArtistModelArrayList;
    private EditText mSearchArtistEditText;
    private String mSearchQuery;
    private View mView;
    private TextView mResultsTextView;
    private ProgressBar mLoadingProgressBar;
    private PassArtistData mPassData;
    private int mSelectedListItem = -1;

    public interface PassArtistData {
        public void getArtistIdAndName(String id, String name);
        public void searchAgain();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPassData = (PassArtistData) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreate View");
        manipulateActionBar();
        mView = inflater.inflate(R.layout.fragment_artists, container, false);
        mSearchArtistEditText = (EditText) mView.findViewById(R.id.search_edit_text);
        mResultsTextView = (TextView) mView.findViewById(R.id.results_text_view);
        mLoadingProgressBar = (ProgressBar) mView.findViewById(R.id.progress_bar);
        mSearchArtistEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Log.d(TAG, "here in action search");
                    mSearchQuery = mSearchArtistEditText.getText().toString();
                    if (mSearchQuery == null || mSearchQuery.isEmpty()) {
                        Log.d(TAG, "search query is empty");
                    } else {
                        imm.hideSoftInputFromWindow(mSearchArtistEditText.getWindowToken(), 0);
                        searchForArtist();
                        mPassData.searchAgain();
                    }
                }
                return false;

            }
        });
        if (savedInstanceState != null) {
            mArtistModelArrayList = savedInstanceState.getParcelableArrayList(AppConstants.BundleExtras.ARTISTS_EXTRA);
            mSearchQuery = savedInstanceState.getString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA);
        }
        if (mSearchQuery != null && !mSearchQuery.isEmpty()) {
            mSearchArtistEditText.setText(mSearchQuery);
        }
        if (mArtistModelArrayList != null) {
            Log.d(TAG, "mArtistModelArrayList not null");
            fillListView(mArtistModelArrayList);
        } else {
            mResultsTextView.setVisibility(View.VISIBLE);
            Log.d(TAG, "mArtistModelArrayList is nll");
        }
        return mView;
    }

    public void manipulateActionBar() {
        ActionBar actionBar=((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle("");
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onView state restored");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "in onResume");
    }

    private void searchForArtist() {
        mResultsTextView.setVisibility(View.GONE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        new SearchArtistAsyncTask().execute(mSearchQuery);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mArtistModelArrayList != null) {
            outState.putParcelableArrayList(AppConstants.BundleExtras.ARTISTS_EXTRA, mArtistModelArrayList);
        }
        if (mSearchQuery != null && !mSearchQuery.isEmpty()) {
            outState.putString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA, mSearchQuery);
        }
        super.onSaveInstanceState(outState);
    }

    public class ArtistListAdapter extends BaseAdapter {
        ArrayList<ArtistModel> mArtistModelArrayList;

        @Override
        public int getCount() {
            return mArtistModelArrayList.size();
        }

        public ArtistListAdapter(ArrayList<ArtistModel> artistModelArrayList) {
            mArtistModelArrayList = artistModelArrayList;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public class ViewHolder {
            TextView nameTextView;
            ImageView picImageView;

            public ViewHolder(View view) {
                nameTextView = (TextView) view.findViewById(R.id.artist_text_view);
                picImageView = (ImageView) view.findViewById(R.id.artist_image_view);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder;
            if (view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_artists, parent, false);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.nameTextView.setText(mArtistModelArrayList.get(position).getName());
            if (mArtistModelArrayList.get(position).getImage() != null) {
                Picasso.with(getActivity()).load(mArtistModelArrayList.get(position).getImage()).into(viewHolder.picImageView);
            } else {
                Log.d(TAG, "in pic null");
                viewHolder.picImageView.setImageResource(R.drawable.no_image);
            }
            return view;
        }
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
            mLoadingProgressBar.setVisibility(View.GONE);
            List<Artist> artists = artistsPager.artists.items;
            if (artists.isEmpty()) {
                mSearchArtistEditText.setText("");
                Toast.makeText(getActivity(), "no artist found named " + mSearchQuery, Toast.LENGTH_SHORT).show();
                fillListView(null);
            } else {
                ArrayList<ArtistModel> artistModelArrayList = new ArrayList<>();
                for (Artist artist : artists) {
                    ArtistModel artistModel = new ArtistModel();
                    Log.d(TAG, artist.uri);
                    Log.d(TAG, artist.name);
                    if (artist.images.size() > 0) {
                        if(artist.images.size()>1) {
                            artistModel.setImage(artist.images.get(1).url);
                        }else{
                            artistModel.setImage(artist.images.get(0).url);
                        }
                    } else {
                        artistModel.setImage(null);
                    }
                    artistModel.setName(artist.name);
                    artistModel.setId(artist.id);
                    artistModelArrayList.add(artistModel);
                }
                fillListView(artistModelArrayList);
            }
        }
    }

    private void fillListView(ArrayList<ArtistModel> artistModelArrayList) {
        mArtistModelArrayList = artistModelArrayList;
        if (artistModelArrayList == null) {
            artistModelArrayList = new ArrayList<>();
            mResultsTextView.setVisibility(View.VISIBLE);
        }
        ListView listView = (ListView) mView.findViewById(R.id.list_view);
        ArtistListAdapter artistListAdapter = new ArtistListAdapter(artistModelArrayList);
        listView.setAdapter(artistListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                Log.d(TAG,"position->"+position);
                mPassData.getArtistIdAndName(mArtistModelArrayList.get(position).getId(),
                        mArtistModelArrayList.get(position).getName());
            }
        });


    }
}

package com.example.krishnateja.spotifystreamer.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.krishnateja.spotifystreamer.R;
import com.example.krishnateja.spotifystreamer.models.AppConstants;
import com.example.krishnateja.spotifystreamer.models.TrackModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by krishnateja on 6/1/2015.
 */
public class TracksActivity extends AppCompatActivity {
    private static final String TAG = TracksActivity.class.getSimpleName();
    private ProgressBar mProgressBar;
    private ArrayList<TrackModel> mTrackModelArrayList;
    private String mArtistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        if (savedInstanceState == null) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            getArtistIdAndNameFromIntent();
        } else {
            mTrackModelArrayList = savedInstanceState.getParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA);
            if (mTrackModelArrayList == null) {
                getArtistIdAndNameFromIntent();
            } else {
                setTrackListAdapter();
            }
        }
        setUpActionBar();


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA, mTrackModelArrayList);
        super.onSaveInstanceState(outState);
    }

    public void getArtistIdAndNameFromIntent() {
        Intent intent = getIntent();
        mArtistName = intent.getStringExtra(AppConstants.BundleExtras.ARTIST_NAME_EXTRA);
        new TracksAyncTask().execute(intent.getStringExtra(AppConstants.BundleExtras.ARTIST_ID_EXTRA));
    }

    public void setTrackListAdapter() {
        ListView listView = (ListView) findViewById(R.id.list_view);
        TracksListAdapter tracksListAdapter = new TracksListAdapter(mTrackModelArrayList);
        listView.setAdapter(tracksListAdapter);
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setSubtitle(mArtistName);

    }

    public class TracksAyncTask extends AsyncTask<String, Void, Tracks> {

        @Override
        protected Tracks doInBackground(String... params) {
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService apiService = spotifyApi.getService();
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("country", "us");
            return apiService.getArtistTopTrack(params[0], queryParams);
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mTrackModelArrayList = new ArrayList<>();
            List<Track> trackList = tracks.tracks;
            for (Track track : trackList) {
                TrackModel trackModel = new TrackModel();
                trackModel.setTrackName(track.name);
                trackModel.setAlbumName(track.album.name);
                trackModel.setPreview(track.preview_url);
                if (track.album.images.size() > 0) {
                    trackModel.setLargeImage(track.album.images.get(0).url);
                    trackModel.setSmallImage(track.album.images.get(1).url);
                }
                mTrackModelArrayList.add(trackModel);
            }
            setTrackListAdapter();

        }
    }


    public class TracksListAdapter extends BaseAdapter {
        ArrayList<TrackModel> mTrackList;

        @Override
        public int getCount() {
            return mTrackList.size();
        }

        public TracksListAdapter(ArrayList<TrackModel> trackList) {
            mTrackList = trackList;
        }

        @Override
        public Object getItem(int position) {
            return mTrackList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public class ViewHolder {
            TextView trackNameTextView, albumNameTextView;
            ImageView picImageView;

            public ViewHolder(View view) {
                trackNameTextView = (TextView) view.findViewById(R.id.track_name_text_view);
                albumNameTextView = (TextView) view.findViewById(R.id.album_name_text_view);
                picImageView = (ImageView) view.findViewById(R.id.track_image_view);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder;
            if (view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_tracks, parent, false);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.trackNameTextView.setText(mTrackList.get(position).getTrackName());
            viewHolder.albumNameTextView.setText(mTrackList.get(position).getAlbumName());
            if (mTrackList.get(position).getSmallImage() != null) {
                Picasso.with(TracksActivity.this).load(mTrackList.get(position).getSmallImage()).into(viewHolder.picImageView);
            }
            return view;
        }
    }


}


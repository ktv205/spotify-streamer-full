package com.example.krishnateja.spotifystreamer.Fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
 * Created by krishnateja on 6/4/2015.
 */
public class TracksFragment extends Fragment {

    View mView;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private ArrayList<TrackModel> mTrackModelArrayList;
    private String mArtistName;
    private PassTracksData mPassTracksData;


    public interface PassTracksData {
        public void getTracksAndArtistName(ArrayList<TrackModel> trackModelArrayList, String artistName, int position);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPassTracksData = (PassTracksData) activity;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_tracks, container, false);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.progress_bar);
        mTextView = (TextView) mView.findViewById(R.id.results_text_view);

        if (savedInstanceState == null) {
            if (mTrackModelArrayList == null) {
                mTextView.setVisibility(View.VISIBLE);
            } else {
                setTrackListAdapter();
            }
        } else {
            mTrackModelArrayList = savedInstanceState.getParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA);
            setTrackListAdapter();
        }
        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mTrackModelArrayList != null) {
            outState.putParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA, mTrackModelArrayList);
        }
        super.onSaveInstanceState(outState);
    }


    public void manipulateActionBar(int flag) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (flag == AppConstants.FLAGS.PHONE_FLAG) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getActivity().getString(R.string.top_tracks));
        }
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

    public void setTrackListAdapter() {
        ListView listView = (ListView) mView.findViewById(R.id.list_view);
        if (mTrackModelArrayList.size() == 0) {
            mTextView.setVisibility(View.VISIBLE);
        }
        TracksListAdapter tracksListAdapter = new TracksListAdapter(mTrackModelArrayList);
        listView.setAdapter(tracksListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPassTracksData.getTracksAndArtistName(mTrackModelArrayList, mArtistName, position);
            }
        });
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
                Picasso.with(getActivity()).load(mTrackList.get(position).getSmallImage()).into(viewHolder.picImageView);
            }
            return view;
        }
    }

    public void onLoadData(String id, String name, int flag) {
        mTextView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mArtistName = name;
        new TracksAyncTask().execute(id);
        manipulateActionBar(flag);

    }

    public void emptyTheList() {
        mTrackModelArrayList = new ArrayList<>();
        setTrackListAdapter();
    }
}

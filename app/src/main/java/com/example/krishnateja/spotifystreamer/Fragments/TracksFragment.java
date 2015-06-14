package com.example.krishnateja.spotifystreamer.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.krishnateja.spotifystreamer.R;
import com.example.krishnateja.spotifystreamer.models.AppConstants;
import com.example.krishnateja.spotifystreamer.models.TrackModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.client.Response;

/**
 * Created by krishnateja on 6/4/2015.
 */
public class TracksFragment extends Fragment {

    View mView;
    private ProgressBar mLoadingProgressBar;
    private TextView mResultsTextView;
    private ArrayList<TrackModel> mTrackModelArrayList;
    private String mArtistName;
    private PassTracksData mPassTracksData;
    private ListView mListView;
    private int mTrackSelected = -1;
    private String TAG = TracksFragment.class.getSimpleName();


    public interface PassTracksData {
        void setTracksAndArtistName(ArrayList<TrackModel> trackModelArrayList, String artistName, int position);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mPassTracksData = (PassTracksData) activity;
        } catch (Exception e) {
            Log.d(TAG, e.toString() + "MainActivity should inplement PassTracksData interface");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_tracks, container, false);
        mLoadingProgressBar = (ProgressBar) mView.findViewById(R.id.progress_bar);
        mResultsTextView = (TextView) mView.findViewById(R.id.results_text_view);

        if (savedInstanceState != null) {
            mTrackModelArrayList = savedInstanceState.getParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA);
            mTrackSelected = savedInstanceState.getInt(AppConstants.BundleExtras.CURRENT_TRACK);
            mArtistName = savedInstanceState.getString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA);
        }
        if (mTrackModelArrayList == null) {
            mResultsTextView.setVisibility(View.VISIBLE);
        } else {
            setTrackListAdapter();
        }

        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(AppConstants.BundleExtras.TRACKS_EXTRA, mTrackModelArrayList);
        outState.putInt(AppConstants.BundleExtras.CURRENT_TRACK, mTrackSelected);
        outState.putString(AppConstants.BundleExtras.ARTIST_NAME_EXTRA, mArtistName);
        super.onSaveInstanceState(outState);
    }

    public void manipulateActionBar(int flag) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (flag == AppConstants.FLAGS.PHONE_FLAG) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(getActivity().getString(R.string.top_tracks));
            }
            actionBar.setSubtitle(mArtistName);
        }
    }

    public void changeCurrentTrack(int position, boolean isSelected) {
        if (isSelected && position != mTrackSelected) {
            if (mListView != null) {
                mListView.smoothScrollToPosition(position);
                mTrackSelected = position;
            }
        } else if (!isSelected) {
            if (mListView != null) {
                setViewSelected(false, mTrackSelected);
                mTrackSelected = -1;
            }
        }
    }

    public void setTrackListAdapter() {
        mListView = (ListView) mView.findViewById(R.id.list_view);
        if (mTrackModelArrayList.size() == 0) {
            mResultsTextView.setVisibility(View.VISIBLE);
        }
        TracksListAdapter tracksListAdapter = new TracksListAdapter(mTrackModelArrayList);
        mListView.setAdapter(tracksListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setViewSelected(false, mTrackSelected);
                setViewSelected(true, position);
                mTrackSelected = position;
                mPassTracksData.setTracksAndArtistName(mTrackModelArrayList, mArtistName, position);
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem <= mTrackSelected && mTrackSelected <= firstVisibleItem + visibleItemCount) {

                    setViewSelected(true, mTrackSelected);
                }
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

    public void onLoadData(String id, String name, int deviceFlag) {
        mTrackSelected = -1;
        mResultsTextView.setVisibility(View.GONE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mArtistName = name;
        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService apiService = spotifyApi.getService();
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("country", "us");
        manipulateActionBar(deviceFlag);
        apiService.getArtistTopTrack(id, queryParams, new SpotifyCallback<Tracks>() {
            android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

            @Override
            public void failure(SpotifyError spotifyError) {
                handler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Please check your network connection and try again", Toast.LENGTH_SHORT).show();
                                mLoadingProgressBar.setVisibility(View.GONE);
                                mResultsTextView.setVisibility(View.VISIBLE);
                                mPassTracksData.setTracksAndArtistName(null, null, -1);
                            }
                        }
                );
            }

            @Override
            public void success(final Tracks tracks, Response response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingProgressBar.setVisibility(ProgressBar.INVISIBLE);
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
                });
            }
        });
    }

    public void emptyTheList() {
        mTrackSelected = -1;
        mTrackModelArrayList = new ArrayList<>();
        setTrackListAdapter();
    }

    public void setViewSelected(boolean isSelected, int pos) {
        if (mListView != null) {
            View view = mListView.getChildAt(pos - mListView.getFirstVisiblePosition());
            if (view != null) {
                view.setSelected(isSelected);
            }
        }
    }
}

package com.example.krishnateja.spotifystreamer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
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
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.client.Response;

/**
 * Created by krishnateja on 6/1/2015.
 */
public class ArtistsFragment extends Fragment {
    private ArrayList<ArtistModel> mArtistModelArrayList;
    private EditText mSearchArtistEditText;
    private String mSearchQuery;
    private View mView;
    private TextView mResultsTextView;
    private ProgressBar mLoadingProgressBar;
    private PassArtistData mPassData;
    private ListView mListView;
    private int mArtistSelected = -1;

    public interface PassArtistData {
        void getArtistIdAndName(String id, String name);

        void searchAgain();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPassData = (PassArtistData) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mListView != null) {
            mListView.setSelection(mArtistSelected);
            mListView.setItemChecked(mArtistSelected, true);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                    String query = mSearchArtistEditText.getText().toString();
                    if (!query.isEmpty()
                            && (mSearchQuery == null || !mSearchQuery.equals(query))) {
                        mSearchQuery = query;
                        manipulateActionBar();
                        mArtistSelected = -1;
                        searchForArtist();
                        mPassData.searchAgain();
                    }
                    imm.hideSoftInputFromWindow(mSearchArtistEditText.getWindowToken(), 0);
                }
                return false;

            }
        });
        if (mSearchQuery != null && !mSearchQuery.isEmpty()) {
            mSearchArtistEditText.setText(mSearchQuery);
        }
        if (mArtistModelArrayList != null) {
            fillListView(mArtistModelArrayList);
        } else {
            mResultsTextView.setVisibility(View.VISIBLE);
        }
        return mView;
    }

    public void manipulateActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle("");
            actionBar.setTitle(getString(R.string.app_name));
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }


    private void searchForArtist() {
        mResultsTextView.setVisibility(View.GONE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService apiService = spotifyApi.getService();
        apiService.searchArtists(mSearchQuery, new SpotifyCallback<ArtistsPager>() {
            Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void failure(SpotifyError spotifyError) {
                handler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Please check your network connection and try again", Toast.LENGTH_SHORT).show();
                                mLoadingProgressBar.setVisibility(View.GONE);
                                mResultsTextView.setVisibility(View.VISIBLE);
                                mSearchQuery = "";
                                if (mListView != null) {
                                    fillListView(new ArrayList<ArtistModel>());
                                }
                                mPassData.getArtistIdAndName(null,null);
                            }
                        }
                );
            }

            @Override
            public void success(final ArtistsPager artistsPager, Response response) {
                handler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                mLoadingProgressBar.setVisibility(View.GONE);
                                parseArtistsPager(artistsPager);
                            }
                        }
                );
            }
        });
    }

    private void parseArtistsPager(ArtistsPager artistsPager) {
        mLoadingProgressBar.setVisibility(View.GONE);
        List<Artist> artists = artistsPager.artists.items;
        if (artists != null && artists.isEmpty()) {
            mSearchArtistEditText.setText("");
            Toast.makeText(getActivity(), "no artist found named " + mSearchQuery, Toast.LENGTH_SHORT).show();
            fillListView(null);
        } else {
            ArrayList<ArtistModel> artistModelArrayList = new ArrayList<>();
            for (Artist artist : artists) {
                ArtistModel artistModel = new ArtistModel();
                if (artist.images.size() > 0) {
                    if (artist.images.size() > 1) {
                        artistModel.setImage(artist.images.get(1).url);
                    } else {
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
                viewHolder.picImageView.setImageResource(R.drawable.no_image);
            }
            return view;
        }
    }


    private void fillListView(ArrayList<ArtistModel> artistModelArrayList) {
        mArtistModelArrayList = artistModelArrayList;
        if (artistModelArrayList == null) {
            artistModelArrayList = new ArrayList<>();
            mResultsTextView.setVisibility(View.VISIBLE);
        }
        mListView = (ListView) mView.findViewById(R.id.list_view);
        ArtistListAdapter artistListAdapter = new ArtistListAdapter(artistModelArrayList);
        mListView.setAdapter(artistListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View lastView = mListView.getChildAt(mArtistSelected - mListView.getFirstVisiblePosition());
                if (lastView != null) {
                    lastView.setSelected(false);
                }

                mArtistSelected = position;
                view.setSelected(true);
                mPassData.getArtistIdAndName(mArtistModelArrayList.get(position).getId(),
                        mArtistModelArrayList.get(position).getName());
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem <= mArtistSelected && mArtistSelected <= firstVisibleItem + visibleItemCount) {
                    View itemView = mListView.getChildAt(mArtistSelected - firstVisibleItem);
                    if (itemView != null) {
                        itemView.setSelected(true);
                    }
                }

            }
        });
    }
}

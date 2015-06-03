package com.example.krishnateja.spotifystreamer.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.krishnateja.spotifystreamer.R;
import com.example.krishnateja.spotifystreamer.activities.MainActivity;
import com.example.krishnateja.spotifystreamer.activities.TracksActivity;
import com.example.krishnateja.spotifystreamer.models.AppConstants;
import com.example.krishnateja.spotifystreamer.models.ArtistModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by krishnateja on 6/1/2015.
 */
public class ArtistsFragment extends Fragment {


    private static final String TAG =ArtistsFragment.class.getSimpleName() ;
    private ArrayList<ArtistModel> mArtistModelArrayList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.list_view);
        if (savedInstanceState == null) {
            mArtistModelArrayList = getArguments().getParcelableArrayList(AppConstants.BundleExtras.ARTISTS_EXTRA);
        } else {
            mArtistModelArrayList = savedInstanceState.getParcelableArrayList(AppConstants.BundleExtras.ARTISTS_EXTRA);
        }

        ArtistListAdapter artistListAdapter = new ArtistListAdapter(mArtistModelArrayList);
        listView.setAdapter(artistListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TracksActivity.class);
                intent.putExtra(AppConstants.BundleExtras.ARTIST_ID_EXTRA, mArtistModelArrayList.get(position).getId());
                intent.putExtra(AppConstants.BundleExtras.ARTIST_NAME_EXTRA, mArtistModelArrayList.get(position).getName());
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(AppConstants.BundleExtras.ARTISTS_EXTRA, mArtistModelArrayList);
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
}

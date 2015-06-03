package com.example.krishnateja.spotifystreamer.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.krishnateja.spotifystreamer.R;

/**
 * Created by krishnateja on 6/1/2015.
 */
public class LoadingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading,container,false);
    }
}

package com.example.etaka;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class FragmentLoading extends Fragment {
    ImageView imageView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_loading, container, false);
        imageView = (ImageView) root.findViewById(R.id.loadGif);
        Glide.with(root.getContext()).load(R.drawable.loading).into(imageView);
        return root;
    }
}
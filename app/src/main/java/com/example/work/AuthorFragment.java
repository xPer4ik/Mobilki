package com.example.work;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AuthorFragment extends DialogFragment {

    public AuthorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_author, container, false);

        // Находжение ImageView и применение к нему анимации
        ImageView imageView = view.findViewById(R.id.imageViewAuthor);
        @SuppressLint("ResourceType") Animation slideAnimation = AnimationUtils.loadAnimation(getContext(), R.transition.slide);
        imageView.startAnimation(slideAnimation);

        return view;
    }
}
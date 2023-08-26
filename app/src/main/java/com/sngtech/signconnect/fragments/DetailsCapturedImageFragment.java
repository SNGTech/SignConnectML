package com.sngtech.signconnect.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sngtech.signconnect.databinding.FragmentDetailsCapturedImageBinding;

public class DetailsCapturedImageFragment extends Fragment {

    FragmentDetailsCapturedImageBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsCapturedImageBinding.inflate(getLayoutInflater());

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();

        String path = bundle.getString("capturedPath");
        Log.println(Log.INFO, "file_signconnect", "Saved image file at: " + path);
        Bitmap bitmapBuffer = BitmapFactory.decodeFile(path);
        binding.capturedImageView.setImageBitmap(bitmapBuffer);

        if(bundle.getInt("facing") == 0) {
            binding.capturedImageView.setScaleX(-1.0f);
        }
    }
}
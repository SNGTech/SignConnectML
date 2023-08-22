package com.sngtech.signconnect.fragments;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sngtech.signconnect.databinding.FragmentDetailsCapturedVideoBinding;

import java.io.IOException;

public class DetailsCapturedVideoFragment extends Fragment implements TextureView.SurfaceTextureListener {

    FragmentDetailsCapturedVideoBinding binding;
    TextureView videoView;
    Bundle bundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsCapturedVideoBinding.inflate(getLayoutInflater());

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bundle = this.getArguments();
        int facing = bundle.getInt("facing");
        videoView = binding.capturedVideoView;
        videoView.setSurfaceTextureListener(this);
        if(facing == 0) // Flip front facing video
            videoView.setScaleX(-1f);
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        String path = bundle.getString("capturedPath");
        Log.println(Log.INFO, "file_signconnect", "Saved video file at: " + path);
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setSurface(new Surface(surface));
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        videoView.setOnClickListener(ignore -> {
            mediaPlayer.start();
        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
}
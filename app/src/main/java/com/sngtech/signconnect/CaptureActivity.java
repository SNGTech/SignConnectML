package com.sngtech.signconnect;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.sngtech.signconnect.databinding.ActivityCaptureBinding;
import com.sngtech.signconnect.fragments.LetterCameraFragment;
import com.sngtech.signconnect.fragments.WordsCameraFragment;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class CaptureActivity extends AppCompatActivity {

    private ActivityCaptureBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Camera Fragment cause Static Fragment in XML refuses to work
        replaceFragment(new LetterCameraFragment());

        binding = ActivityCaptureBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.capture_letters) {
                replaceFragment(new LetterCameraFragment());
            } else if (id == R.id.capture_words) {
                replaceFragment(new WordsCameraFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_fade_out,
                    R.anim.slide_pop_in,
                    R.anim.slide_fade_out_pop
            )
            .setReorderingAllowed(true)
            .replace(R.id.fragment_camera_container, fragment)
            .commit();
    }
}

package com.sngtech.signconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sngtech.signconnect.databinding.ActivityMainBinding;
import com.sngtech.signconnect.fragments.LetterCameraFragment;
import com.sngtech.signconnect.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Start at Home Fragment
        //replaceFragment(new HomeFragment());

        binding.bottomNavMenu.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if(id == R.id.history) {
                switchActivity(HistoryActivity.class);
            }
            else if(id == R.id.capture) {
                switchActivity(CaptureActivity.class);
            }
            else if(id == R.id.logout) {
                switchActivity(LoginActivity.class);
            }

            return true;
        });
    }

//    private void replaceFragment(Fragment fragment) {
//        getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(
//                        R.anim.slide_in,
//                        R.anim.slide_fade_out,
//                        R.anim.slide_pop_in,
//                        R.anim.slide_fade_out_pop
//                )
//                .setReorderingAllowed(true)
//                .replace(R.id.fragment_container, fragment)
//                .commit();
//    }

    private void switchActivity(Class<?> activityClass) {
        Intent newIntent = new Intent(getApplicationContext(), activityClass);
        startActivity(newIntent);
    }
}
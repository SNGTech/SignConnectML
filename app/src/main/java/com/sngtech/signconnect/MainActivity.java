package com.sngtech.signconnect;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sngtech.signconnect.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase Authentication Check
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null) {
            Log.println(Log.INFO, "firebase", "No user available");
            finishAffinity();
            switchActivity(LoginActivity.class);
            return;
        }
        Log.println(Log.INFO, "firebase", "Have user available");

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
                mAuth.signOut();
                Toast.makeText(getApplicationContext(), "Logged out!", Toast.LENGTH_SHORT).show();
                finishAffinity();
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
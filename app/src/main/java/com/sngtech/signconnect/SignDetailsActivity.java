package com.sngtech.signconnect;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.sngtech.signconnect.databinding.ActivitySignDetailsBinding;
import com.sngtech.signconnect.fragments.DetailsCapturedImageFragment;
import com.sngtech.signconnect.fragments.DetailsCapturedVideoFragment;

import java.util.Locale;

public class SignDetailsActivity extends AppCompatActivity {

    private ActivitySignDetailsBinding binding;
    TextToSpeech tts;

    String capturedPath;
    int facing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Bundle detailsBundle = getIntent().getExtras();

        String signType = detailsBundle.getString("signType");
        String typeText = signType.equals("L") ? "Letter: " : "Word: ";
        String resultText = detailsBundle.getString("result");
        String resultFullText = typeText + resultText;
        binding.capturedResult.setText(resultFullText);

        String datetimeText = "Captured on: " + detailsBundle.getString("datetime");
        binding.datetime.setText(datetimeText);

        capturedPath = detailsBundle.getString("capturedPath");
        facing = detailsBundle.getInt("facing");

        tts = new TextToSpeech(getApplicationContext(), status -> {
            if(status == TextToSpeech.ERROR) {
                Toast.makeText(getApplicationContext(), "Unknown Error: Failed to activate Text to Speech!", Toast.LENGTH_SHORT).show();
                return;
            }

            tts.setLanguage(Locale.ENGLISH);
        });

        binding.btnTTS.setOnClickListener(ignore -> {
            tts.speak(resultText, TextToSpeech.QUEUE_FLUSH, null, resultText + "_tts");
        });

        if(signType.equals("L")) {
            binding.capturedSubtitle.setText("Captured Image");
            replaceFragment(new DetailsCapturedImageFragment());
        }
        else if(signType.equals("W")) {
            binding.capturedSubtitle.setText("Captured Video");
            replaceFragment(new DetailsCapturedVideoFragment());
        }
    }

    private void replaceFragment(Fragment fragment) {
        Bundle pathBundle = new Bundle();
        pathBundle.putString("capturedPath", capturedPath);
        pathBundle.putInt("facing", facing);
        fragment.setArguments(pathBundle);
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.media_container, fragment)
                .commit();
    }
}

package com.sngtech.signconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.sngtech.signconnect.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.signupLink.setOnClickListener(ignore -> {
            Intent newIntent =new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(newIntent);
        });

        binding.button.setOnClickListener(ignore -> {
            // TEMPORARY
            if(SignUpActivity.email.equals(binding.editTextTextEmailAddress.getText().toString())
            && SignUpActivity.password.equals(binding.editTextTextPassword.getText().toString())) {
                Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(newIntent);
            } else {
                Toast.makeText(getApplicationContext(), "Invalid email and/or password!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
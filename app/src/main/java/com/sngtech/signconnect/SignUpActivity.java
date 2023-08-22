package com.sngtech.signconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sngtech.signconnect.databinding.ActivityLoginBinding;
import com.sngtech.signconnect.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    // TEMPORARY UNTIL FIREBASE IS IMPLEMENTED
    public static String email = "§";
    public static String password = "§";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.loginLink.setOnClickListener(ignore -> {
            Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(newIntent);
        });

        binding.button.setOnClickListener(ignore -> {
            email = binding.editTextTextEmailAddress.getText().toString();
            password = binding.editTextTextPassword.getText().toString();
            Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(newIntent);
            Toast.makeText(getApplicationContext(), "Sign up successful!", Toast.LENGTH_SHORT).show();
        });
    }
}
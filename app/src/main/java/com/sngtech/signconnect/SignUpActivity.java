package com.sngtech.signconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.sngtech.signconnect.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.loginLink.setOnClickListener(ignore -> {
            Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(newIntent);
        });

        binding.button.setOnClickListener(ignore -> {
            String email = binding.editTextTextEmailAddress.getText().toString();
            String password = binding.editTextTextPassword.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Log.d("firebaseAuth", "createUserWithEmail:success");

                        Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(newIntent);
                        Toast.makeText(getApplicationContext(), "Sign up successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w("firebaseAuth", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Account signup failed. Please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}
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
import com.sngtech.signconnect.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.signupLink.setOnClickListener(ignore -> {
            Intent newIntent =new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(newIntent);
        });

        binding.button.setOnClickListener(ignore -> {
            String email = binding.editTextTextEmailAddress.getText().toString();
            String password = binding.editTextTextPassword.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Log.d("firebaseAuth", "loginUserWithEmail:success");

                        Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                        finishAffinity();
                        startActivity(newIntent);
                        Toast.makeText(getApplicationContext(), "Log in successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w("firebaseAuth", "loginUserWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Account login failed. Please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // TEMPORARY
//            if(SignUpActivity.email.equals(binding.editTextTextEmailAddress.getText().toString())
//            && SignUpActivity.password.equals(binding.editTextTextPassword.getText().toString())) {
//                Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(newIntent);
//            } else {
//                Toast.makeText(getApplicationContext(), "Invalid email and/or password!", Toast.LENGTH_SHORT).show();
//            }
        });
    }
}
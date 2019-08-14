package com.example.android.swill;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by DAKONY on 11/6/2018.
 */

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEmail, etPass, etConfirmPass;
    private Button SignUp;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPass = (EditText) findViewById(R.id.etPass);
        etConfirmPass = (EditText) findViewById(R.id.etConfirmPass);
        SignUp = (Button) findViewById(R.id.btn_SignUp);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        SignUp.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            LoginInUser();
        }
    }

    private void LoginInUser() {
        Intent intent = new Intent(SignUpActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v == SignUp){
            Register();
        }
    }

    private void Register() {
        final String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString().trim();
        final String comfirmpassword = etConfirmPass.getText().toString().trim();

        if(email.isEmpty()){
            etEmail.setError("Please email cannot be Empty");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please Provide a Valid Email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()){
            etPass.setError("Please Password Cannot be Empty");
            etPass.requestFocus();
            return;
        }
        if (comfirmpassword.isEmpty()){
            etConfirmPass.setError("Please Confirm Password");
            etConfirmPass.requestFocus();
            return;
        }
        else if (!password.equals(comfirmpassword)){
            Toast.makeText(this,"Password Doesn't match",Toast.LENGTH_SHORT).show();
        }


        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            SendUserToSetupActivity();
                            Toast.makeText(getApplicationContext(), getString(R.string.Registration),Toast.LENGTH_LONG).show();
                        }else {

                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });

    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(SignUpActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}

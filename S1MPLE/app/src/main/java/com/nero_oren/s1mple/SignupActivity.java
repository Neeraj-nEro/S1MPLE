package com.nero_oren.s1mple;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignupActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;
    private EditText RegisterUserName;
    private EditText RegisterUserEmail;
    private EditText RegisterUserPassword;
    private Button CreateAccountButton;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mToolbar = (Toolbar) findViewById(R.id.Sign_up_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        RegisterUserName = (EditText) findViewById(R.id.register_name);
        RegisterUserEmail = (EditText) findViewById(R.id.register_email);
        RegisterUserPassword = (EditText) findViewById(R.id.register_password);
        CreateAccountButton = (Button) findViewById(R.id.sign_up_button);
        loadingBar = new ProgressDialog(this);
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = RegisterUserName.getText().toString();
                String email = RegisterUserEmail.getText().toString();
                String password = RegisterUserPassword.getText().toString();
                RegisterAccount(name, email, password);

            }
        });

    }


    private void RegisterAccount(final String name, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(SignupActivity.this, "Please write your name", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(SignupActivity.this, "Please write your email", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(SignupActivity.this, "Please write your password", Toast.LENGTH_LONG).show();
        } else {
            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("please wait");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String DeviceToken = FirebaseInstanceId.getInstance().getToken();
                        String current_user_id = mAuth.getCurrentUser().getUid();
                        storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
                        storeUserDefaultDataReference.child("user_name").setValue(name);
                        storeUserDefaultDataReference.child("user_status").setValue("I m LovIng it , nEro is AwEsoMe  !!");
                        storeUserDefaultDataReference.child("user_image").setValue("default_pic");
                        storeUserDefaultDataReference.child("device_token").setValue(DeviceToken);
                        storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent miintent = new Intent(SignupActivity.this, MainActivity.class);
                                            miintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(miintent);
                                            finish();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(SignupActivity.this, "error", Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                }
            });

        }
    }
}
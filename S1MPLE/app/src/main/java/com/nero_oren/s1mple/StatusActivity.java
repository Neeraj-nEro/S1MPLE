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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button SaveChangeButton;
    private EditText StatusInput;
    private ProgressDialog loadingbar;

    private DatabaseReference changeStatusReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        changeStatusReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mToolbar = (Toolbar)findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SaveChangeButton = (Button)findViewById(R.id.status_change_button);
        StatusInput = (EditText)findViewById(R.id.status_input);
        loadingbar = new ProgressDialog(this);

        String old_status = getIntent().getExtras().get("user_status").toString();
        StatusInput.setText(old_status);

        SaveChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String new_status = StatusInput.getText().toString();
                ChangeProfileStatus(new_status);
            }
        });
    }

    private void ChangeProfileStatus(String new_status) {
        if(TextUtils.isEmpty(new_status)){
            Toast.makeText(StatusActivity.this, "Please Write Your Status", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingbar.setTitle("Updating Status");
            loadingbar.setMessage("Please Wait");
            loadingbar.show();
            changeStatusReference.child("user_status").setValue(new_status)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                loadingbar.dismiss();
                                Intent profileIntent = new Intent(StatusActivity.this, ProfileActivity.class);
                                startActivity(profileIntent);

                                Toast.makeText(StatusActivity.this, "Status updated Sucessfully",Toast.LENGTH_LONG ).show();
                            }
                            else {
                                Toast.makeText(StatusActivity.this, "Error Occured",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}

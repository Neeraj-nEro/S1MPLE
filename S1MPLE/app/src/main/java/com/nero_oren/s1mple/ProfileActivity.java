package com.nero_oren.s1mple;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {


    private CircleImageView settingDisplaypic;
    private TextView DisplayName;
    private TextView DispalyStatus;
    private Button ChangePic;
    private Button ChangeStatus;
    private final static int Gallery_Pick = 1;
    private StorageReference StoreProfile;
    private Toolbar mToolbar;

    private DatabaseReference getUserDataReference;
    private FirebaseAuth mAuth;

    Bitmap thumb_bitmap = null;

    private StorageReference thumbimagereference;
    private ProgressDialog loading_bar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        String online_user_id = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataReference.keepSynced(true);

        StoreProfile = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        thumbimagereference = FirebaseStorage.getInstance().getReference().child("thumb_Images");

        mToolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.profile_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingDisplaypic = (CircleImageView)findViewById(R.id.setting_user_image);
        DisplayName = (TextView)findViewById(R.id.setting_user_name);
        DispalyStatus = (TextView)findViewById(R.id.setting_user_status);
        ChangePic = (Button)findViewById(R.id.setting_change_pic);
        ChangeStatus = (Button)findViewById(R.id.setting_change_status);
        loading_bar = new ProgressDialog(this);

        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();
                String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                DisplayName.setText(name);
                DispalyStatus.setText(status);

                if(!image.equals("default_pic")){
   //                 Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_pic).into(settingDisplaypic);
                    Picasso.with(ProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_pic).into(settingDisplaypic, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_pic).into(settingDisplaypic);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ChangePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        ChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String old_status = DispalyStatus.getText().toString();
                Intent statusIntent = new Intent(ProfileActivity.this, StatusActivity.class);
                statusIntent.putExtra("user_status", old_status);
                startActivity(statusIntent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null) {
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loading_bar.setTitle("Updating Profile Image");
                loading_bar.setMessage("Please wait");
                loading_bar.show();

                Uri resultUri = result.getUri();
                File thumb_filepathUri = new File(resultUri.getPath());
                String user_id = mAuth.getCurrentUser().getUid();

                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(250)
                            .setMaxHeight(250)
                            .setQuality(50)
                            .compressToBitmap(thumb_filepathUri);
                }
                catch (IOException e){
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                StorageReference filepath = StoreProfile.child(user_id + ".jpg");

                final StorageReference thumb_filepath = thumbimagereference.child( user_id + ".jpg");



                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ProfileActivity.this,
                                    "Uploading image ..." , Toast.LENGTH_LONG).show();
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);


                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(task.isSuccessful()){
                                        Map update_user_data = new HashMap();
                                        update_user_data.put("user_image", downloadUrl);
                                        update_user_data.put("user_thumb_image", thumb_downloadUrl);


                                        getUserDataReference.updateChildren(update_user_data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(ProfileActivity.this,
                                                                "Profile Image Updated Sucessfully " , Toast.LENGTH_LONG).show();
                                                        loading_bar.dismiss();
                                                    }
                                                });

                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(ProfileActivity.this,
                                    "Error Occured, while uploading image", Toast.LENGTH_SHORT).show();
                            loading_bar.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}

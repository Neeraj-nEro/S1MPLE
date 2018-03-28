package com.nero_oren.s1mple;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class TimelineActivity extends AppCompatActivity {

    private Button SendFriendRequest;
    private Button DeclineFriendRequest;
    private TextView profileName;
    private TextView profileStatus;
    private ImageView profileImage;

    private DatabaseReference UserReference;

    private String CURRENT_STATE;
    private DatabaseReference FriendRequestReference;
    private FirebaseAuth mAuth;
    String send_user_id;
    String receiver_user_id;


    private DatabaseReference FriendReference;
    private DatabaseReference NotificationReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendRequestReference.keepSynced(true);

        NotificationReference = FirebaseDatabase.getInstance().getReference().child("Notificaations");
        NotificationReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        send_user_id = mAuth.getCurrentUser().getUid();

        FriendReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendReference.keepSynced(true);

        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");

        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();

        SendFriendRequest = (Button) findViewById(R.id.profile_visit_send_req);
        DeclineFriendRequest = (Button) findViewById(R.id.profile_visit_decline_req);
        profileName = (TextView) findViewById(R.id.profile_visit_user_name);
        profileStatus = (TextView) findViewById(R.id.profile_visit_status);
        profileImage = (ImageView) findViewById(R.id.profilr_visit_user_image);

        CURRENT_STATE = "not_friends";
        UserReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);
                Picasso.with(TimelineActivity.this).load(image).placeholder(R.drawable.default_pic).into(profileImage);


                FriendRequestReference.child(send_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(receiver_user_id)){
                                    String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();
                                    if(req_type.equals("sent")){
                                        CURRENT_STATE = "request_sent";
                                        SendFriendRequest.setText("Cancel Friend Request");

                                        DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                        DeclineFriendRequest.setEnabled(false);
                                    }
                                    else if(req_type.equals("received")){
                                        CURRENT_STATE = "request_received";
                                        SendFriendRequest.setText("Accept Friend Request");

                                        DeclineFriendRequest.setVisibility(View.VISIBLE);
                                        DeclineFriendRequest.setEnabled(true);

                                        DeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                DeclineFriendRequestofaPerson();
                                            }
                                        });
                                    }
                                }
                                else{
                                    FriendReference.child(send_user_id)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(receiver_user_id)){
                                                        CURRENT_STATE = "friends";
                                                        SendFriendRequest.setText("Unfriend This Person");

                                                        DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                        DeclineFriendRequest.setEnabled(false);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DeclineFriendRequest.setVisibility(View.INVISIBLE);
        DeclineFriendRequest.setEnabled(false);

        if(!send_user_id.equals(receiver_user_id)){
            SendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendFriendRequest.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequestToaFriend();
                    }

                    if(CURRENT_STATE.equals("request_sent")){
                        cancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends")){
                        UnFriendaFriend();
                    }
                }
            });
        }
        else{
            DeclineFriendRequest.setVisibility(View.INVISIBLE);
            SendFriendRequest.setVisibility(View.INVISIBLE) ;
        }
    }

    private void DeclineFriendRequestofaPerson() {
        FriendRequestReference.child(send_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(send_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequest.setText("Send Friend Request");

                                                DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void UnFriendaFriend() {
        FriendReference.child(send_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendReference.child(receiver_user_id).child(send_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequest.setText("Send Friend Request");

                                                DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendReference.child(send_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendReference.child(receiver_user_id).child(send_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FriendRequestReference.child(send_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FriendRequestReference.child(receiver_user_id).child(send_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                SendFriendRequest.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                SendFriendRequest.setText("Unfriend this Person");

                                                                                DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                                                DeclineFriendRequest.setEnabled(false);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });

    }

    private void cancelFriendRequest() {
            FriendRequestReference.child(send_user_id).child(receiver_user_id).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FriendRequestReference.child(receiver_user_id).child(send_user_id).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    SendFriendRequest.setEnabled(true);
                                                    CURRENT_STATE = "not_friends";
                                                    SendFriendRequest.setText("Send Friend Request");

                                                    DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                    DeclineFriendRequest.setEnabled(false);
                                                }
                                            }
                                        });
                            }
                        }
                    });
    }

    private void SendFriendRequestToaFriend() {
        FriendRequestReference.child(send_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            FriendRequestReference.child(receiver_user_id).child(send_user_id)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                HashMap<String , String> notificationData = new HashMap<String ,String >();
                                                notificationData.put("from", send_user_id);
                                                notificationData.put("type", "request");
                                                NotificationReference.child(receiver_user_id).push().setValue(notificationData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    SendFriendRequest.setEnabled(true);
                                                                    CURRENT_STATE = "request_sent";
                                                                    SendFriendRequest.setText("Cancel Friend Request");

                                                                    DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                                    DeclineFriendRequest.setEnabled(false);
                                                                }
                                                            }
                                                        });


                                            }
                                        }
                                    });
                        }
                    }
                });
    }

}

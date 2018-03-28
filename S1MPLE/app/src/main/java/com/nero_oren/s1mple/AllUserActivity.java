package com.nero_oren.s1mple;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;

public class AllUserActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView allUserList;
    private DatabaseReference alluserreference;
    private EditText SearchInputText;
    private ImageButton SearchButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        mToolbar = (Toolbar)findViewById(R.id.all_users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SearchButton = (ImageButton) findViewById(R.id.search_people_button);
        SearchInputText = (EditText) findViewById(R.id.search_input_text);



        allUserList = (RecyclerView)findViewById(R.id.all_user_list);
        allUserList.setHasFixedSize(true);
        allUserList.setLayoutManager(new LinearLayoutManager(this));

        alluserreference = FirebaseDatabase.getInstance().getReference().child("Users");
        alluserreference.keepSynced(true);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchUserName = SearchInputText.getText().toString();
                if(TextUtils.isEmpty(searchUserName)){
                    Toast.makeText(AllUserActivity.this, "Please weite user name to search.", Toast.LENGTH_SHORT).show();
                }
                SearchForPeople(searchUserName);
            }
        });

    }


    private void SearchForPeople(String searchUserName){

        Toast.makeText(AllUserActivity.this, "Searching...", Toast.LENGTH_SHORT).show();

        Query searchpeople = alluserreference.orderByChild("user_name")
                .startAt(searchUserName).endAt(searchUserName + "\uf8ff");

        FirebaseRecyclerAdapter<Allusers, AllUserViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Allusers, AllUserViewHolder>
                (
                        Allusers.class,
                        R.layout.all_user_display_layout,
                        AllUserViewHolder.class,
                        searchpeople
                ) {
            @Override
            protected void populateViewHolder(AllUserViewHolder viewHolder, Allusers model, final int position) {
                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_image(getApplicationContext(), model.getUser_thumb_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(AllUserActivity.this, TimelineActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        allUserList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class AllUserViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public AllUserViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUser_name(String user_name){
            TextView name = (TextView) mView.findViewById(R.id.all_users_username);
            name.setText(user_name);
        }

        public void setUser_status(String user_status){
            TextView status = (TextView) mView.findViewById(R.id.all_user_status);
            status.setText(user_status);
        }

        public void setUser_thumb_image(final Context ctx, final String user_thumb_image){
            final CircleImageView thumb_image = (CircleImageView)mView.findViewById(R.id.all_user_profile_image);

            Picasso.with(ctx).load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_pic)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(user_thumb_image).placeholder(R.drawable.default_pic).into(thumb_image);
                        }
                    });

        }
    }
}

package com.nero_oren.s1mple;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager myViewPager;
    private TabLayout myTablayout;
    private TabsPagerAdapter myTabsPagerAdapter;
    FirebaseUser current_user;
    private DatabaseReference UsersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        current_user = mAuth.getCurrentUser();

        if(current_user != null){
            String online_user_id = mAuth.getCurrentUser().getUid();
            UsersReference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(online_user_id);
        }
        myViewPager = (ViewPager)findViewById(R.id.main_tabs_pager);
        myTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsPagerAdapter);
        myTablayout = (TabLayout)findViewById(R.id.main_tab);
        myTablayout.setupWithViewPager(myViewPager);

        mToolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("S1MPLE");
    }

    @Override
    protected void onStart() {
        super.onStart();

        current_user = mAuth.getCurrentUser();

        if(current_user == null){
            LogOutUser();
        }
        else if(current_user != null){
            UsersReference.child("online").setValue("true");

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(current_user != null){
            UsersReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void LogOutUser() {
        Intent loginintent = new Intent(MainActivity.this, LoginActivity.class);
        loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginintent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_button){

            if(current_user != null){
                UsersReference.child("online").setValue(ServerValue.TIMESTAMP);
            }
            mAuth.signOut();
            LogOutUser();
        }
        if(item.getItemId() == R.id.profile_setting_button){
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        }
        if(item.getItemId() == R.id.all_user_button){
            Intent alluserIntent = new Intent(MainActivity.this, AllUserActivity.class);
            startActivity(alluserIntent);
        }
        return true;
    }
}

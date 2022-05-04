package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleObserver;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.whatsappclone.sendNotificationCode.Token;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagePage extends AppCompatActivity{
    Toolbar mainPageToolBar;
    ViewPager viewPager;
    SectionsPagerAdapter sectionsPagerAdapter;
    TabLayout tabLayout;
    SharedPreferences pref;
    DatabaseReference ref;
    String UID;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_page);

        mainPageToolBar = findViewById(R.id.mainAppBar);
        viewPager = findViewById(R.id.tabPager);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        pref = getSharedPreferences("UID",MODE_APPEND);
        UID = pref.getString("phoneNumber","");
        ref = FirebaseDatabase.getInstance().getReference().child("users").child(UID);
        ref.keepSynced(true);
        setSupportActionBar(mainPageToolBar);
        getSupportActionBar().setTitle("We App");

        updateToken();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_page,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.logOut){
             ref.child("token").setValue(null);
             Intent intentLogOut = new Intent(MessagePage.this,Login.class);
             intentLogOut.putExtra("check",1);
             startActivity(intentLogOut);
             finish();
         }
         if(item.getItemId()==R.id.settings){
             Intent intentSettings = new Intent(MessagePage.this,Settings.class);
             startActivity(intentSettings);
         }
         if(item.getItemId()==R.id.searchUser){
             Intent intentUsers = new Intent(MessagePage.this,AllUsers.class);
             startActivity(intentUsers);
         }
         return true;
    }

    public void updateToken() {
        String refreshId = FirebaseInstanceId.getInstance().getToken();
        Token token = new Token(refreshId);
        Map updateHashMap = new HashMap<>();
        updateHashMap.put("token",token.getToken());
        ref.updateChildren(updateHashMap);
    }
}
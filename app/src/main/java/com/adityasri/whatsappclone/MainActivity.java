package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Animation topAnim,bottomAnim;
    TextView title,subTitle;
    ImageView logo;
    SharedPreferences pref;
    DatabaseReference ref;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_anim);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_anim);

        title = findViewById(R.id.title);
        subTitle = findViewById(R.id.subTitle);
        logo = findViewById(R.id.logo);

        logo.setAnimation(topAnim);
        title.setAnimation(bottomAnim);
        subTitle.setAnimation(bottomAnim);


        pref = getSharedPreferences("MyPref",MODE_APPEND); //local storage

        final String phoneNumberLogin = pref.getString("phoneNumber", "");
        final String passwordLogin = pref.getString("password","");

        ref = FirebaseDatabase.getInstance().getReference("users");
        ref.keepSynced(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ref.child(phoneNumberLogin).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            if(phoneNumberLogin.equals("")||passwordLogin.equals("")){
                                Intent intent = new Intent(MainActivity.this,Login.class);
                                Pair[] pairs = new Pair[3];
                                pairs[0] = new Pair<View,String>(logo,"imageTrans");
                                pairs[1] = new Pair<View,String>(title,"textTrans");
                                pairs[2] = new Pair<View,String>(subTitle,"textTrans");
                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pairs);
                                startActivity(intent,options.toBundle());
                                supportFinishAfterTransition();
                            }
                            else{
                                Intent intent = new Intent(MainActivity.this,MessagePage.class);
                                startActivity(intent);
                                finish();
                            }
                        }else{
                            Intent intent = new Intent(MainActivity.this,Login.class);
                            Pair[] pairs = new Pair[3];
                            pairs[0] = new Pair<View,String>(logo,"imageTrans");
                            pairs[1] = new Pair<View,String>(title,"textTrans");
                            pairs[2] = new Pair<View,String>(subTitle,"textTrans");
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pairs);
                            startActivity(intent,options.toBundle());
                            supportFinishAfterTransition();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            }
        },3000);
        }
}
package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappclone.sendNotificationCode.Token;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class Login extends AppCompatActivity {
    Button signUp,login,forgetPass;
    TextView title,subTitle;
    TextInputLayout phoneNumber,password;
    Pair pairs[] = new Pair[7];
    ActivityOptions options;
    Intent intent;
    String phoneNumberText,passwordText;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    CheckBox rememberMe;
    CipherClass cipherClass;
    DatabaseReference ref;

    public boolean validatePhoneNumber(String val){
        if(val.isEmpty()){
            phoneNumber.setError("Field cannot be left Empty");
            return false;
        }
        else{
            phoneNumber.setError(null);
            phoneNumber.setErrorEnabled(false);
            return true;
        }
    }

    public boolean validatePassword(String val){

        if(val.isEmpty()){
            password.setError("Field cannot be left Empty");
            return false;
        }
        else{
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    public void logIn(View view){

        phoneNumberText = phoneNumber.getEditText().getText().toString();
        passwordText = password.getEditText().getText().toString();
        boolean validatePhoneNumber = validatePhoneNumber(phoneNumberText);
        boolean validatePassword = validatePassword(passwordText);

        if(validatePassword&&validatePhoneNumber){
            isUser();
        }
    }

    public void isUser(){

        ref = FirebaseDatabase.getInstance().getReference("users");
        ref.keepSynced(true);
        Query checkUser = ref.orderByChild("phoneNumber").equalTo(phoneNumberText);
        checkUser.keepSynced(true);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    phoneNumber.setError(null);
                    phoneNumber.setErrorEnabled(false);
                    String passwordFromDB = snapshot.child(phoneNumberText).child("password").getValue(String.class);
                    String decryptedPassword = cipherClass.decrypt(passwordFromDB);
                    if(decryptedPassword.equals(passwordText)){
                        password.setError(null);
                        password.setErrorEnabled(false);
                        String name = snapshot.child(phoneNumberText).child("name").getValue(String.class);
                        Toast.makeText(Login.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();

                        if(rememberMe.isChecked()){
                            editor.putString("phoneNumber",phoneNumberText);
                            editor.putString("password",passwordText);
                        }
                        else{
                            editor.clear();
                        }

                        editor.commit();
                        SharedPreferences preferences = getSharedPreferences("UID",0);
                        SharedPreferences.Editor editorUID = preferences.edit();
                        editorUID.putString("phoneNumber",phoneNumberText);
                        editorUID.commit();
                        updateToken();
                        Intent intent = new Intent(Login.this,MessagePage.class);
                        startActivity(intent);
                        finish();
                    }else{
                        password.setError("Password Incorrect!");
                        password.requestFocus();
                    }
                }else{
                    phoneNumber.setError("User Does Not Exist!");
                    phoneNumber.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login.this, "Error! Not able to be Logged In", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void updateToken() {
        String refreshId = FirebaseInstanceId.getInstance().getToken();
        Token token = new Token(refreshId);
        Map updateHashMap = new HashMap<>();
        updateHashMap.put("token",token.getToken());
        ref.child(phoneNumberText).updateChildren(updateHashMap);
    }

    public void signUp(View view){
        startActivity(intent,options.toBundle());
        finish();
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signUp = findViewById(R.id.signUp);
        login = findViewById(R.id.login);
        forgetPass = findViewById(R.id.forgetPass);
        title = findViewById(R.id.title);
        subTitle = findViewById(R.id.subTitle);
        phoneNumber = findViewById(R.id.phoneno);
        password = findViewById(R.id.password);
        intent = new Intent(Login.this,SignUp.class);
        cipherClass = new CipherClass();

        int check = getIntent().getIntExtra("check",-1);

        pairs[0] = new Pair<View,String>(signUp,"signUpTrans");
        pairs[1] = new Pair<View,String>(login,"signUpTrans");
        pairs[2] = new Pair<View,String>(forgetPass,"signUpTrans");
        pairs[3] = new Pair<View,String>(title,"textTrans");
        pairs[4] = new Pair<View,String>(subTitle,"signUpTrans");
        pairs[5] = new Pair<View,String>(phoneNumber,"signUpTrans");
        pairs[6] = new Pair<View,String>(password,"signUpTrans");
        options = ActivityOptions.makeSceneTransitionAnimation(Login.this,pairs);
        pref = getApplicationContext().getSharedPreferences("MyPref",MODE_APPEND);
        rememberMe = findViewById(R.id.rememberMe);
        editor = pref.edit();


        String phoneNumberLogin = pref.getString("phoneNumber", "");
        String passwordLogin = pref.getString("password","");
        phoneNumber.getEditText().setText(phoneNumberLogin);
        password.getEditText().setText(passwordLogin);
        if(phoneNumber.getEditText().getText().toString().length()==0||password.getEditText().getText().length()==0){
            rememberMe.setChecked(false);
        }
        else{
            rememberMe.setChecked(true);
        }
    }

    public void forgetPassword(View view){
        Intent intentFPass = new Intent(Login.this, ForgetPasswordPage.class);
        startActivity(intentFPass);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ref = FirebaseDatabase.getInstance().getReference("users");
        phoneNumberText = phoneNumber.getEditText().getText().toString();
        Map updateStatus = new HashMap();
        updateStatus.put("online",false);
        updateStatus.put("lastSeen", ServerValue.TIMESTAMP);
        ref.child(phoneNumberText).updateChildren(updateStatus);
    }

    @Override
    public void onPause() {
        super.onPause();
        ref.child(phoneNumberText).child("online").setValue(false);
        ref.child(phoneNumberText).child("lastSeen").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void onResume() {
        super.onResume();
        ref.child(phoneNumberText).child("online").setValue(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        ref.child(phoneNumberText).child("online").setValue(false);
        ref.child(phoneNumberText).child("lastSeen").setValue(ServerValue.TIMESTAMP);
    }
}
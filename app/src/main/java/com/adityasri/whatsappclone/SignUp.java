package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {
    TextInputLayout name,email,phoneNumber,password;
    Button register,login;
    TextView title;
    ActivityOptions options;
    Intent intent;
    CipherClass cipherClass;
    Pair pairs[] = new Pair[7];
    String nameText,passwordText,phoneNumberText,emailText;
    String encryptPasswordText = null;
    boolean validateName,validatePhone,validatePassword,validateEmail;
    DatabaseReference ref;

    public boolean validateName(String val){
        if(val.isEmpty()){
            name.setError("Field cannot be left Empty");
            return false;
        }
        else{
            name.setError(null);
            name.setErrorEnabled(false);
            return true;
        }
    }
    public boolean validatePassword(String val){
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";
        if(val.isEmpty()){
            password.setError("Field cannot be left Empty");
            return false;
        }else if (!val.matches(passwordVal)) {
            password.setError("Password is too weak");
            return false;
        }
        else{
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }
    public boolean validateEmail(String val){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(val.isEmpty()){
            email.setError("Field cannot be left Empty");
            return false;
        }else if (!val.matches(emailPattern)) {
            email.setError("Invalid email address");
            return false;
        }
        else{
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }
    public boolean validatePhoneNumber(String val){
        if(val.isEmpty()){
            phoneNumber.setError("Field cannot be left Empty");
            return false;
        }
        else if(val.length()!=10){
            phoneNumber.setError("Number not of 10 Digits");
            return false;
        }
        else{
            phoneNumber.setError(null);
            phoneNumber.setErrorEnabled(false);
            return true;
        }
    }
    public void isUser(){

        Query checkUser = ref.orderByChild("phoneNumber").equalTo(phoneNumberText);
        checkUser.keepSynced(true);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(SignUp.this, "User Already Exists!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    try {
                        encryptPasswordText = cipherClass.encrypt(passwordText);
                    } catch (Exception e) {
                        Toast.makeText(SignUp.this, "Error in encrypting the password!", Toast.LENGTH_LONG).show();
                    }

                    Intent intentOTP = new Intent(SignUp.this,OTPVerification.class);
                    intentOTP.putExtra("phoneNumber",phoneNumberText);
                    intentOTP.putExtra("name",nameText);
                    intentOTP.putExtra("email",emailText);
                    intentOTP.putExtra("password",encryptPasswordText);
                    intentOTP.putExtra("counter",1);
                    startActivity(intentOTP);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUp.this, "Error in checking through Database! Try Again!", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void registerInfo(View view){

        nameText = name.getEditText().getText().toString();
        emailText = email.getEditText().getText().toString();
        phoneNumberText = phoneNumber.getEditText().getText().toString();
        passwordText = password.getEditText().getText().toString();

        validateName = validateName(nameText);
        validatePhone = validatePhoneNumber(phoneNumberText);
        validateEmail = validateEmail(emailText);
        validatePassword = validatePassword(passwordText);

        if(validateEmail&&validateName&&validatePassword&&validatePhone){
            isUser();
        }
    }
    public void gotoLogin(View view){
        startActivity(intent,options.toBundle());
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        name = findViewById(R.id.fullName);
        email = findViewById(R.id.emailid);
        phoneNumber = findViewById(R.id.phoneNumber);
        password = findViewById(R.id.pass);
        register = findViewById(R.id.register);
        login = findViewById(R.id.login);
        title = findViewById(R.id.title);
        intent = new Intent(SignUp.this,Login.class);
        cipherClass = new CipherClass();
        ref = FirebaseDatabase.getInstance().getReference("users");
        ref.keepSynced(true);

        pairs[0] = new Pair<View,String>(name,"signUpTrans");
        pairs[1] = new Pair<View,String>(email,"signUpTrans");
        pairs[2] = new Pair<View,String>(phoneNumber,"signUpTrans");
        pairs[3] = new Pair<View,String>(password,"signUpTrans");
        pairs[4] = new Pair<View,String>(register,"signUpTrans");
        pairs[5] = new Pair<View,String>(login,"signUpTrans");
        pairs[6] = new Pair<View,String>(title,"textTrans");
        options = ActivityOptions.makeSceneTransitionAnimation(SignUp.this,pairs);

    }

}
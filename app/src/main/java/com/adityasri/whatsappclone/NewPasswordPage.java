package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class NewPasswordPage extends AppCompatActivity {
TextInputLayout newPassword,reNewPassword;
String newPasswordText,reNewPasswordText;
String phoneNumberText;
DatabaseReference ref;
Query checkUser;
CipherClass cipherClass;
Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password_page);
        newPassword = findViewById(R.id.newPassword);
        reNewPassword = findViewById(R.id.reNewPassword);
        toolbar = findViewById(R.id.forgetPasswordBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Forget Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        phoneNumberText = getIntent().getStringExtra("phoneNumber");
        ref = FirebaseDatabase.getInstance().getReference("users");
        checkUser = ref.orderByChild("phoneNumber").equalTo(phoneNumberText);
        cipherClass = new CipherClass();
    }

    public void confirm(View view){
        newPasswordText = newPassword.getEditText().getText().toString();
        reNewPasswordText = reNewPassword.getEditText().getText().toString();
        checkSamePassword();
    }
    private void checkSamePassword() {
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";
        if(!newPasswordText.equals(reNewPasswordText)){
            newPassword.setError("Password Do Not Match");
        }else if(newPasswordText.isEmpty()||reNewPasswordText.isEmpty()){
            newPassword.setError("Field Cannot be Left Empty");
            reNewPassword.setError("Field Cannot be Left Empty");
        }else if (!newPasswordText.matches(passwordVal)) {
            newPassword.setError("Password is too Weak");
        }else{
            newPassword.setError(null);
            newPassword.setErrorEnabled(false);
            reNewPassword.setError(null);
            reNewPassword.setErrorEnabled(false);
            update();
        }
    }

    private void update() {
        String encryptedPassword = cipherClass.encrypt(newPasswordText);
        ref.child(phoneNumberText).child("password").setValue(encryptedPassword);
        Toast.makeText(this, "Password Has Been Updated!", Toast.LENGTH_SHORT).show();
        Intent intent_new = new Intent(NewPasswordPage.this,Login.class);
        intent_new.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent_new);
    }

}
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

public class ForgetPasswordPage extends AppCompatActivity {
TextInputLayout phoneNumber;
String phoneNumberText;
Toolbar toolbar;
DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password_page);
        phoneNumber = findViewById(R.id.phoneNumber);
        ref = FirebaseDatabase.getInstance().getReference("users");
        toolbar = findViewById(R.id.forgetPasswordBar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Forget Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void continueBtn(View view){
        checkPhoneNumber();
    }

    private void checkPhoneNumber() {
        phoneNumber.setErrorEnabled(true);
        phoneNumberText = phoneNumber.getEditText().getText().toString();
        if(phoneNumberText.length()!=10){
            phoneNumber.setError("Number not of 10 digits");
        }
        else if(phoneNumberText.isEmpty()){
            phoneNumber.setError("Field Cannot be Left Empty");
        }
        else{

            Query checkUser = ref.orderByChild("phoneNumber").equalTo(phoneNumberText);
            checkUser.keepSynced(true);
            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        phoneNumber.setError(null);
                        phoneNumber.setErrorEnabled(false);
                        Intent intent = new Intent(ForgetPasswordPage.this,OTPVerification.class);
                        intent.putExtra("phoneNumber",phoneNumberText);
                        intent.putExtra("email","");
                        intent.putExtra("name","");
                        intent.putExtra("password","");
                        intent.putExtra("counter",2);
                        startActivity(intent);
                    }else{
                        phoneNumber.setErrorEnabled(true);
                        phoneNumber.setError("No Such User Exists!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ForgetPasswordPage.this, "Error! Not able to retrieve Data!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.concurrent.TimeUnit;

public class OTPVerification extends AppCompatActivity {

    TextView timer;
    Button verifyButton,resendButton;
    TextInputLayout otp;
    ProgressBar progressBar;
    String codeBySystem;
    private FirebaseAuth mAuth;
    String phoneNumberText,emailText,nameText,passwordText;
    DatabaseReference myRef;
    Intent intent;
    int counter;
    CountDownTimer countDownTimer;
    int SECONDS = 120;
    long TIME_IN_MILLI_SECONDS = SECONDS * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p_verification);

        verifyButton = findViewById(R.id.verifyBtn);
        resendButton = findViewById(R.id.retryBtn);
        otp = findViewById(R.id.OTPText);
        progressBar = findViewById(R.id.Progress);
        mAuth = FirebaseAuth.getInstance();

        phoneNumberText = getIntent().getStringExtra("phoneNumber");
        emailText = getIntent().getStringExtra("email");
        nameText = getIntent().getStringExtra("name");
        passwordText = getIntent().getStringExtra("password");
        counter = getIntent().getIntExtra("counter",0);

        myRef = FirebaseDatabase.getInstance().getReference("users");
        myRef.keepSynced(true);

        intent = new Intent(OTPVerification.this,Login.class);
        timer = findViewById(R.id.timer);
        updateTimer(SECONDS); //120 seconds
        resendButton.setVisibility(View.INVISIBLE);

        countDownTime();

        progressBar.setVisibility(View.INVISIBLE);
        sendVerificationCode(phoneNumberText);
    }

    private void countDownTime() {
        countDownTimer = new CountDownTimer(TIME_IN_MILLI_SECONDS, 1000) {

            @Override
            public void onTick(long l) {
                updateTimer((int) l / 1000);
            }

            @Override
            public void onFinish() {
                resendButton.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void updateTimer(int secondsLeft) {
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - (minutes * 60);

        String secondString = Integer.toString(seconds);
        String minutesString = Integer.toString(minutes);

        if (seconds <= 9) {
            secondString = "0" + secondString;
        }
        if (minutes <= 9) {
            minutesString = "0" + minutesString;
        }

        timer.setText(minutesString + ":" + secondString);
    }

    private void sendVerificationCode(String phoneNumberText) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+phoneNumberText)       // Phone number to verify
                        .setTimeout(TIME_IN_MILLI_SECONDS, TimeUnit.MILLISECONDS) // Timeout and unit
                        .setActivity(OTPVerification.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(OTPVerification.this, "Verification Failed! Enter Correct OTP", Toast.LENGTH_LONG).show();
        }
    };

    private void verifyCode(String code) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credentials = PhoneAuthProvider.getCredential(codeBySystem,code);
        signInUser(credentials);
    }

    private void signInUser(PhoneAuthCredential credentials) {

        mAuth.signInWithCredential(credentials)
        .addOnCompleteListener(OTPVerification.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                 if(task.isSuccessful()){
                     if(counter==1){
                            UserHelperClass userHelperClass = new UserHelperClass(nameText,phoneNumberText,emailText,passwordText,"default_dp.png","Hi There! I am using We App","default_dp.png");
                            myRef.child(phoneNumberText).setValue(userHelperClass,new DatabaseReference.CompletionListener() {
                         public void onComplete(DatabaseError error, DatabaseReference ref) {
                             if(error==null) {
                                 Toast.makeText(OTPVerification.this, "Data has been added!", Toast.LENGTH_SHORT).show();
                                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                             }
                             else{
                                 Toast.makeText(OTPVerification.this, "An unexpected error occurred!", Toast.LENGTH_SHORT).show();
                             }
                             startActivity(intent);
                         }
                     });
                 }else if(counter==2){
                         Toast.makeText(OTPVerification.this, "Account Verified!", Toast.LENGTH_SHORT).show();
                         Intent intentNewPassword = new Intent(OTPVerification.this,NewPasswordPage.class);
                         intentNewPassword.putExtra("phoneNumber",phoneNumberText);
                         startActivity(intentNewPassword);
                         finish();
                     }
                 }else{
                     Toast.makeText(OTPVerification.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                 }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OTPVerification.this, "Error in verifying the credentials!", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void verifyOTP(View view){
        String codeByUser = otp.getEditText().getText().toString();
        if(codeByUser==null||codeByUser.length()<6){
            otp.setError("Wrong OTP");
            otp.requestFocus();
            return;
        }
        verifyCode(codeByUser);
    }

    public void resendOTP(View view){
        resetTimer();
        sendVerificationCode(phoneNumberText);
    }

    private void resetTimer() {
        updateTimer(SECONDS); //120 seconds
        countDownTime();
        resendButton.setVisibility(View.INVISIBLE);
    }

}
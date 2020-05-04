package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Dialogs.InfoDialog;
import com.curiocodes.decrypta.Models.UserModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public class OtpActivity extends AppCompatActivity {

    private static final String TAG = "Verification";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId;
    private String phoneNumber;
    private EditText mCode;
    private TextView resendText;
    private FirebaseAuth mAuth;
    private Handler handler;
    private Runnable runnable;
    private FirebaseFirestore db;
    private Snackbar bar;
    private View view;
    private String type, soldier;
    private boolean isExist = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Verify your phone number");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Bundle bundle = getIntent().getExtras();
        type = getIntent().getStringExtra("type");
        if (type.equals("Other")) {
            soldier = getIntent().getStringExtra("soldier");
        }
        view = findViewById(R.id.OtpView);
        phoneNumber = getIntent().getStringExtra("phone");

        resendText = findViewById(R.id.resend);
        mAuth = FirebaseAuth.getInstance();
        mCode = findViewById(R.id.code);
        db = FirebaseFirestore.getInstance();

        sendVerificationCode(phoneNumber);

        resendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode(phoneNumber);
                resendText.setClickable(false);
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bar = General.proSnackBar(OtpActivity.this, view, "Verifying phone number");
                String code = mCode.getText().toString();
                try {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                } catch (IllegalArgumentException e) {
                    General.toast(getApplicationContext(), "Retry");
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

    }

    public void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks


    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.w(TAG, "onVerificationFailed", e);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {

            } else if (e instanceof FirebaseTooManyRequestsException) {

            }


        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            Log.d(TAG, "onCodeSent:" + verificationId);

            General.toast(OtpActivity.this, "Sending code...");

            mVerificationId = verificationId;
            mResendToken = token;

            handler = new android.os.Handler();
            runnable = new Runnable() {
                public void run() {
                    Log.i("tag", "This'll run 60000 milliseconds later");
                    InfoDialog infoDialog = new InfoDialog("VERIFICATION TIMED OUT");
                    infoDialog.show(getSupportFragmentManager(), "dialog");
                    resendText.setClickable(true);
                }
            };
            handler.postDelayed(runnable, 60000);

        }
    };

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        handler.removeCallbacks(runnable);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            final Map<String, Object> map = new HashMap<>();
                            Intent intent = new Intent(OtpActivity.this, SetProfileActivity.class);
                            intent.putExtra("phone", phoneNumber);
                            intent.putExtra("type", type);
                            if (type.equals("Other")) {
                                intent.putExtra("soldier", soldier);
                            }
                            startActivity(intent);
                            finish();
                                /*db.collection("Users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                        if (documentSnapshot.exists()){
                                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
                                            map.put("name",userModel.getName());
                                            map.put("profile",userModel.getProfile());
                                            map.put("phone",mAuth.getCurrentUser().getPhoneNumber());
                                            Date time = new Timestamp(System.currentTimeMillis());
                                            map.put("signedDate",time);
                                            map.put("userId",mAuth.getCurrentUser().getUid());
                                            map.put("type",type);
                                            if (type.equals("Other")){
                                                map.put("soldier",soldier);
                                            }

                                            db.collection("Users").document(mAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isComplete()){
                                                        bar.dismiss();
                                                        Intent intent = new Intent(OtpActivity.this,SetProfileActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                        }else{
                                            map.put("name","");
                                            map.put("profile","");
                                            map.put("phone",mAuth.getCurrentUser().getPhoneNumber());
                                            Date time = new Timestamp(System.currentTimeMillis());
                                            map.put("signedDate",time);
                                            map.put("userId",mAuth.getCurrentUser().getUid());
                                            map.put("type",type);
                                            if (type.equals("Other")){
                                                map.put("soldier",soldier);
                                            }

                                            db.collection("Users").document(mAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isComplete()){
                                                        bar.dismiss();
                                                        Intent intent = new Intent(OtpActivity.this,SetProfileActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                                map.put("phone",mAuth.getCurrentUser().getPhoneNumber());
                                Date time = new Timestamp(System.currentTimeMillis());
                                map.put("signedDate",time);
                                map.put("userId",mAuth.getCurrentUser().getUid());
                                map.put("type",type);
                                if (type.equals("Other")){
                                    map.put("soldier",soldier);
                                }

                                db.collection("Users").document(mAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isComplete()){
                                            bar.dismiss();
                                            Intent intent = new Intent(OtpActivity.this,SetProfileActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
*/
                        } else {
                            bar.dismiss();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                InfoDialog infoDialog = new InfoDialog("YOU ENTERED INVALID VERIFICATION CODE !");
                                infoDialog.show(getSupportFragmentManager(), "dialog");
                            }
                        }
                    }
                });
    }

   /* private boolean checkUserIsExisted(){
        db.collection("Users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    isExist = true;
                }
            }
        });
        return isExist;

    }*/


}

package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Dialogs.InfoDialog;
import com.curiocodes.decrypta.Models.UserModel;
import com.curiocodes.decrypta.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class RegisterActivity extends AppCompatActivity {

    private static final int READ_SMS = 1001;
    private EditText mPhone, sPhone;
    private FirebaseFirestore db;
    private RadioGroup group;
    private View view;
    private String checkedStr = "Soldier";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        view = findViewById(R.id.registerView);
        mPhone = findViewById(R.id.phone);
        db = FirebaseFirestore.getInstance();
        group = findViewById(R.id.radioGrp);
        sPhone = findViewById(R.id.soldierNum);

        group.setSelected(true);
        group.check(((RadioButton) group.getChildAt(0)).getId());
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedStr = ((RadioButton) findViewById(checkedId)).getText().toString();
                if (checkedStr.equals("Other")) {
                    findViewById(R.id.txt2).setVisibility(View.VISIBLE);
                    findViewById(R.id.txt3).setVisibility(View.VISIBLE);
                    findViewById(R.id.soldierNum).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.txt2).setVisibility(View.GONE);
                    findViewById(R.id.soldierNum).setVisibility(View.GONE);
                    findViewById(R.id.txt3).setVisibility(View.GONE);
                }
            }
        });

        checkPermissionIsOk();

        findViewById(R.id.verifyBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final String phone = mPhone.getText().toString();
                final String solPhone = sPhone.getText().toString();
                final CollectionReference reference = db.collection("Soldiers");
                final CollectionReference userReference = db.collection("Users");

                if (phone.length() == 10) {

                    final Snackbar bar = General.proSnackBar(RegisterActivity.this, view, "Identifying user");

                    if (checkedStr.equals("Soldier")) {

                        Query query = reference.whereEqualTo("phone", phone);
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().getDocuments().size() > 0) {
                                        Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                                        intent.putExtra("phone", phone);
                                        intent.putExtra("type", checkedStr);
                                        bar.dismiss();
                                        startActivity(intent);
                                    } else {
                                        bar.dismiss();
                                        General.snackBar(view, "YOU ARE NOT AN AUTHORIZED USER.");
                                    }
                                }
                            }
                        });
                    } else {
                        if (solPhone.length() == 10) {
                            Query notesQuery = userReference.whereEqualTo("phone", "+91" + solPhone);
                            notesQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().getDocuments().size() > 0) {
                                            String userId = task.getResult().getDocuments().get(0).getString("userId");
                                            CollectionReference connections = userReference.document(userId).collection("Connections");
                                            Query query = connections.whereEqualTo("phone", "+91" + phone);
                                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.getResult().getDocuments().size() > 0) {
                                                            Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                                                            intent.putExtra("phone", phone);
                                                            intent.putExtra("type", checkedStr);
                                                            intent.putExtra("soldier", "+91" + solPhone);
                                                            bar.dismiss();
                                                            startActivity(intent);
                                                        } else {
                                                            bar.dismiss();
                                                            General.snackBar(view, "YOU ARE NOT AN AUTHORIZED USER.");
                                                        }
                                                    }
                                                }
                                            });
                                        } else {
                                            bar.dismiss();
                                            General.snackBar(view, "YOU ARE NOT AN AUTHORIZED USER.");
                                        }
                                    } else {

                                    }
                                }
                            });


                        } else {
                            sPhone.setError("Enter a valid phone number.");
                            //progressDialog.dismiss();
                            bar.dismiss();
                        }
                    }
                } else {
                    mPhone.setError("Enter a valid phone number.");
                }
            }
        });
    }

    private void checkPermissionIsOk() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    READ_SMS);

        } else if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    READ_SMS);
        } else if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    READ_SMS);
        } else if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    READ_SMS);
        } else {
            ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{Manifest.permission.READ_SMS},
                    READ_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case READ_SMS: {
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //General.toast(RegisterActivity.this,"Permission granted");

                } else if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    General.toast(RegisterActivity.this, "Permission denied");
                    checkPermissionIsOk();
                }
                return;
            }
        }
    }


}

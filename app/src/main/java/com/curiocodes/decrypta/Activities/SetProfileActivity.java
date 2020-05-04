package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.curiocodes.decrypta.AddOn.CompressTask;
import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Models.UserModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetProfileActivity extends AppCompatActivity {

    private int PICK_IMAGE = 1001;
    private int READ_DATA = 2002;
    private CircleImageView profileImage;
    private EditText userName;
    private Uri uri, uploadedUri;
    private FirebaseAuth mAuth;
    private byte[] uploadBytes;
    private ProgressBar progressBar;
    private String phone, type, soldier;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Set profile");

        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.name);
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress);

        type = getIntent().getStringExtra("type");
        if (type.equals("Other")) {
            soldier = getIntent().getStringExtra("soldier");
        }
        phone = getIntent().getStringExtra("phone");

        findViewById(R.id.profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(SetProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(SetProfileActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_DATA);

                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                }
            }
        });

        findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = userName.getText().toString();
                if (!name.isEmpty()) {
                    if (uri != null || uploadedUri != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", name);
                        map.put("type", type);
                        map.put("userId", mAuth.getCurrentUser().getUid());
                        map.put("phone", "+91" + phone);
                        if (type.equals("Other")) {
                            map.put("soldier", soldier);
                        }
                        if (uri == null && uploadedUri != null) {
                            map.put("profile", uploadedUri.toString());
                        }
                        Date time = new Timestamp(System.currentTimeMillis());
                        map.put("signedDate", time);
                        db.collection("Users").document(mAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (uri != null) {
                                    ImageResize imageResize = new ImageResize(null);
                                    imageResize.execute(uri);
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(SetProfileActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });

                    } else {
                        General.toast(SetProfileActivity.this, "Please choose a profile picture");
                    }
                } else {
                    General.toast(SetProfileActivity.this, "Please enter your name");
                }
            }
        });

        getUserData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            uri = data.getData();
            profileImage.setImageURI(uri);
        }
    }


    //compression class
    public class ImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap bitmap;

        public ImageResize(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            ;
            if (bitmap == null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uris[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return getBytesFromBitmap(bitmap, 10);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            uploadBytes = bytes;
            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("Uploads").child("profile")
                    .child(mAuth.getCurrentUser().getUid());
            UploadTask uploadTask = ref.putBytes(uploadBytes);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> map = new HashMap<>();
                        map.put("profile", downloadUri.toString());
                        db.collection("Users").document(mAuth.getCurrentUser().getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(SetProfileActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    General.toast(getApplicationContext(), task.getException().getMessage());
                                }
                            }
                        });
                    } else {
                        // Handle failures
                        // ...
                        General.toast(SetProfileActivity.this, "failed");
                    }
                }
            });
        }
    }


    private void getUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {
            db.collection("Users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.exists()) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        assert userModel != null;
                        userName.setText(userModel.getName());
                        if (userModel.getProfile() != null) {
                            uploadedUri = Uri.parse(userModel.getProfile());
                        }
                        Picasso.get().load(userModel.getProfile()).fit().placeholder(R.drawable.avatar).into(profileImage);
                    }
                }
            });
        }
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

}

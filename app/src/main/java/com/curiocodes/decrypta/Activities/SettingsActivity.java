package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Dialogs.ConfirmDialog;
import com.curiocodes.decrypta.Models.UserModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.Internal;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements ConfirmDialog.onSelected {

    private static final int PICK_IMAGE = 1001;
    private CircleImageView imageView;
    private TextView name;
    private TextView phone;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri uri;
    private ProgressBar progressBar;
    private byte[] uploadBytes;
    private LinearLayout edit_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.profile_image);
        TextView update = findViewById(R.id.update);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        TextView logout = findViewById(R.id.logout);
        TextView delete = findViewById(R.id.delete);
        progressBar = findViewById(R.id.progress);
        edit_name = findViewById(R.id.edit_name);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        getUser();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        findViewById(R.id.logout_block).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmDialog confirmDialog = new ConfirmDialog("Logout", "Are you sure you want to logout ?", "Logout");
                confirmDialog.show(getSupportFragmentManager(), "dialog");
            }
        });

        findViewById(R.id.delete_block).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Users").document(mAuth.getCurrentUser().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        }
                    }
                });
            }
        });

        findViewById(R.id.username).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_name.getVisibility() == View.GONE) {
                    edit_name.setVisibility(View.VISIBLE);
                } else {
                    edit_name.setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.saveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.new_name);
                final String newName = editText.getText().toString();
                if (!newName.isEmpty()) {
                    db.collection("Users").document(mAuth.getCurrentUser().getUid())
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if (!documentSnapshot.getString("name").equals(newName)) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("name", newName);
                                        documentSnapshot.getReference().update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    General.toast(getApplicationContext(), "Username updated");
                                                    edit_name.setVisibility(View.GONE);
                                                    return;
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            uri = data.getData();
            imageView.setImageURI(uri);
            progressBar.setVisibility(View.VISIBLE);
            ImageResize imageResize = new ImageResize(null);
            imageResize.execute(uri);
        }
    }

    private void getUser() {
        db.collection("Users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    UserModel userModel = documentSnapshot.toObject(UserModel.class);
                    Picasso.get().load(documentSnapshot.getString("profile")).placeholder(R.drawable.profile_placeholder).fit().into(imageView);
                    name.setText(userModel.getName());
                    phone.setText(userModel.getPhone());
                }
            }
        });
    }


    @Override
    public void onDone(boolean isDone) {
        if (isDone) {
            mAuth.signOut();
            Intent intent = new Intent(SettingsActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
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
                                    progressBar.setVisibility(View.GONE);
                                    getUser();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
                    } else {
                        // Handle failures
                        // ...
                        General.toast(SettingsActivity.this, "failed");
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

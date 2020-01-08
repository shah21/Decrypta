package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Models.ChatModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static com.curiocodes.decrypta.Activities.SetProfileActivity.getBytesFromBitmap;

public class SendFileActivity extends AppCompatActivity {

    private ImageView imageView;
    private Uri uri;
    private String uname,img,phone,uid;
    private EditText mText;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private byte[] uploadBytes;
    private Cipher cipher,decipher;
    private Snackbar snackbar;
    private SecretKeySpec secretKeySpec;
    private byte[] encryptionKey = {9,112,51,84,105,4,-31,-23,-48, 88,17,20,3,-105,119,-53};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        uname = getIntent().getStringExtra("name");
        img = getIntent().getStringExtra("filepath");
        phone = getIntent().getStringExtra("phone");
        getSupportActionBar().setTitle(uname);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uri = Uri.parse(img);

        mText = findViewById(R.id.message);
        imageView = findViewById(R.id.image);
        imageView.setImageURI(uri);
        View view = findViewById(R.id.layout);
        snackbar = General.proSnackBar(getApplicationContext(),view,"Sending image");
        snackbar.dismiss();

        //encryption

        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey,"AES");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageResize imageResize = new ImageResize(null);
                imageResize.execute(uri);
            }
        });
    }

    //compression class
    public class ImageResize extends AsyncTask<Uri,Integer,byte[]> {

        Bitmap bitmap;

        public ImageResize(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {;
            if (bitmap==null){
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uris[0]);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            return getBytesFromBitmap(bitmap,100);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            uploadBytes = bytes;
            snackbar.show();
            uploadImage();
        }
    }

    private void uploadImage(){
        String id = String.valueOf(new Date().getTime());
        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("Uploads/Chats/"+uid)
                .child(id);

        UploadTask uploadTask = ref.putBytes(uploadBytes);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                snackbar.setText("Sending.. "+(int)progress+"%");
            }
        });
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    snackbar.dismiss();
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
                    sendMessage(downloadUri);
                } else {
                    // Handle failures
                    // ...
                    snackbar.dismiss();
                }
            }
        });

    }

    private void sendMessage(Uri downloadUri) {
        final DocumentReference reference = db.collection("Users").document(uid);
        String message = mText.getText().toString();
        final Date time = new Timestamp(System.currentTimeMillis());
        final ChatModel chatModel;
        if (!message.isEmpty()) {
            chatModel = new ChatModel(encryptData(message), encryptData(String.valueOf(downloadUri)), mAuth.getCurrentUser().getPhoneNumber(), time);
        }else {
            chatModel = new ChatModel(null, encryptData(String.valueOf(downloadUri)), mAuth.getCurrentUser().getPhoneNumber(), time);
        }

        db.collection("Users").document(uid).collection("ChatRooms").document(phone)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.get("created")==null){
                    Map<String,Object> map = new HashMap<>();
                    map.put("created",new Timestamp(System.currentTimeMillis()));
                    documentSnapshot.getReference().set(map);
                }
            }
        });

        db.collection("Users").whereEqualTo("phone",phone).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().getDocuments().size() > 0){
                        db.collection("Users").document(task.getResult().getDocuments().get(0).getId()).collection("ChatRooms")
                                .document(mAuth.getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.get("created")==null){
                                    Map<String,Object> map = new HashMap<>();
                                    map.put("created",new Timestamp(System.currentTimeMillis()));
                                    documentSnapshot.getReference().set(map);
                                }
                            }
                        });
                    }
                }
            }
        });


        reference.collection("ChatRooms").document(phone).collection("Chats")
                .document(String.valueOf(new Random().nextInt()))
                .set(chatModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Query query = db.collection("Users").whereEqualTo("phone",phone);
                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()){
                                        if (task.getResult().getDocuments().size() > 0){
                                            task.getResult().getDocuments().get(0).getReference().collection("ChatRooms")
                                                    .document(mAuth.getCurrentUser().getPhoneNumber())
                                                    .collection("Chats").document(String.valueOf(new Random().nextInt())).set(chatModel)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                snackbar.dismiss();
                                                                Intent intent = new Intent(SendFileActivity.this,ChatRoomActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }else {
                                                                snackbar.dismiss();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });
                        }
                    }
                });


    }

    private String encryptData(String data){
        byte[] stringBytes = data.getBytes();
        byte[] encryptedBytes = new byte[stringBytes.length];
        String encryptedStr = null;

        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedBytes = cipher.doFinal(stringBytes);
            encryptedStr = new String(encryptedBytes,"ISO-8859-1");
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encryptedStr;
    }


}

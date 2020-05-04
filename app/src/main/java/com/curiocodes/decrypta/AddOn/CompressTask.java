package com.curiocodes.decrypta.AddOn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import androidx.annotation.NonNull;

import com.curiocodes.decrypta.Activities.MainActivity;
import com.curiocodes.decrypta.Activities.SetProfileActivity;
import com.curiocodes.decrypta.Dialogs.AddDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CompressTask extends AsyncTask<Bitmap, Integer, byte[]> {


    private ByteArrayOutputStream byteArrayOutputStream;


    @Override
    protected byte[] doInBackground(Bitmap... bitmaps) {
        byteArrayOutputStream = new ByteArrayOutputStream();
        bitmaps[0].compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        super.onPostExecute(bytes);
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("Uploads").child("profile");
        UploadTask uploadTask = ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).putBytes(bytes);
        Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
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

                            } else {

                            }
                        }
                    });
                } else {
                    //Handle exception
                }
            }
        });
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}

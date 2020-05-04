package com.curiocodes.decrypta.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.curiocodes.decrypta.Adapters.SavedAdapter;
import com.curiocodes.decrypta.Models.SaveModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SavedActivity extends AppCompatActivity {

    private GridView gridView;
    private SavedAdapter adapter;
    private List<SaveModel> list;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_saved);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Saved photos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        list = new ArrayList<>();
        adapter = new SavedAdapter(SavedActivity.this, list, new SavedAdapter.Onclick() {
            @Override
            public void onClickItem(int pos) {
                Intent intent = new Intent(SavedActivity.this, OpenPhotoActivity.class);
                intent.putExtra("uri",list.get(pos).getUri());
                intent.putExtra("date",list.get(pos).getDate());
                intent.putExtra("sender",list.get(pos).getSender());
                startActivity(intent);
                finish();
            }
        });

        progressBar = findViewById(R.id.progress);

        gridView = findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        getPhotos();
    }

    private void getPhotos() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Saved").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.getDocuments().size() > 0){
                    for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){
                        SaveModel saveModel = documentSnapshot.toObject(SaveModel.class);
                        list.add(saveModel);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}

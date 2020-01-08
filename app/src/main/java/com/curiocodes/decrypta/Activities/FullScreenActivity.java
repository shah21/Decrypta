package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Models.SaveModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.Date;

public class FullScreenActivity extends AppCompatActivity {

    private String uri,name,type;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uri = getIntent().getStringExtra("uri");
        name = getIntent().getStringExtra("name");

        getSupportActionBar().setTitle(name);

        imageView = findViewById(R.id.image);
        Picasso.get().load(uri).fit().into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save:
            {
                savePhoto();
                return true;
            }
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }
        return true;
    }

    private void savePhoto() {
        Date date = new Timestamp(System.currentTimeMillis());
        final SaveModel saveModel = new SaveModel(uri,name,date);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Saved").whereEqualTo("uri",uri).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().getDocuments().size() == 0 ){
                        db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                collection("Saved").add(saveModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference reference) {
                                General.toast(getApplicationContext(),"Photo saved");
                            }
                        });
                    }else {
                        General.toast(getApplicationContext(),"Already saved");
                    }
                }
            }
        });

    }
}

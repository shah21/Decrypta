package com.curiocodes.decrypta.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.curiocodes.decrypta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView mName;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        phone = getIntent().getStringExtra("phone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        mName = findViewById(R.id.name);
        mName.setText(phone);
        profileImage = findViewById(R.id.profile_image);
        mAuth = FirebaseAuth.getInstance();

        getUserData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_room_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void getUserData(){
        db.collection("Users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.getString("phone").equals(phone)){
                    mName.setText(documentSnapshot.getString("phone"));
                    Uri uri = Uri.parse(documentSnapshot.getString("profile"));
                    profileImage.setImageURI(uri);
                    return;
                }
            }
        });
    }
}

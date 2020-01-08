package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.curiocodes.decrypta.Adapters.ChatsAdapter;
import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Models.ChatModel;
import com.curiocodes.decrypta.Models.UserModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1001;
    private FirebaseFirestore db;
    private TextView mName;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private RecyclerView chatsView;
    private String phone,uname;
    private List<ChatModel> list;
    private ChatsAdapter adapter;
    private String uid;
    private EditText message;
    private ProgressBar progressBar;
    private boolean isAuth = true;
    //encryption vars
    private LinearLayout warning;
    private byte[] encryptionKey = {9,112,51,84,105,4,-31,-23,-48, 88,17,20,3,-105,119,-53};
    private Cipher cipher,decipher;
    private SecretKeySpec secretKeySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        phone = getIntent().getStringExtra("phone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //shared preference
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (phone!=null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("phone", phone);
            editor.apply();
        }
        if (phone == null) {
            phone = sharedPref.getString("phone", null);
        }

        db = FirebaseFirestore.getInstance();
        mName = findViewById(R.id.name);
        mName.setText(phone);
        profileImage = findViewById(R.id.profile_image);
        mAuth = FirebaseAuth.getInstance();
        list = new ArrayList<>();
        warning = findViewById(R.id.warning);
        adapter = new ChatsAdapter(this, list, new ChatsAdapter.OnItemClick() {
            @Override
            public void imgClick(int pos) {
                Intent intent = new Intent(ChatRoomActivity.this,FullScreenActivity.class);
                intent.putExtra("uri",list.get(pos).getUri());
                intent.putExtra("name",uname);
                startActivity(intent);
            }
        });
        message = findViewById(R.id.message);
        progressBar = findViewById(R.id.progress);
        uid = mAuth.getCurrentUser().getUid();

        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey,"AES");

        chatsView = findViewById(R.id.chats);
        chatsView.setLayoutManager(new LinearLayoutManager(this));
        chatsView.setAdapter(adapter);

        getConnectionsData();
        getUserData();
        //get chats list
        getMessage();

        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAuth) {
                    String msg = message.getText().toString();
                    if (!msg.isEmpty()) {
                        message.setText("");
                        sendMessage(msg);
                    }
                }
            }
        });

        findViewById(R.id.attach).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == PICK_IMAGE) {
            if (data!=null) {
                Intent intent = new Intent(ChatRoomActivity.this, SendFileActivity.class);
                intent.putExtra("filepath", String.valueOf(data.getData()));
                intent.putExtra("name", uname);
                intent.putExtra("phone", phone);
                startActivity(intent);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_room_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void sendMessage(final String msg) {
        final DocumentReference reference = db.collection("Users").document(uid);
        final Date time = new Timestamp(System.currentTimeMillis());
        final ChatModel chatModel = new ChatModel(encryptData(msg),null,mAuth.getCurrentUser().getPhoneNumber(),time);

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
                                                                adapter.notifyDataSetChanged();
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

    private void getMessage(){
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Users").document(uid).collection("ChatRooms")
                .document(phone).collection("Chats")
                .orderBy("time")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                list.clear();
                for (DocumentSnapshot doc:queryDocumentSnapshots){
                    ChatModel model = doc.toObject(ChatModel.class);
                    String message = doc.getString("message");
                    try {
                        ChatModel chatModel;
                        if (model.getUri()!=null && model.getMessage() !=null){
                            chatModel = new ChatModel(decryptData(message), decryptData(model.getUri()), doc.getString("sender"), model.getTime());
                        }else if (model.getUri()!=null && model.getMessage()==null){
                            chatModel = new ChatModel(null,decryptData(model.getUri()) ,doc.getString("sender"), model.getTime());
                        }else {
                            chatModel = new ChatModel(decryptData(message),null ,doc.getString("sender"), model.getTime());
                        }
                        list.add(chatModel);
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                    }


                }
                progressBar.setVisibility(View.INVISIBLE);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void getUserData(){
        db.collection("Users").whereEqualTo("phone",phone).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.getDocuments().size() > 0) {
                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    //mName.setText(doc.getString("name"));
                    uname = doc.getString("name");
                    Uri uri = Uri.parse(doc.getString("profile"));
                    Picasso.get().load(uri).fit().placeholder(R.drawable.child_placeholder).into(profileImage);
                    isAuth = true;
                }else {
                    isAuth = false;
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getConnectionsData(){
        db.collection("Users").document(uid).collection("Connections")
                .whereEqualTo("phone",phone).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots.getDocuments().size() > 0){
                    mName.setText(queryDocumentSnapshots.getDocuments().get(0).getString("name"));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear:
            {
                clearChats();
                return true;
            }
            case R.id.delete:
            {
                deleteChat();
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

    private void deleteChat() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Users").document(uid).collection("ChatRooms")
                .document(phone).collection("Chats").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.getDocuments().size() > 0){
                    for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()){
                        doc.getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                deleteChat();
                            }
                        });
                    }
                }else {
                    db.collection("Users").document(uid).collection("ChatRooms")
                            .document(phone).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(ChatRoomActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        });
    }

    private void clearChats() {
        Query query = db.collection("Users").document(uid).collection("ChatRooms")
                .document(phone).collection("Chats");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().getDocuments().size() > 0){
                        for (DocumentSnapshot doc:task.getResult().getDocuments()){
                            doc.getReference().delete();
                        }
                    }
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

    private String decryptData(String string) throws UnsupportedEncodingException {
        String decryptedString = null;
        if (string!=null) {
            byte[] encryptedBytes = string.getBytes("ISO-8859-1");
            byte[] decryptedBytes;

            try {
                decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
                decryptedBytes = decipher.doFinal(encryptedBytes);
                decryptedString = new String(decryptedBytes);
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }


        return decryptedString;
    }
}

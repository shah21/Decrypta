package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.curiocodes.decrypta.Adapters.ChatsAdapter;
import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Models.ChatModel;
import com.curiocodes.decrypta.Models.UserModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private static final int PERMISSION_ID = 2002;
    private FirebaseFirestore db;
    private TextView mName;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private RecyclerView chatsView;
    private String phone, uname;
    private double lat, logt;
    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationListener locationListener;
    private List<ChatModel> list;
    public static final String DIC_PATH = "vocab.txt";
    private ChatsAdapter adapter;
    private String uid;
    private EditText message;
    private ProgressBar progressBar;
    private boolean isAuth = true;
    //encryption vars
    private LinearLayout warning;
    private byte[] encryptionKey = {9, 112, 51, 84, 105, 4, -31, -23, -48, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;
    private String receiverType;
    private String receiverId;
    private String senderType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_chat_room);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        phone = getIntent().getStringExtra("phone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //shared preference
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (phone != null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("phone", phone);
            editor.apply();
        }
        if (phone == null) {
            phone = sharedPref.getString("phone", null);
        }
        //location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


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
                Intent intent = new Intent(ChatRoomActivity.this, FullScreenActivity.class);
                intent.putExtra("uri", list.get(pos).getUri());
                if (list.get(pos).getSender().equals(mAuth.getCurrentUser().getPhoneNumber())) {
                    intent.putExtra("name", "You");
                } else {
                    intent.putExtra("name", uname);
                }
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

        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");

        chatsView = findViewById(R.id.chats);
        chatsView.setLayoutManager(new LinearLayoutManager(this));
        chatsView.setAdapter(adapter);

        getConnectionsData();
        getSenderType();
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
                        verifyMesssage(msg);
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

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    lat = location.getLatitude();
                                    logt = location.getLongitude();
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            lat = mLastLocation.getLatitude();
            logt = mLastLocation.getLongitude();
        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
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
        getMenuInflater().inflate(R.menu.chat_room_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private List<String> convertToList(String filename) {
        ArrayList<String> listOfLines = new ArrayList<>();
        try {
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(getAssets().open(filename), "UTF-8"));
            String line = bufReader.readLine();
            while (line != null) {
                listOfLines.add(line);
                line = bufReader.readLine();
            }
            bufReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listOfLines;
    }

    private void verifyMesssage(final String msg) {

        List list = convertToList(DIC_PATH);
        int count = 0;
        if (receiverType.equals("Other") || senderType.equals("Other")) {
            for (Object obj : list) {
                System.out.println(obj.toString());
                if (msg.contains(obj.toString())) {
                    count++;
                }
            }

            if (count > 0) {

                General.toast(this, "You cannot send message with sensitive content !");
                DocumentReference documentReference = db.collection("Notifications").document();
                String notifId = documentReference.getId();
                Map<String, Object> map = new HashMap<>();
                map.put("time", new Timestamp(System.currentTimeMillis()));
                map.put("userId", mAuth.getCurrentUser().getUid());
                map.put("receiverId", receiverId);
                map.put("latitude", lat);
                map.put("notifId", notifId);
                map.put("msg_type", "text");
                map.put("longitude", logt);
                db.collection("Notifications").document(notifId).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
            } else {
                sendMessage(msg);
            }
        } else {
            sendMessage(msg);
        }


    }

    private void sendMessage(final String msg) {
        final DocumentReference reference = db.collection("Users").document(uid);
        final Date time = new Timestamp(System.currentTimeMillis());
        final ChatModel chatModel = new ChatModel(encryptData(msg), null, mAuth.getCurrentUser().getPhoneNumber(), time);

        db.collection("Users").document(uid).collection("ChatRooms").document(phone)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.get("created") == null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("created", new Timestamp(System.currentTimeMillis()));
                    documentSnapshot.getReference().set(map);
                }
            }
        });

        db.collection("Users").whereEqualTo("phone", phone).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        db.collection("Users").document(task.getResult().getDocuments().get(0).getId()).collection("ChatRooms")
                                .document(mAuth.getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.get("created") == null) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("created", new Timestamp(System.currentTimeMillis()));
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
                        if (task.isSuccessful()) {
                            Query query = db.collection("Users").whereEqualTo("phone", phone);
                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().getDocuments().size() > 0) {
                                            task.getResult().getDocuments().get(0).getReference().collection("ChatRooms")
                                                    .document(mAuth.getCurrentUser().getPhoneNumber())
                                                    .collection("Chats").document(String.valueOf(new Random().nextInt())).set(chatModel)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
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

    private void getMessage() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Users").document(uid).collection("ChatRooms")
                .document(phone).collection("Chats")
                .orderBy("time")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        list.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            ChatModel model = doc.toObject(ChatModel.class);
                            String message = doc.getString("message");
                            try {
                                ChatModel chatModel;
                                if (model.getUri() != null && model.getMessage() != null) {
                                    chatModel = new ChatModel(decryptData(message), decryptData(model.getUri()), doc.getString("sender"), model.getTime());
                                } else if (model.getUri() != null && model.getMessage() == null) {
                                    chatModel = new ChatModel(null, decryptData(model.getUri()), doc.getString("sender"), model.getTime());
                                } else {
                                    chatModel = new ChatModel(decryptData(message), null, doc.getString("sender"), model.getTime());
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

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    private void getUserData() {
        db.collection("Users").whereEqualTo("phone", phone).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.getDocuments().size() > 0) {
                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    //mName.setText(doc.getString("name"));
                    uname = doc.getString("name");
                    receiverType = doc.getString("type");
                    receiverId = doc.getString("userId");
                    Uri uri = Uri.parse(doc.getString("profile"));
                    Picasso.get().load(uri).fit().placeholder(R.drawable.profile_placeholder).into(profileImage);
                    isAuth = true;
                } else {
                    isAuth = false;
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
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

    private void getSenderType() {
        db.collection("Users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    senderType = documentSnapshot.getString("type");
                }
            }
        });
    }
}

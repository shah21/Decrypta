package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.curiocodes.decrypta.Adapters.Adapter;
import com.curiocodes.decrypta.Adapters.ContactsAdapter;
import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Dialogs.AddDialog;
import com.curiocodes.decrypta.Models.ContactsModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SelectContactsActivity extends AppCompatActivity implements AddDialog.onSelected {

    private static final int READ_CONTACTS = 2002;
    private RecyclerView contactListView;
    private Adapter contactsAdapter;
    private List<ContactsModel> list;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String cname, cphone;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Select contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        list = new ArrayList<>();
        contactListView = findViewById(R.id.connectionListView);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        checkPermissionIsOk();
        progressBar = findViewById(R.id.progress);
        get();

        contactsAdapter = new Adapter(this, list, new Adapter.OnClick() {
            @Override
            public void onItemClicked(String name, String phone) {
                cname = name;
                cphone = phone;
                AddDialog addDialog = new AddDialog("Add connection", "Are you sure you want to add " + name + " to your connections");
                addDialog.show(getSupportFragmentManager(), "dialog");
            }
        });
        contactListView.setLayoutManager(new LinearLayoutManager(this));
        contactListView.addItemDecoration(new DividerItemDecoration(contactListView.getContext(), DividerItemDecoration.VERTICAL));
        contactListView.setAdapter(contactsAdapter);


    }


    //get data from contacts using cursor
    private void get() {
        final Cursor cursor = getContentResolver().
                query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);


        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            ContactsModel contactsModel = new ContactsModel(phone, name);
            boolean isContain = false;
            for (ContactsModel c : list) {
                if (c.getPhone().equals(phone)) {
                    isContain = true;
                }
            }

            if (!isContain) {
                list.add(contactsModel);
            }
            cursor.moveToNext();
        }

    }

    private void checkPermissionIsOk() {
        if (ContextCompat.checkSelfPermission(SelectContactsActivity.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(SelectContactsActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    General.toast(SelectContactsActivity.this, "Permission granted");
                } else {
                    General.toast(SelectContactsActivity.this, "Permission denied");
                }
                return;
            }
        }
    }


    //interface method from addDialog
    @Override
    public void onDone(boolean isDone) {
        if (isDone) {
            progressBar.setVisibility(View.VISIBLE);
            final Random random = new Random();
            final Map<String, Object> map = new HashMap<>();
            //eliminating spaces
            String number = cphone.replaceAll("\\s+", "");
            //check if number contains +91 or not
            if (!number.contains("+91")) {
                number = "+91" + number;
            }
            map.put("name", cname);
            map.put("phone", number);

            db.collection("Users").document(mAuth.getCurrentUser().getUid())
                    .collection("Connections").document(String.valueOf(random.nextInt())).set(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            General.makeIntent(SelectContactsActivity.this, ConnectionsActivity.class);
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchContact(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchContact(newText);
                return true;
            }
        });

        return true;
    }

    private void searchContact(String query) {
        String userInput = query.toLowerCase();
        List<ContactsModel> newList = new ArrayList<>();

        for (ContactsModel user : list) {
            String userName = user.getName().toLowerCase();
            if (userName.contains(userInput)) {
                newList.add(user);
            }
        }
        contactsAdapter.updateList(newList);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search) {

        }
        return false;
    }
}

package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.curiocodes.decrypta.Adapters.ContactsAdapter;
import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Dialogs.DeleteDialog;
import com.curiocodes.decrypta.Models.ContactsModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ConnectionsActivity extends AppCompatActivity implements DeleteDialog.onSelected {

    private RecyclerView recyclerView;
    private ContactsAdapter adapter;
    private List<ContactsModel> list;
    private ProgressBar progressBar;
    private int position;
    private FirebaseAuth mAuth;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Connections");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        view = findViewById(R.id.conView);
        list = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.connectionListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.addItemDecoration(new DividerItemDecoration(ConnectionsActivity.this, DividerItemDecoration.VERTICAL));
        adapter = new ContactsAdapter(this, list, new ContactsAdapter.OnClick() {
            @Override
            public void onItemClicked(int pos) {
                Intent intent = new Intent(ConnectionsActivity.this, ChatRoomActivity.class);
                intent.putExtra("phone", list.get(pos).getPhone());
                intent.putExtra("name", list.get(pos).getName());
                startActivity(intent);
            }

            @Override
            public void onItemDelete(final int pos) {
                position = pos;
                DeleteDialog deleteDialog = new DeleteDialog("Delete connection", getString(R.string.delete_msg) + " " + list.get(pos).getName() + " ?");
                deleteDialog.show(getSupportFragmentManager(), "dialog");
            }
        });
        recyclerView.setAdapter(adapter);
        getData();
    }

    private void deleteItem(FirebaseFirestore db, final int pos) {

        Query query = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Connections")
                .whereEqualTo("phone", list.get(pos).getPhone());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        doc.getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    General.toast(ConnectionsActivity.this, "Contact deleted");
                                }
                            }
                        });
                    }
                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.getString("type").equals("Soldier")) {
                    getMenuInflater().inflate(R.menu.connections_menu, menu);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addContact) {
            General.makeIntent(ConnectionsActivity.this, SelectContactsActivity.class);
        }
        return false;
    }

    private void getData() {
        progressBar.setVisibility(View.VISIBLE);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable final DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.getString("type").equals("Soldier")) {
                    documentSnapshot.getReference().collection("Connections")
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                    if (queryDocumentSnapshots.getDocuments().size() > 0) {
                                        list.clear();
                                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                            ContactsModel contactsModel = new ContactsModel(doc.getString("phone"), doc.getString("name"));
                                            list.add(contactsModel);
                                        }
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    } else {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                } else {
                    db.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            for (final DocumentSnapshot doc : queryDocumentSnapshots) {
                                Query query = doc.getReference().collection("Connections").whereEqualTo("phone", mAuth.getCurrentUser().getPhoneNumber());
                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().getDocuments().size() > 0) {
                                                final ContactsModel contactsModel = new ContactsModel(doc.getString("phone"),
                                                        doc.getString("name"));

                                                db.collection("Users").document(mAuth.getCurrentUser().getUid()).collection("Connections")
                                                        .whereEqualTo("phone", contactsModel.getPhone()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            if (task.getResult().getDocuments().size() == 0) {
                                                                db.collection("Users").document(mAuth.getCurrentUser().getUid())
                                                                        .collection("Connections").add(contactsModel)
                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                if (task.isSuccessful()) {

                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    }
                                                });

                                                list.add(contactsModel);
                                            }
                                            adapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.INVISIBLE);
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
    public void onDone(boolean isDone) {
        if (isDone) {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.getString("type").equals("Other")) {
                        if (!documentSnapshot.getString("soldier").equals(list.get(position).getPhone())) {
                            deleteItem(db, position);
                        } else {
                            General.snackBar(view, "YOU CANNOT DELETE THIS CONTACT !");
                        }
                    } else {
                        deleteItem(db, position);
                    }
                }
            });
        }
    }
}

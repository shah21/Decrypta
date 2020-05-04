package com.curiocodes.decrypta.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.curiocodes.decrypta.Activities.ChatRoomActivity;
import com.curiocodes.decrypta.Adapters.ChatListAdapter;
import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Activities.ConnectionsActivity;
import com.curiocodes.decrypta.Models.ContactsModel;
import com.curiocodes.decrypta.Models.RecentChatModel;
import com.curiocodes.decrypta.Models.UserModel;
import com.curiocodes.decrypta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView recentListView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<RecentChatModel> list;
    private ChatListAdapter adapter;
    private ProgressBar progressBar;
    private SearchView searchView;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recentListView = view.findViewById(R.id.recentChatsView);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        list = new ArrayList<>();
        progressBar = view.findViewById(R.id.progress);
        searchView = view.findViewById(R.id.searchView);


        adapter = new ChatListAdapter(getContext(), list, new ChatListAdapter.OnClick() {
            @Override
            public void onItemClicked(int pos) {
                Intent intent = new Intent(getContext(), ChatRoomActivity.class);
                intent.putExtra("phone", list.get(pos).getNumber());
                startActivity(intent);
            }
        });

        recentListView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentListView.setAdapter(adapter);

        view.findViewById(R.id.startChatBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                General.makeIntent(getActivity(), ConnectionsActivity.class);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchChat(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchChat(newText);
                return true;
            }
        });

        if (mAuth.getCurrentUser() != null) {
            getRecentChats();
        }

        return view;
    }

    private void searchChat(String query) {
        String userInput = query.toLowerCase();
        List<RecentChatModel> newList = new ArrayList<>();

        for (RecentChatModel recentChatModel : list) {
            String userName = recentChatModel.getName().toLowerCase();
            if (userName.contains(userInput)) {
                newList.add(recentChatModel);
            }
        }
        adapter.updateList(newList);
    }

    private void getRecentChats() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("ChatRooms").
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    db.collection("Users").whereEqualTo("phone", doc.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                final DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                db.collection("Users").document(mAuth.getCurrentUser().getUid())
                                        .collection("Connections").whereEqualTo("phone", documentSnapshot.getString("phone"))
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (queryDocumentSnapshots.getDocuments().size() > 0) {
                                            RecentChatModel recentChatModel = new RecentChatModel(
                                                    queryDocumentSnapshots.getDocuments().get(0).getString("name"),
                                                    documentSnapshot.getString("phone"),
                                                    documentSnapshot.getString("profile")
                                            );
                                            list.add(recentChatModel);
                                            adapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });

                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

        });

    }

}

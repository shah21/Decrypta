package com.curiocodes.decrypta.AddOn;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.curiocodes.decrypta.Models.ContactsModel;
import com.curiocodes.decrypta.Models.UserModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

public class General {


    //toast
    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    //intent
    public static void makeIntent(Context start, Class end) {
        Intent intent = new Intent(start, end);
        start.startActivity(intent);
    }

    //intent
    public static void finishIntent(Context start, Class end) {
        Intent intent = new Intent(start, end);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        start.startActivity(intent);
    }

    //sort in alphebetical order
    public static List convertArray(List<ContactsModel> list) {
        Collections.sort(list, new Comparator<ContactsModel>() {
            @Override
            public int compare(ContactsModel s1, ContactsModel s2) {
                return s1.getName().compareToIgnoreCase(s2.getName());
            }
        });
        return list;
    }

    public static void intentWithData(Context start, Class end, Bundle bundle) {
        Intent intent = new Intent(start, end);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bundle1 = new Bundle();
        intent.putExtras(bundle1);
        start.startActivity(intent);
    }

    public static void snackBar(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setVisibility(View.INVISIBLE);
                    }
                })
                .show();
    }

    public static Snackbar proSnackBar(Context context, View view, String msg) {
        Snackbar bar = Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(com.google.android.material.R.id.snackbar_text).getParent();
        ProgressBar item = new ProgressBar(context);
        contentLay.addView(item);
        bar.show();
        return bar;
    }


}

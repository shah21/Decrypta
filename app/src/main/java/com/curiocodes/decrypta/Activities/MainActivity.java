package com.curiocodes.decrypta.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Fragments.HomeFragment;
import com.curiocodes.decrypta.Models.UserModel;
import com.curiocodes.decrypta.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private CircleImageView profile;
    private TextView uname, phone;
    private FirebaseFirestore db;
    private View headerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.app_name);
        mAuth = FirebaseAuth.getInstance();

        //show fragment
        if (mAuth.getCurrentUser() != null) {
            changeFragment(new HomeFragment());
        }

        db = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigarionView);
        navigationView.setNavigationItemSelectedListener(this);

        headerLayout = navigationView.inflateHeaderView(R.layout.header_layout);
        uname = headerLayout.findViewById(R.id.name);
        phone = headerLayout.findViewById(R.id.phone);
        profile = headerLayout.findViewById(R.id.profile);


        getUserData();
    }

    private void getUserData() {
        if (mAuth.getCurrentUser() != null) {
            db.collection("Users").document(mAuth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot.exists()) {
                                uname.setText(documentSnapshot.getString("name"));
                                phone.setText(documentSnapshot.getString("phone"));
                                if (documentSnapshot.getString("profile") != null) {
                                    Picasso.get().load(documentSnapshot.getString("profile")).fit().placeholder(R.drawable.profile_clr).into(profile);
                                }
                            }
                        }
                    });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //check for signed or not
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            //check profile is set or not
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.exists()) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        if (documentSnapshot.getString("name") == null || userModel.getName() == null) {
                            Intent intent = new Intent(MainActivity.this, SetProfileActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.saved:
                General.makeIntent(MainActivity.this, SavedActivity.class);
                drawerLayout.closeDrawers();
                break;
            case R.id.share:
                sendApp();
                drawerLayout.closeDrawers();
                break;
            case R.id.Settings:
                General.makeIntent(MainActivity.this, SettingsActivity.class);
                drawerLayout.closeDrawers();
                break;
            case R.id.help:
                openHelp();
                drawerLayout.closeDrawers();
                break;
            default:
                changeFragment(new HomeFragment());
                drawerLayout.closeDrawers();
                break;
        }
        return false;
    }

    private void changeFragment(Fragment fragment) {
        // Create new fragment and transaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.container, fragment);
        // Commit the transaction
        transaction.commit();
    }

    private void openHelp() {
        Uri uri = Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSeuCamnBL68Fn46RtbnYgiRuu8MQOT9CxwiwS35UKAQyWTqeg/viewform?usp=pp_url"); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void sendApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        //getting app package name
        final String appPackage = getApplicationContext().getPackageName();
        String strApplink = "";
        try {
            strApplink = "https://play.google.com/store/apps/details?id=" + appPackage;
        } catch (android.content.ActivityNotFoundException anfe) {
            strApplink = "https://play.google.com/store/apps/details?id=" + appPackage;
        }

        intent.setType("text/link");
        String sharebody = "Hey ,download our app and secure your chats" + "\n" + strApplink;
        String sharesub = String.valueOf(R.string.app_name);
        intent.putExtra(Intent.EXTRA_SUBJECT, sharesub);
        intent.putExtra(Intent.EXTRA_TEXT, sharebody);
        startActivity(Intent.createChooser(intent, "Share Using"));
    }
}

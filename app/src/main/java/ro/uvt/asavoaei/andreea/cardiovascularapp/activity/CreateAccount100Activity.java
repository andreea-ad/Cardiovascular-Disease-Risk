package ro.uvt.asavoaei.andreea.cardiovascularapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.InternetConnectionDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.UserProfile;

public class CreateAccount100Activity extends AppCompatActivity {
    private static final String TAG = CreateAccount0Activity.class.getCanonicalName();
    private FirebaseAuth firebaseAuth;
    private InternetConnectionDialog dialog;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private TextView welcomeTv;
    private FloatingActionButton fabNext;
    private CircularProgressIndicator circularProgressIndicator;
    private UserProfile userProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_100);
        dialog = new InternetConnectionDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        if (getIntent() != null) {
            userProfile = createUser(getIntent());
            registerProfile(userProfile);
            registerUser(getIntent().getStringExtra("emailAddress"), getIntent().getStringExtra("password"));

            welcomeTv = findViewById(R.id.welcomeTv);
            fabNext = findViewById(R.id.fabNext);
            circularProgressIndicator = findViewById(R.id.createAccountHeaderCpi);

            circularProgressIndicator.setProgress(100, 100);
            String helloStr = getString(R.string.welcome_ro, userProfile.getFirstname());
            welcomeTv.setText(helloStr);
        }

        fabNext.setOnClickListener(v -> {
            Intent openMainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(openMainActivity);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.stopMonitor();
    }

    private UserProfile createUser(Intent intent) {
        UserProfile tempUserProfile = new UserProfile();
        String emailAddress = intent.getStringExtra("emailAddress");
        tempUserProfile.setEmailAddress(emailAddress);
        String firstname = intent.getStringExtra("firstname");
        tempUserProfile.setFirstname(firstname);
        String lastname = intent.getStringExtra("lastname");
        tempUserProfile.setLastname(lastname);
        String dateOfBirth = intent.getStringExtra("dob");
        tempUserProfile.setDateOfBirth(dateOfBirth);
        String location = intent.getStringExtra("city");
        tempUserProfile.setLocation(location);
        String gender = intent.getStringExtra("gender");
        tempUserProfile.setGender(gender);
        int height = Integer.parseInt(intent.getStringExtra("height"));
        tempUserProfile.setHeight(height);
        boolean isSmoker = intent.getBooleanExtra("smoker", false);
        tempUserProfile.setSmoker(isSmoker);
        boolean isPregnant = intent.getBooleanExtra("pregnant", false);
        tempUserProfile.setPregnant(isPregnant);
        String diseasesStr = intent.getStringExtra("diseases");
        HashMap<String, String> diseases = new HashMap<>();
        if (!diseasesStr.equals("null")) {
            String[] diseasesArray = diseasesStr.split(",");
            for (int i = 0; i < diseasesArray.length; i++) {
                diseases.put(diseasesArray[i], diseasesArray[i]);
            }
        }
        tempUserProfile.setDiseases(diseases);
        return tempUserProfile;
    }

    private void registerUser(String emailAddress, String password) {
        firebaseAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "User registration failed.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(getApplicationContext(), "You are already registered.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void registerProfile(UserProfile userProfile) {
        databaseReference = firebaseDatabase.getReference("user-profile");
        String key = databaseReference.push().getKey();
        if (key != null) {
            databaseReference.child(key).setValue(userProfile);
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), "User profile successfully created.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}


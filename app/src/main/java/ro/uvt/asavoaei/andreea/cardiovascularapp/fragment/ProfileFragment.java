package ro.uvt.asavoaei.andreea.cardiovascularapp.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.activity.LoginActivity;
import ro.uvt.asavoaei.andreea.cardiovascularapp.adapter.DiseasesCustomAdapter;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.LoadingDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.Disease;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.UserProfile;

public class ProfileFragment extends Fragment {
    private static final String TAG = ProfileFragment.class.getCanonicalName();
    private static final int DISEASES_COUNT = 8;
    private LoadingDialog loadingDialog;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private RecyclerView diseasesRecyclerView;
    private ImageView removeAccountIv;
    private Button addDiseaseBtn, editProfileBtn, saveBtn, logoutBtn;
    private EditText nameEt, emailAddressEt, dateOfBirthEt, cityEt, heightEt;
    private TextView genderAgeTv;
    private CheckBox smokerCb, pregnantCb;
    private String emailAddress;
    private UserProfile currentUserProfile = new UserProfile();
    private String[] diseases = new String[DISEASES_COUNT];
    private boolean[] checkedDiseases;
    private DiseasesCustomAdapter diseasesCustomAdapter = new DiseasesCustomAdapter(new ArrayList<>());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, null);

        loadingDialog = new LoadingDialog(getContext());

        removeAccountIv = view.findViewById(R.id.removeAccountIv);
        nameEt = view.findViewById(R.id.nameEt);
        genderAgeTv = view.findViewById(R.id.genderAgeTv);
        emailAddressEt = view.findViewById(R.id.emailEt);
        dateOfBirthEt = view.findViewById(R.id.dobEt);
        cityEt = view.findViewById(R.id.cityEt);
        heightEt = view.findViewById(R.id.heightEt);
        smokerCb = view.findViewById(R.id.smokerCb);
        pregnantCb = view.findViewById(R.id.pregnantCb);
        addDiseaseBtn = view.findViewById(R.id.addDiseaseBtn);
        editProfileBtn = view.findViewById(R.id.editProfileBtn);
        saveBtn = view.findViewById(R.id.saveBtn);
        logoutBtn = view.findViewById(R.id.logoutBtn);
        diseasesRecyclerView = view.findViewById(R.id.diseasesRv);

        if (firebaseAuth.getCurrentUser() != null) {
            emailAddress = firebaseAuth.getCurrentUser().getEmail();
            disableUI();
            new PumpDataTask().execute();
            editProfileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAccountIv.setVisibility(View.VISIBLE);
                    addDiseaseBtn.setVisibility(View.VISIBLE);
                    editProfileBtn.setVisibility(View.GONE);
                    saveBtn.setVisibility(View.VISIBLE);
                    enableUI();

                }
            });
            addDiseaseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final List<String> diseasesList = Arrays.asList(diseases); //Default diseases list
                    final List<String> selectedDiseasesList = new ArrayList<>();
                    AlertDialog displayDiseasesList = new AlertDialog.Builder(getActivity())
                            .setTitle("Alegeți afecțiunile de care suferiți")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (int i = 0; i < diseasesList.size(); i++) {
                                        if (checkedDiseases[i]) {
                                            selectedDiseasesList.add(diseasesList.get(i));
                                        }
                                    }
                                    diseasesCustomAdapter.setDiseasesList(selectedDiseasesList);
                                    diseasesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    diseasesRecyclerView.setAdapter(diseasesCustomAdapter);
                                }
                            })
                            .setNegativeButton("Anulează", null)
                            .setMultiChoiceItems(diseases, checkedDiseases, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    checkedDiseases[which] = isChecked;
                                }
                            })
                            .create();
                    displayDiseasesList.show();
                }
            });
            diseasesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            diseasesRecyclerView.setAdapter(diseasesCustomAdapter);

            removeAccountIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog askForRemoval = new AlertDialog.Builder(getActivity())
                            .setMessage("Sunteți sigur că doriți să eliminați acest cont?")
                            .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Query removeAccountAndProfileByEmail = databaseReference.child("user-profile")
                                            .orderByChild("emailAddress").equalTo(emailAddress);
                                    removeAccountAndProfileByEmail.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot != null) {
                                                        firebaseAuth.getCurrentUser().delete()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            snapshot.getRef().removeValue();
                                                                            Toast.makeText(getContext(), "Your account has been removed.", Toast.LENGTH_SHORT).show();
                                                                            Intent loginActivity = new Intent(getContext(), LoginActivity.class);
                                                                            startActivity(loginActivity);
                                                                        } else {
                                                                            Toast.makeText(getContext(), "Your account cannot be removed.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Nu", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();
                    askForRemoval.show();
                }
            });

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveData();
                    removeAccountIv.setVisibility(View.GONE);
                    addDiseaseBtn.setVisibility(View.GONE);
                    editProfileBtn.setVisibility(View.VISIBLE);
                    saveBtn.setVisibility(View.GONE);
                    disableUI();
                }
            });

            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firebaseAuth.signOut();
                    Intent openLoginActivity = new Intent(getContext(), LoginActivity.class);
                    openLoginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(openLoginActivity);
                }
            });

        }

        return view;
    }

    private void setData() {
        nameEt.setText(currentUserProfile.getFirstname() + " " + currentUserProfile.getLastname());
        genderAgeTv.setText(currentUserProfile.getGender() + ", " + getAge(currentUserProfile.getDateOfBirth()) + " ani");
        emailAddressEt.setText(currentUserProfile.getEmailAddress());
        dateOfBirthEt.setText(currentUserProfile.getDateOfBirth());
        cityEt.setText(currentUserProfile.getLocation());
        heightEt.setText(String.valueOf(currentUserProfile.getHeight()));
        smokerCb.setChecked(currentUserProfile.isSmoker());
        pregnantCb.setChecked(currentUserProfile.isPregnant());
        if (currentUserProfile.getDiseases() != null && currentUserProfile.getDiseases().size() > 0) {
            List<String> diseasesList = new ArrayList<>(currentUserProfile.getDiseases().keySet());
            int i = 0;
            for (String disease : diseasesList) {
                diseases[i] = disease;
                i++;
            }
            for (int j = 0; j < diseases.length; j++) {
                if (diseasesList.contains(diseases[j])) {
                    checkedDiseases[j] = true;
                }
            }
            diseasesCustomAdapter.setDiseasesList(diseasesList);
            diseasesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            diseasesRecyclerView.setAdapter(diseasesCustomAdapter);
        }
        loadingDialog.dismissDialog();

    }

    private void getDiseasesArray() {
        Query getDiseasesQuery = databaseReference.child("disease").orderByChild("diseaseName");
        getDiseasesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int i = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        diseases[i] = snapshot.getValue(Disease.class).getName();
                        Log.d(TAG, "Disease: " + diseases[i]);
                        i++;
                    }
                    checkedDiseases = new boolean[diseases.length];
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void disableUI() {
        nameEt.setEnabled(false);
        emailAddressEt.setEnabled(false);
        dateOfBirthEt.setEnabled(false);
        cityEt.setEnabled(false);
        heightEt.setEnabled(false);
        smokerCb.setClickable(false);
        pregnantCb.setClickable(false);
    }

    private void enableUI() {
        nameEt.setEnabled(true);
        emailAddressEt.setEnabled(true);
        dateOfBirthEt.setEnabled(true);
        cityEt.setEnabled(true);
        heightEt.setEnabled(true);
        smokerCb.setClickable(true);
        pregnantCb.setClickable(true);
    }

    private void getCurrentUserProfile() {
        Query getUserProfileByEmail = databaseReference.child("user-profile").orderByChild("emailAddress").equalTo(emailAddress);
        getUserProfileByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserProfile snapshotUser = snapshot.getValue(UserProfile.class);
                        if (snapshotUser != null) {
                            currentUserProfile.setEmailAddress(snapshotUser.getEmailAddress());
                            currentUserProfile.setFirstname(snapshotUser.getFirstname());
                            currentUserProfile.setLastname(snapshotUser.getLastname());
                            currentUserProfile.setDateOfBirth(snapshotUser.getDateOfBirth());
                            currentUserProfile.setLocation(snapshotUser.getLocation());
                            currentUserProfile.setGender(snapshotUser.getGender());
                            currentUserProfile.setHeight(snapshotUser.getHeight());
                            currentUserProfile.setSmoker(snapshotUser.isSmoker());
                            currentUserProfile.setPregnant(snapshotUser.isPregnant());
                            if (snapshotUser.getDiseases() != null) {
                                currentUserProfile.setDiseases(snapshotUser.getDiseases());
                            }
                        }
                    }
                    setData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int getAge(String dateOfBirth) {
        try {
            Date currentDate = new Date();
            SimpleDateFormat getYear = new SimpleDateFormat("YYYY");
            int currentYear = Integer.parseInt(getYear.format(currentDate));
            Date birthdayDate = new SimpleDateFormat("dd-MM-yyyy").parse(dateOfBirth);
            int birthYear = Integer.parseInt(getYear.format(birthdayDate));
            return currentYear - birthYear;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void saveData() {
        currentUserProfile.setEmailAddress(emailAddressEt.getText().toString());
        String[] fullname = nameEt.getText().toString().split(" ");
        currentUserProfile.setFirstname(fullname[0]);
        currentUserProfile.setLastname(fullname[1]);
        currentUserProfile.setDateOfBirth(dateOfBirthEt.getText().toString().replaceAll("/", "-"));
        currentUserProfile.setLocation(cityEt.getText().toString());
        currentUserProfile.setHeight(Integer.parseInt(heightEt.getText().toString()));
        HashMap<String, String> diseasesMap = new HashMap<>();
        for (String disease : diseasesCustomAdapter.getDiseasesList()) {
            diseasesMap.put(disease, disease);
        }
        currentUserProfile.setDiseases(diseasesMap);
        currentUserProfile.setSmoker(smokerCb.isChecked());
        currentUserProfile.setPregnant(pregnantCb.isChecked());
        Query getUserProfileByEmail = databaseReference.child("user-profile").orderByChild("emailAddress").equalTo(emailAddress);
        getUserProfileByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(snapshot.getKey() != null){
                            databaseReference.child("user-profile").child(snapshot.getKey()).setValue(currentUserProfile);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private class PumpDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loadingDialog.showDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getDiseasesArray();
            getCurrentUserProfile();
            return null;
        }
    }
}

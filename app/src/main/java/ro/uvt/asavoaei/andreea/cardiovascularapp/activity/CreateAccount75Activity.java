package ro.uvt.asavoaei.andreea.cardiovascularapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.adapter.DiseasesCustomAdapter;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.InternetConnectionDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.LoadingDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.Disease;

public class CreateAccount75Activity extends AppCompatActivity {
    private static final String TAG = CreateAccount75Activity.class.getCanonicalName();
    private static final int DISEASES_COUNT = 8;
    private InternetConnectionDialog dialog;
    private LoadingDialog loadingDialog;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference diseaseDbReference = firebaseDatabase.getReference();
    private RecyclerView diseasesRecyclerView;
    private FloatingActionButton fabNext;
    private CircularProgressIndicator circularProgressIndicator;
    private String[] diseases = new String[DISEASES_COUNT];
    private boolean[] checkedDiseases;
    private DiseasesCustomAdapter diseasesCustomAdapter = new DiseasesCustomAdapter(new ArrayList<>());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_75);

        loadingDialog = new LoadingDialog(this);
        dialog = new InternetConnectionDialog(this);

        new PumpDataTask().execute();
        final Intent intent = getIntent();

        diseasesRecyclerView = findViewById(R.id.diseasesRv);
        fabNext = findViewById(R.id.fabNext100);
        circularProgressIndicator = findViewById(R.id.createAccountHeaderCpi);
        Button addDiseaseButton = findViewById(R.id.addDiseaseBtn);

        addDiseaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> diseasesList = Arrays.asList(diseases); //Default diseases list
                final List<String> selectedDiseasesList = new ArrayList<>();
                AlertDialog displayDiseasesList = new AlertDialog.Builder(CreateAccount75Activity.this)
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
                                diseasesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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
        diseasesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        diseasesRecyclerView.setAdapter(diseasesCustomAdapter);

        circularProgressIndicator.setProgress(75, 100);

        fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(getApplicationContext(), CreateAccount100Activity.class);
                open.putExtra("emailAddress", intent.getStringExtra("emailAddress"));
                open.putExtra("password", intent.getStringExtra("password"));
                open.putExtra("firstname", intent.getStringExtra("firstname"));
                open.putExtra("lastname", intent.getStringExtra("lastname"));
                open.putExtra("city", intent.getStringExtra("city"));
                open.putExtra("dob", intent.getStringExtra("dob"));
                open.putExtra("gender", intent.getStringExtra("gender"));
                open.putExtra("height", intent.getStringExtra("height"));
                open.putExtra("smoker", intent.getBooleanExtra("smoker", false));
                open.putExtra("pregnant", intent.getBooleanExtra("pregnant", false));

                open.putExtra("diseases", getSelectedDiseases(diseasesCustomAdapter));
                startActivity(open);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getDiseasesArray() {
        Query query = diseaseDbReference.child("disease").orderByChild("diseaseName");
        query.addValueEventListener(new ValueEventListener() {
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

    private String getSelectedDiseases(DiseasesCustomAdapter diseasesCustomAdapter) {
        List<String> diseasesList = diseasesCustomAdapter.getDiseasesList();
        StringBuilder diseases = new StringBuilder();
        if (diseasesList.size() > 0) {
            for (int i = 0; i < diseasesList.size(); i++) {
                if (i == diseasesList.size() - 1) {
                    diseases.append(diseasesList.get(i));
                } else {
                    diseases.append(diseasesList.get(i)).append(",");
                }
            }
        } else {
            diseases.append("null");
        }
        return diseases.toString();
    }

    private class PumpDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loadingDialog.showDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getDiseasesArray();
            return null;
        }
    }

}
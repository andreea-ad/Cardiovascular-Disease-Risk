package ro.uvt.asavoaei.andreea.cardiovascularapp.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.adapter.MedicationCustomAdapter;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.LoadingDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.CardioRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.Medication;

public class CollectDataFragment extends Fragment {
    private static final String TAG = CollectDataFragment.class.getCanonicalName();
    private static final int systolicBPMinimumValue = 70;
    private static final int systolicBPMaximumValue = 190;
    private static final int systolicBPDefaultValue = 110;
    private static final int diastolicBPMinimumValue = 40;
    private static final int diastolicBPMaximumValue = 100;
    private static final int diastolicBPDefaultValue = 70;
    private static final int pulseMinimumValue = 40;
    private static final int pulseMaximumValue = 120;
    private static final int pulseDefaultValue = 70;
    private static final double weightMinimumValue = 40.0;
    private static final double weightMaximumValue = 635.0;
    private static final int cholesterolMinimumValue = 100;
    private static final int cholesterolMaximumValue = 270;
    private static final int MEDICATION_COUNT = 77;
    private final Calendar myCalendar = Calendar.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private LoadingDialog loadingDialog;
    private EditText recordingDateEt, recordingHourEt, cholesterolEt, weightEt;
    private String dateFormatter = "dd-MM-yyyy";
    private String hourFormatter = "HH:mm";
    private NumberPicker systolicNumberPicker, diastolicNumberPicker, pulseNumberPicker;
    private CheckBox alcoholUsage;
    private Button addMedicationButton, saveButton;
    private RecyclerView medicationRecyclerView;
    private MedicationCustomAdapter medicationCustomAdapter = new MedicationCustomAdapter(new ArrayList<>());
    private boolean isRecordingDateValid = false;
    private boolean isRecordingHourValid = false;
    private boolean isCholesterolValid = false;
    private boolean isWeightValid = false;
    private int lastCholesterol = 0;
    private int lastWeight = 0;
    private HashMap<String, String> selectedMedication = new HashMap<>();
    private CardioRecord cardioRecordToRegister = new CardioRecord();
    private String currentUserEmailAddress;
    private String[] medications = new String[MEDICATION_COUNT];
    private boolean[] checkedMedication;
    private List<String> selectedMedicationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collect_data, null);

        loadingDialog = new LoadingDialog(getContext());

        recordingDateEt = view.findViewById(R.id.recordingDateEt);
        recordingHourEt = view.findViewById(R.id.recordingHourEt);
        alcoholUsage = view.findViewById(R.id.alcoholUsageCb);
        cholesterolEt = view.findViewById(R.id.cholesterolEt);
        weightEt = view.findViewById(R.id.weightEt);
        addMedicationButton = view.findViewById(R.id.addMedicationBtn);
        medicationRecyclerView = view.findViewById(R.id.medicationRv);
        saveButton = view.findViewById(R.id.saveBtn);

        if (firebaseAuth.getCurrentUser() != null) {

            currentUserEmailAddress = firebaseAuth.getCurrentUser().getEmail();
            new PumpDataTask().execute();

            final DatePickerDialog.OnDateSetListener date = (view1, year, monthOfYear, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDate();
            };

            recordingDateEt.setOnClickListener(v -> new DatePickerDialog(getContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show());

            recordingDateEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!checkDate(s.toString()) && !checkHour(s.toString(), recordingHourEt.getText().toString())) {
                        recordingDateEt.setError("Dată invalidă.");
                        recordingDateEt.requestFocus();
                        isRecordingDateValid = false;
                        recordingHourEt.setError("Oră invalidă");
                        recordingHourEt.requestFocus();
                        isRecordingHourValid = false;
                    } else {
                        recordingDateEt.setError(null);
                        isRecordingDateValid = true;
                        recordingHourEt.setError(null);
                        isRecordingHourValid = true;
                    }
                }
            });

            recordingHourEt.setOnClickListener(v -> new TimePickerDialog(getContext(), (view12, hourOfDay, minute) -> {
                String selectedTime = checkDigit(hourOfDay) + ":" + checkDigit(minute);
                recordingHourEt.setText(selectedTime);
            }, 0, 0, true).show());

            recordingHourEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String selectedDate = recordingDateEt.getText().toString();

                    if (!checkHour(selectedDate, s.toString())) {
                        recordingHourEt.setError("Oră invalidă");
                        recordingHourEt.requestFocus();
                        isRecordingHourValid = false;
                    } else {
                        recordingHourEt.setError(null);
                        isRecordingHourValid = true;
                    }
                }
            });

            systolicNumberPicker = view.findViewById(R.id.sysNp);
            systolicNumberPicker.setMinValue(systolicBPMinimumValue);
            systolicNumberPicker.setMaxValue(systolicBPMaximumValue);
            systolicNumberPicker.setValue(systolicBPDefaultValue);

            diastolicNumberPicker = view.findViewById(R.id.diaNp);
            diastolicNumberPicker.setMinValue(diastolicBPMinimumValue);
            diastolicNumberPicker.setMaxValue(diastolicBPMaximumValue);
            diastolicNumberPicker.setValue(diastolicBPDefaultValue);

            pulseNumberPicker = view.findViewById(R.id.hrNp);
            pulseNumberPicker.setMinValue(pulseMinimumValue);
            pulseNumberPicker.setMaxValue(pulseMaximumValue);
            pulseNumberPicker.setValue(pulseDefaultValue);

            weightEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().isEmpty()) {
                        weightEt.setError(null);
                        isWeightValid = checkWeight(Integer.parseInt(s.toString()));
                    } else {
                        weightEt.setError("Valoare invalidă");
                    }
                }
            });

            cholesterolEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().isEmpty()) {
                        cholesterolEt.setError(null);
                        isCholesterolValid = checkCholesterol(Integer.parseInt(s.toString()));
                    } else {
                        cholesterolEt.setError("Valoare invalidă");
                    }
                }
            });

            addMedicationButton.setOnClickListener(v -> {
                final List<String> medicationList = Arrays.asList(medications); //Default medication list
                AlertDialog displayMedicationList = new AlertDialog.Builder(getActivity())
                        .setTitle("Alegeți medicația administrată")
                        .setPositiveButton("Ok", (dialog, which) -> {
                            int medicationCounter = 0;
                            for (int i = 0; i < medicationList.size(); i++) {
                                if (checkedMedication[i]) {
                                    medicationCounter++;
                                    selectedMedicationList.add(medicationList.get(i));
                                    selectedMedication.put("medication" + medicationCounter, medicationList.get(i));
                                }
                            }
                            medicationCustomAdapter.setMedicationList(selectedMedicationList);
                            medicationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            medicationRecyclerView.setAdapter(medicationCustomAdapter);
                        })
                        .setNegativeButton("Anulează", null)
                        .setMultiChoiceItems(medications, checkedMedication, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                checkedMedication[which] = isChecked;
                            }
                        })
                        .create();
                displayMedicationList.show();

            });

            medicationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            medicationRecyclerView.setAdapter(medicationCustomAdapter);

            saveButton.setOnClickListener(v -> {
                if (getContext() != null) {
                    Log.d(TAG, "Weight: " + weightEt.getText());
                    if (isCholesterolValid && isWeightValid && isRecordingDateValid && isRecordingHourValid) {
                        setDataToRecord();
                        new SaveDataTask().execute();
                    }
                }
            });
        }
        return view;
    }

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatter);
        String selectedDate = sdf.format(myCalendar.getTime());
        recordingDateEt.setText(selectedDate);
    }

    private String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    private boolean checkDate(String selectedDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatter);
            Date date = sdf.parse(selectedDate);
            LocalDate selected = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate currentDate = LocalDate.now();
            if (selected.isAfter(currentDate)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean checkHour(String selectedDate, String selectedHour) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatter);
            Date date = sdf.parse(selectedDate);
            LocalDate selectedDateL = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate currentDateL = LocalDate.now();
            sdf = new SimpleDateFormat(hourFormatter);
            Date time = sdf.parse(selectedHour);
            LocalTime selectedTimeL = time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime currentTimeL = LocalTime.now();
            LocalTime selHour = LocalTime.of(selectedTimeL.getHour(), selectedTimeL.getMinute());
            LocalTime currHour = LocalTime.of(currentTimeL.getHour(), currentTimeL.getMinute());
            if (selectedDateL.isAfter(currentDateL) || selectedDateL.isEqual(currentDateL)) {
                if (selHour.isAfter(currHour)) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    private boolean checkCholesterol(int cholesterolValue) {
        boolean isValid = false;
        if (cholesterolValue >= cholesterolMinimumValue && cholesterolValue <= cholesterolMaximumValue) {
            isValid = true;
        }

        if (!isValid) {
            cholesterolEt.setError("Colesterolul total trebuie să fie între " + cholesterolMinimumValue + " mg/dL și " + cholesterolMaximumValue + " mg/dL.");
            cholesterolEt.requestFocus();
        } else {
            cholesterolEt.setError(null);
        }

        return isValid;
    }

    private boolean checkWeight(int weightValue) {
        boolean isValid = false;
        if (weightValue >= weightMinimumValue && weightValue <= weightMaximumValue) {
            isValid = true;
        }

        if (!isValid) {
            weightEt.setError("Greutatea trebuie să fie între " + weightMinimumValue + " kg și " + weightMaximumValue + " kg.");
            weightEt.requestFocus();
        } else {
            weightEt.setError(null);
        }

        return isValid;
    }

    private void getMedicationArray() {
        Query medicationQuery = databaseReference.child("medication").orderByChild("medicationName");
        medicationQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int i = 0;
                    medications = new String[MEDICATION_COUNT];
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Medication medication = snapshot.getValue(Medication.class);
                        if (medication != null && medication.getMedicationName() != null) {
                            medications[i] = medication.getMedicationName();
                        }
                        i++;
                    }
                    checkedMedication = new boolean[medications.length];
                    setMedication();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setDefaultData() {
        setCurrentDate();
        setCurrentHour();
        setCholesterol();
        setWeight();
        //setMedication();
        loadingDialog.dismissDialog();
    }

    private String getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateFormatter);
        LocalDate localDate = LocalDate.now();
        return localDate.format(dtf);
    }

    private void setCurrentDate() {
        recordingDateEt.setText(getCurrentDate());
    }

    private String getCurrentHour() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(hourFormatter);
        LocalTime localTime = LocalTime.now();
        return dtf.format(localTime);
    }

    private void setCurrentHour() {
        recordingHourEt.setText(getCurrentHour());
    }

    private void setCholesterol() {
        Query getCardioRecordsByEmail = databaseReference.child("cardio-record").orderByChild("emailAddress").equalTo(currentUserEmailAddress).limitToLast(1);
        getCardioRecordsByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CardioRecord cardioRecord = snapshot.getValue(CardioRecord.class);
                        if (cardioRecord != null) {
                            lastCholesterol = cardioRecord.getCholesterol();
                            cholesterolEt.setText(String.valueOf(lastCholesterol));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setWeight() {
        Query getCardioRecordsByEmail = databaseReference.child("cardio-record").orderByChild("emailAddress").equalTo(currentUserEmailAddress).limitToLast(1);
        getCardioRecordsByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CardioRecord cardioRecord = snapshot.getValue(CardioRecord.class);
                        if (cardioRecord != null) {
                            lastWeight = cardioRecord.getWeight();
                            weightEt.setText(String.valueOf(lastWeight));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setMedication() {
        Query getCardioRecordsByEmail = databaseReference.child("cardio-record").orderByChild("emailAddress").equalTo(currentUserEmailAddress).limitToLast(1);
        getCardioRecordsByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    selectedMedicationList = new ArrayList<>();
                    selectedMedication = new HashMap<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CardioRecord cardioRecord = snapshot.getValue(CardioRecord.class);
                        if (cardioRecord != null && cardioRecord.getMedication() != null) {
                            selectedMedication = cardioRecord.getMedication();
                            selectedMedicationList = new ArrayList<>(selectedMedication.values());
                            for (int i = 0; i < medications.length; i++) {
                                if (selectedMedicationList.contains(medications[i])) {
                                    Log.d(TAG, "Medication: " + medications[i]);
                                    checkedMedication[i] = true;
                                }
                            }
                            medicationCustomAdapter.setMedicationList(selectedMedicationList);
                            medicationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            medicationRecyclerView.setAdapter(medicationCustomAdapter);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setDataToRecord() {
        cardioRecordToRegister.setEmailAddress(currentUserEmailAddress);
        cardioRecordToRegister.setSystolicBP(systolicNumberPicker.getValue());
        cardioRecordToRegister.setDiastolicBP(diastolicNumberPicker.getValue());
        cardioRecordToRegister.setPulse(pulseNumberPicker.getValue());
        cardioRecordToRegister.setCholesterol(Integer.parseInt(cholesterolEt.getText().toString()));
        cardioRecordToRegister.setWeight(Integer.parseInt(weightEt.getText().toString()));
        cardioRecordToRegister.setAlcoholUse(alcoholUsage.isChecked());
        cardioRecordToRegister.setRecordingDate(recordingDateEt.getText().toString());
        cardioRecordToRegister.setRecordingHour(recordingHourEt.getText().toString());
        cardioRecordToRegister.setMedication(selectedMedication);
    }

    private void saveData() {
        if (getContext() != null) {
            databaseReference = firebaseDatabase.getReference("cardio-record");
            String key = databaseReference.push().getKey();
            if (key != null) {
                databaseReference.child(key).setValue(cardioRecordToRegister, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError == null){
                            Toast.makeText(getContext(), "Datele au fost înregistrate cu succes.", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getContext(), "Datele nu au putut fi înregistrate.", Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismissDialog();
                    }
                });
            }
        }
    }

    private class PumpDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loadingDialog.showDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getMedicationArray();
            setDefaultData();
            return null;
        }
    }

    private class SaveDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loadingDialog.showDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            saveData();
            return null;
        }
    }
}
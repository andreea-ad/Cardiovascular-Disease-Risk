package ro.uvt.asavoaei.andreea.cardiovascularapp.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.adapter.RecordsCustomAdapter;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.LoadingDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.CardioAndWeatherRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.CardioRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.UserProfile;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.WeatherRecord;

public class HistoryFragment extends Fragment {
    private static final String TAG = HistoryFragment.class.getCanonicalName();
    private static final int RECORDING_DATE = 1;
    private static final int BLOOD_PRESSURE = 2;
    private static final int PULSE = 3;
    private static final int SORT_ASCENDING = 4;
    private static final int SORT_DESCENDING = 5;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseAuth firebaseAuth;
    private RecyclerView historyRecyclerView;
    private RadioGroup timeRg, sortRg;
    private LoadingDialog loadingDialog;
    private List<CardioAndWeatherRecord> cardioAndWeatherRecordList = new ArrayList<>();
    private List<CardioAndWeatherRecord> listToSort = new ArrayList<>();
    private RecordsCustomAdapter recordsCustomAdapter;
    private String emailAddress = "";
    private String userCity = "";
    private boolean isSmoker = false;
    private boolean isPregnant = false;
    private int userHeight = 0;

    private List<CardioRecord> allCardioRecordsByUser = new ArrayList<>();
    private List<WeatherRecord> allWeatherRecordsByCity = new ArrayList<>();

    private Set<WeatherRecord> filteredWeatherRecords = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, null);
        loadingDialog = new LoadingDialog(getContext());
        historyRecyclerView = v.findViewById(R.id.cardioHistoryRv);
        timeRg = v.findViewById(R.id.timeRg);
        sortRg = v.findViewById(R.id.sortRg);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            emailAddress = firebaseAuth.getCurrentUser().getEmail();
            new PumpDataTask().execute();
            timeRg.setOnCheckedChangeListener((group, checkedId) -> {
                int id = sortRg.getCheckedRadioButtonId();
                int option = 4;
                switch (id) {
                    case R.id.sortAscendingRb:
                        option = SORT_ASCENDING;
                        break;
                    case R.id.sortDescendingRb:
                        option = SORT_DESCENDING;
                        break;
                }
                /**
                 * Sort list of cardio and weather records by selected filter
                 */
                switch (checkedId) {
                    case R.id.recordingDateRb:
                        cardioAndWeatherRecordList = sortBy(listToSort, RECORDING_DATE, option);
                        break;
                    case R.id.bloodPressureRb:
                        cardioAndWeatherRecordList = sortBy(listToSort, BLOOD_PRESSURE, option);
                        break;
                    case R.id.pulseRb:
                        cardioAndWeatherRecordList = sortBy(listToSort, PULSE, option);
                        break;
                }
                setAdapter();
            });

            sortRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int id = timeRg.getCheckedRadioButtonId();
                    int option = 4;
                    switch (checkedId) {
                        case R.id.sortAscendingRb:
                            option = SORT_ASCENDING;
                            break;
                        case R.id.sortDescendingRb:
                            option = SORT_DESCENDING;
                            break;
                    }
                    /**
                     * Sort list of cardio and weather records by selected filter
                     */
                    switch (id) {
                        case R.id.recordingDateRb:
                            cardioAndWeatherRecordList = sortBy(listToSort, RECORDING_DATE, option);
                            break;
                        case R.id.bloodPressureRb:
                            cardioAndWeatherRecordList = sortBy(listToSort, BLOOD_PRESSURE, option);
                            break;
                        case R.id.pulseRb:
                            cardioAndWeatherRecordList = sortBy(listToSort, PULSE, option);
                            break;
                    }
                    setAdapter();
                }
            });

        }

        return v;
    }

    /**
     * Initialize list with all the data from the DB and sort it ascending by recording date
     */
    private void initializeList() {
        cardioAndWeatherRecordList = sortBy(listToSort, RECORDING_DATE, SORT_ASCENDING);
        if (!cardioAndWeatherRecordList.isEmpty()) {
            setAdapter();
        } else {
            Toast.makeText(getContext(), "Nu au fost găsite date în baza de date.", Toast.LENGTH_LONG).show();
        }
        loadingDialog.dismissDialog();
    }

    private List<CardioAndWeatherRecord> sortBy(List<CardioAndWeatherRecord> list, int sortBy, int order) {
        switch (sortBy) {
            case RECORDING_DATE:
                Collections.sort(list, (CardioAndWeatherRecord c1, CardioAndWeatherRecord c2) -> sortByRecordingDate(c1, c2, order));
                break;
            case BLOOD_PRESSURE:
                Collections.sort(list, (CardioAndWeatherRecord c1, CardioAndWeatherRecord c2) -> sortByBloodPressure(c1, c2, order));
                break;
            case PULSE:
                Collections.sort(list, (CardioAndWeatherRecord c1, CardioAndWeatherRecord c2) -> sortByPulse(c1, c2, order));
                break;
        }
        return list;
    }

    /**
     * Compare recording dates
     * @param c1
     * @param c2
     * @param order
     * @return 0 (equal dates), 1 (date no. 1 is after date no. 2), -1 (date no. 1 is before date no. 2)
     */
    private int sortByRecordingDate(CardioAndWeatherRecord c1, CardioAndWeatherRecord c2, int order) {
        String time1 = c1.getRecordingDate() + " " + c1.getRecordingHour();
        String time2 = c2.getRecordingDate() + " " + c2.getRecordingHour();

        try {
            Date recordingDate1 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(time1);
            Date recordingDate2 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(time2);
            switch (order) {
                case SORT_ASCENDING:
                    boolean d2Befored1 = recordingDate2.before(recordingDate1);
                    boolean d2Afterd1 = recordingDate2.after(recordingDate1);
                    if (!d2Befored1 && !d2Afterd1) {
                        return 0;
                    } else if (d2Befored1) {
                        return 1;
                    } else {
                        return -1;
                    }
                case SORT_DESCENDING:
                    boolean d1Befored2 = recordingDate1.before(recordingDate2);
                    boolean d1Afterd2 = recordingDate1.after(recordingDate2);
                    if (!d1Befored2 && !d1Afterd2) {
                        return 0;
                    } else if (d1Befored2) {
                        return 1;
                    } else {
                        return -1;
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Compare blood pressure values
     * @param c1
     * @param c2
     * @param order
     * @return positive (blood pressure no. 1 is higher than blood pressure no. 2), negative (blood pressure no. 1 is lower than blood pressure no. 2) value or 0 (equal blood pressure values)
     */
    private int sortByBloodPressure(CardioAndWeatherRecord c1, CardioAndWeatherRecord c2, int order) {
        switch (order) {
            case SORT_ASCENDING:
                if (c1.getSystolicBP() == c2.getSystolicBP())
                    return c1.getDiastolicBP() - c2.getDiastolicBP();
                else
                    return c1.getSystolicBP() - c2.getSystolicBP();
            case SORT_DESCENDING:
                if (c2.getSystolicBP() == c1.getSystolicBP())
                    return c2.getDiastolicBP() - c1.getDiastolicBP();
                else
                    return c2.getSystolicBP() - c1.getSystolicBP();
        }
        return 0;
    }

    /**
     * Compare pulse values
     * @param c1
     * @param c2
     * @param order
     * @return positive (pulse no. 1 is higher than pulse no. 2), negative (pulse no. 1 is lower than pulse no. 2) value or 0 (equal pulse values)
     */
    private int sortByPulse(CardioAndWeatherRecord c1, CardioAndWeatherRecord c2, int order) {
        switch (order) {
            case SORT_ASCENDING:
                return c1.getPulse() - c2.getPulse();
            case SORT_DESCENDING:
                return c2.getPulse() - c1.getPulse();
        }
        return 0;
    }

    private void setAdapter() {
        recordsCustomAdapter = new RecordsCustomAdapter(cardioAndWeatherRecordList);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyRecyclerView.setAdapter(recordsCustomAdapter);
    }

    /**
     * Retrieve all cardio records from DB belonging to the authenticated user
     */
    private void getCardio() {
        Query getCardioRecordsByEmail = databaseReference.child("cardio-record").orderByChild("emailAddress").equalTo(emailAddress);
        getCardioRecordsByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    allCardioRecordsByUser = new ArrayList<>();
                    for (final DataSnapshot cardio : dataSnapshot.getChildren()) {
                        CardioRecord cardioRecord = cardio.getValue(CardioRecord.class);
                        if (cardioRecord != null) {
                            allCardioRecordsByUser.add(cardioRecord);
                        }
                    }
                    getWeather();
                } else {
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * Retrieve weather data
     * Filter the data by timestamp (keep only the weather data that can be mapped to a cardio record)
     */
    private void getWeather() {
        Query getWeatherDataByCity = databaseReference.child("weather-record").orderByChild("city").equalTo(userCity);
        getWeatherDataByCity.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    allWeatherRecordsByCity = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        WeatherRecord weatherRecord = snapshot.getValue(WeatherRecord.class);
                        if (weatherRecord != null) {
                            allWeatherRecordsByCity.add(weatherRecord);
                        }
                    }
                    filteredWeatherRecords = new HashSet<>();
                    for (CardioRecord c : allCardioRecordsByUser) {
                        for (WeatherRecord w : allWeatherRecordsByCity) {
                            String recHourC = c.getRecordingHour().split(":")[0];
                            String recHourW = w.getRecordingHour().split(":")[0];
                            if (c.getRecordingDate().equals(w.getRecordingDate()) && recHourC.equals(recHourW)) {    // filter weather records by timestamp
                                filteredWeatherRecords.add(w);
                            }
                        }
                    }
                    concatenateLists();
                } else {
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Retrieve user profile data from the DB
     */
    private void setUserData() {
        Query getUserProfileByEmail = databaseReference.child("user-profile").orderByChild("emailAddress").equalTo(emailAddress).limitToFirst(1);
        getUserProfileByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        final UserProfile userProfile = user.getValue(UserProfile.class);
                        if (userProfile != null) {
                            userCity = userProfile.getLocation();
                            isSmoker = userProfile.isSmoker();
                            isPregnant = userProfile.isPregnant();
                            userHeight = userProfile.getHeight();
                        }
                    }
                    getCardio();
                } else {
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Create a list containing the cardio records and weather records mapped by timestamp
     */
    private void concatenateLists() {
        if (!allCardioRecordsByUser.isEmpty() && !filteredWeatherRecords.isEmpty()) {
            for (CardioRecord c : allCardioRecordsByUser) {
                for (WeatherRecord w : filteredWeatherRecords) {
                    if (c.getRecordingDate().equals(w.getRecordingDate()) && c.getRecordingHour().split(":")[0].equals(w.getRecordingHour().split(":")[0])) {
                        CardioAndWeatherRecord cardioAndWeatherRecord = new CardioAndWeatherRecord();
                        cardioAndWeatherRecord.setSystolicBP(c.getSystolicBP());
                        cardioAndWeatherRecord.setDiastolicBP(c.getDiastolicBP());
                        cardioAndWeatherRecord.setPulse(c.getPulse());
                        cardioAndWeatherRecord.setCholesterol(c.getCholesterol());
                        cardioAndWeatherRecord.setBMI(c.getWeight() / (int) Math.pow((double) userHeight / 100, 2));
                        cardioAndWeatherRecord.setPregnant(isPregnant);
                        cardioAndWeatherRecord.setSmoker(isSmoker);
                        cardioAndWeatherRecord.setTemperature(w.getTemperature());
                        cardioAndWeatherRecord.setHumidity(w.getHumidity());
                        cardioAndWeatherRecord.setPressure(w.getPressure());
                        cardioAndWeatherRecord.setNebulosity(w.getNebulosity());
                        cardioAndWeatherRecord.setRecordingDate(c.getRecordingDate());
                        cardioAndWeatherRecord.setRecordingHour(c.getRecordingHour());
                        listToSort.add(cardioAndWeatherRecord);
                    }
                }
            }
            initializeList();
        }
    }

    /**
     * Background task to retrieve data from the DB
     */
    private class PumpDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loadingDialog.showDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            setUserData();
            return null;
        }

    }
}

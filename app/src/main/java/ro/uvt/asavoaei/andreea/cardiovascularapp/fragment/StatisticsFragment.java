package ro.uvt.asavoaei.andreea.cardiovascularapp.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.LoadingDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.CardioRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.UserProfile;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.WeatherRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureAndBMIMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureAndPressureMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureAndTemperatureMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.FloatValueFormatter;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.PulseMarkerView;

public class StatisticsFragment extends Fragment {
    private static final String TAG = StatisticsFragment.class.getName();
    private static final int BLOOD_PRESSURE = 1;
    private static final int PULSE = 2;
    private static final int HUMIDITY = 3;
    private static final int TEMPERATURE = 4;
    private static final int WIND_SPEED = 5;
    private static final int PRESSURE = 6;
    private static final String dateFormat = "dd-MM-yyyy";
    private LoadingDialog loadingDialog;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private TextView chartTitleTv, bpTempCorrelationTv, bpPressureCorrelationTv;
    private RadioGroup timeRg;
    private String emailAddress = "", city = "";
    private int height = 0;
    private Date startingDate;
    private LocalDate currentDate = LocalDate.now();
    private int numberOfEntries = 7;
    private int numberOfLabels = numberOfEntries;
    private int independentVariable = 0;
    private int dependentVariable = 0;

    private List<CardioRecord> cardioRecordList = new ArrayList<>();
    private List<WeatherRecord> weatherRecordList = new ArrayList<>();

    private ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    private LineData lineData;

    private LineChart bloodPressureLc, pulseLc, bloodPressureAndTemperatureLc, bloodPressureAndPressureLc, bloodPressureAndBMILc;
    private PieChart hypertensionStagesPc;
    private LineDataSet systolicBPDataSet = new LineDataSet(null, null);
    private LineDataSet diastolicBPDataSet = new LineDataSet(null, null);
    private LineDataSet pulseDataSet = new LineDataSet(null, null);
    private LineDataSet temperatureDataSet = new LineDataSet(null, null);
    private LineDataSet pressureDataSet = new LineDataSet(null, null);
    private LineDataSet BMIdataSet = new LineDataSet(null, null);

    private ArrayList<Entry> systolic = new ArrayList<>();
    private ArrayList<Entry> diastolic = new ArrayList<>();
    private ArrayList<Entry> pulse = new ArrayList<>();
    private ArrayList<Entry> temperature = new ArrayList<>();
    private ArrayList<Entry> pressure = new ArrayList<>();
    private ArrayList<Entry> BMI = new ArrayList<>();

    private Map<Long, Integer> systolicObj = new HashMap<>();
    private Map<Long, Integer> diastolicObj = new HashMap<>();
    private Map<Long, Integer> pulseObj = new HashMap<>();
    private Map<Long, Float> temperatureObj = new HashMap<>();
    private Map<Long, Float> pressureObj = new HashMap<>();
    private Map<Long, Integer> BMIObj = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, null);
        loadingDialog = new LoadingDialog(getContext());
        chartTitleTv = view.findViewById(R.id.bpTempChartTv);
        bloodPressureLc = view.findViewById(R.id.bpChart);
        pulseLc = view.findViewById(R.id.pulseChart);
        bloodPressureAndTemperatureLc = view.findViewById(R.id.bpTempChart);
        bloodPressureAndPressureLc = view.findViewById(R.id.bpPressureChart);
        bloodPressureAndBMILc = view.findViewById(R.id.bpBMIChart);
        hypertensionStagesPc = view.findViewById(R.id.bpStagesChart);
        timeRg = view.findViewById(R.id.timeRg);
        bpTempCorrelationTv = view.findViewById(R.id.bpTempCorrelationTv);
        bpPressureCorrelationTv = view.findViewById(R.id.bpPressureCorrelationTv);

        if (firebaseAuth.getCurrentUser() != null) {
            emailAddress = firebaseAuth.getCurrentUser().getEmail();
            //displayVariablePicker();
            startingDate = Date.from(currentDate.minusDays(numberOfEntries).atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
            new PumpDataTask().execute();
            timeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.weeklyRb:
                            numberOfEntries = 7;
                            numberOfLabels = numberOfEntries;
                            break;
                        case R.id.monthlyRb:
                            numberOfEntries = 30;
                            numberOfLabels = 7;
                            break;
                        case R.id.yearlyRb:
                            numberOfEntries = 366;
                            numberOfLabels = 7;
                            break;
                    }
                    startingDate = Date.from(currentDate.minusDays(numberOfEntries).atStartOfDay()
                            .atZone(ZoneId.systemDefault())
                            .toInstant());
                    createEntryLists();
                    createCharts();
                }
            });
        }
        return view;
    }

    private void displayVariablePicker(){
        String[] cardioVariables = {"Tensiune arterială", "Puls"};
        String[] weatherVariables = {"Umiditate", "Temperatură", "Viteza vântului", "Presiune atmosferică"};
        String[] allVariables = {"Tensiune arterială", "Puls", "Umiditate", "Temperatură", "Viteza vântului", "Presiune atmosferică"};
        int checkedVariable = 0;
        AlertDialog displayIndependentVariablePicker = new AlertDialog.Builder(getActivity())
                .setTitle("Alegeți variabila independentă")
                .setSingleChoiceItems(allVariables, checkedVariable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getContext(), "Checked: " + variables[which], Toast.LENGTH_SHORT).show();
                        setVariable(allVariables[which], 0);
                        String[] leftVariables = new String[4];
                        int option = 0;
                        for(String s : cardioVariables){
                            if(s.equals(allVariables[which])){
                                leftVariables = new String[weatherVariables.length];
                                break;
                            }
                        }
                        for(String s : weatherVariables){
                            if(s.equals(allVariables[which])){
                                leftVariables = new String[cardioVariables.length];
                                option = 1;
                                break;
                            }
                        }
                        int i = 0;
                        if(option == 0){
                            for(String s : weatherVariables){
                                leftVariables[i++] = s;
                            }
                        }else{
                            for(String s : cardioVariables){
                                leftVariables[i++] = s;
                            }
                        }

                        String[] finalLeftVariables = leftVariables;
                        AlertDialog displayDependentVariablePicker = new AlertDialog.Builder(getActivity())
                                .setTitle("Alegeți variabila dependentă")
                                .setSingleChoiceItems(leftVariables, checkedVariable, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog1, int which) {
                                        setVariable(finalLeftVariables[which], 1);
                                        dialog1.dismiss();
                                        dialog.dismiss();
                                        setChartTitle();
                                        new PumpDataTask().execute();
                                    }
                                })
                                .setNegativeButton("Anulează", null)
                                .create();
                        displayDependentVariablePicker.show();
                        Log.d(TAG, "Ind var: " + independentVariable + " - dep var: " + dependentVariable);

                    }
                })
                .setNegativeButton("Anulează", null)
                .create();
        displayIndependentVariablePicker.show();
    }

    private void setVariable(String variable, int option){
        switch(variable){
            case "Tensiune arterială":
                if(option == 0){
                    independentVariable = 1;
                }else{
                    dependentVariable = 1;
                }
                break;
            case "Puls":
                if(option == 0){
                    independentVariable = 2;
                }else{
                    dependentVariable = 2;
                }
                break;
            case "Umiditate":
                if(option == 0){
                    independentVariable = 3;
                }else{
                    dependentVariable = 3;
                }
                break;
            case "Temperatură":
                if(option == 0){
                    independentVariable = 4;
                }else{
                    dependentVariable = 4;
                }
                break;
            case "Viteza vântului":
                if(option == 0){
                    independentVariable = 5;
                }else{
                    dependentVariable = 5;
                }
                break;
            case "Presiune atmosferică":
                if(option == 0){
                    independentVariable = 6;
                }else{
                    dependentVariable = 6;
                }
                break;
        }
    }
    private void retrieveUserProfile() {
        Query getUserProfileByEmail = databaseReference.child("user-profile").orderByChild("emailAddress").equalTo(emailAddress);
        getUserProfileByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if (userProfile != null) {
                            city = userProfile.getLocation();
                            height = userProfile.getHeight();
                            break;
                        }
                    }
                    retrieveWeatherRecords();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void retrieveCardioRecords() {
        Query getCardioRecordsByEmail = databaseReference.child("cardio-record").orderByChild("emailAddress").equalTo(emailAddress);
        getCardioRecordsByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CardioRecord cardioRecord = snapshot.getValue(CardioRecord.class);
                        if (cardioRecord != null) {
                            cardioRecordList.add(cardioRecord);
                        }
                    }
                    createEntryLists();
                    createCharts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void retrieveWeatherRecords() {
        Query getWeatherRecordsByCity = databaseReference.child("weather-record").orderByChild("city").equalTo(city);
        getWeatherRecordsByCity.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        WeatherRecord weatherRecord = snapshot.getValue(WeatherRecord.class);
                        if (weatherRecord != null) {
                            weatherRecordList.add(weatherRecord);
                        }
                    }
                    retrieveCardioRecords();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setChartTitle(){
        String independentVariableStr = "";
        String dependentVariableStr = "";
        switch(independentVariable) {
            case BLOOD_PRESSURE:
                independentVariableStr += "Tensiune arterială";
                break;
            case PULSE:
                independentVariableStr += "Puls";
                break;
            case HUMIDITY:
                independentVariableStr += "Umiditate";
                break;
            case TEMPERATURE:
                independentVariableStr += "Temperatură";
                break;
            case WIND_SPEED:
                independentVariableStr += "Viteza vântului";
                break;
            case PRESSURE:
                independentVariableStr += "Presiune atmosferică";
                break;
        }

        switch(dependentVariable) {
            case BLOOD_PRESSURE:
                dependentVariableStr += "Tensiune arterială";
                break;
            case PULSE:
                dependentVariableStr += "Puls";
                break;
            case HUMIDITY:
                dependentVariableStr += "Umiditate";
                break;
            case TEMPERATURE:
                dependentVariableStr += "Temperatură";
                break;
            case WIND_SPEED:
                dependentVariableStr += "Viteza vântului";
                break;
            case PRESSURE:
                dependentVariableStr += "Presiune atmosferică";
                break;
        }

        String title = independentVariableStr + " - " + dependentVariableStr;
        chartTitleTv.setText(title);
    }

    private void createEntryListsForCardioRecords() {
        systolic = new ArrayList<>();
        diastolic = new ArrayList<>();
        pulse = new ArrayList<>();
        systolicObj = new HashMap<>();
        diastolicObj = new HashMap<>();
        pulseObj = new HashMap<>();

        for (CardioRecord c : cardioRecordList) {
            try {
                Date recordingDate = new SimpleDateFormat(dateFormat).parse(c.getRecordingDate());
                if (recordingDate.before(Date.from(currentDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())) && recordingDate.after(startingDate) ||
                        recordingDate.equals(startingDate)) {
                    LocalDate localDate = LocalDate.parse(c.getRecordingDate(), DateTimeFormatter.ofPattern(dateFormat));
                    Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                    long timeMillis = date.getTime();

                    if(!systolicObj.containsKey(timeMillis) && !diastolicObj.containsKey(timeMillis) && !pulseObj.containsKey(timeMillis)) {
                        systolicObj.put(timeMillis, c.getSystolicBP());
                        diastolicObj.put(timeMillis, c.getDiastolicBP());
                        pulseObj.put(timeMillis, c.getPulse());

                        systolic.add(new Entry(timeMillis, c.getSystolicBP()));
                        diastolic.add(new Entry(timeMillis, c.getDiastolicBP()));
                        pulse.add(new Entry(timeMillis, c.getPulse()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createEntryListsForWeatherRecords() {
        systolic = new ArrayList<>();
        diastolic = new ArrayList<>();
        pulse = new ArrayList<>();
        systolicObj = new HashMap<>();
        diastolicObj = new HashMap<>();
        pulseObj = new HashMap<>();

        temperature = new ArrayList<>();
        pressure = new ArrayList<>();
        temperatureObj = new HashMap<>();
        pressureObj = new HashMap<>();

        for (CardioRecord c : cardioRecordList) {
            for (WeatherRecord w : weatherRecordList) {
                if (c.getRecordingDate().equals(w.getRecordingDate()) && c.getRecordingHour().split(":")[0].equals(w.getRecordingHour().split(":")[0])) {
                    try {
                        Date recordingDate = new SimpleDateFormat(dateFormat).parse(w.getRecordingDate());
                        if (recordingDate.before(Date.from(currentDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())) && recordingDate.after(startingDate) ||
                                recordingDate.equals(startingDate)) {
                            LocalDate localDate = LocalDate.parse(w.getRecordingDate(), DateTimeFormatter.ofPattern(dateFormat));
                            Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                            long timeMillis = date.getTime();

                            if(!temperatureObj.containsKey(timeMillis) && !pressureObj.containsKey(timeMillis)) {
                                Log.d(TAG, "Cardio: " + c + "\n" + "Weather: " + w);
                                temperatureObj.put(timeMillis, (float) w.getTemperature());
                                pressureObj.put(timeMillis, (float) w.getPressure());

                                temperature.add(new Entry(timeMillis, (float) w.getTemperature()));
                                pressure.add(new Entry(timeMillis, (float) w.getPressure()));
                            }

                            if(!systolicObj.containsKey(timeMillis) && !diastolicObj.containsKey(timeMillis) && !pulseObj.containsKey(timeMillis)) {
                                systolicObj.put(timeMillis, c.getSystolicBP());
                                diastolicObj.put(timeMillis, c.getDiastolicBP());
                                pulseObj.put(timeMillis, c.getPulse());

                                systolic.add(new Entry(timeMillis, c.getSystolicBP()));
                                diastolic.add(new Entry(timeMillis, c.getDiastolicBP()));
                                pulse.add(new Entry(timeMillis, c.getPulse()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void createEntryListForBMI() {
        BMI = new ArrayList<>();
        BMIObj = new HashMap<>();
        for (CardioRecord c : cardioRecordList) {
            try {
                Date recordingDate = new SimpleDateFormat(dateFormat).parse(c.getRecordingDate());
                if (recordingDate.before(Date.from(currentDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())) && recordingDate.after(startingDate) ||
                        recordingDate.equals(startingDate)) {
                    LocalDate localDate = LocalDate.parse(c.getRecordingDate(), DateTimeFormatter.ofPattern(dateFormat));
                    Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                    long timeMillis = date.getTime();

                    if(!BMIObj.containsKey(timeMillis)) {
                        BMIObj.put(timeMillis, c.getWeight() / (int) Math.pow((double) height / 100, 2));
                        BMI.add(new Entry(timeMillis, c.getWeight() / (int) Math.pow((double) height / 100, 2)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void createEntryLists() {
        //createEntryListsForCardioRecords();
        createEntryListsForWeatherRecords();
        createEntryListForBMI();
        computeCorrelationCoefficient();
    }

    private void computeCorrelationCoefficient(){
        Log.d(TAG, "Sizes: " + systolic.size() + " - " + diastolic.size() + " - " + temperature.size() + " - " + pressure.size());
        double[] systolicData = new double[systolic.size()];
        double[] diastolicData = new double[diastolic.size()];
        double[] temperatureData = new double[temperature.size()];
        double[] pressureData = new double[pressure.size()];
        //double[] bmiData = new double[BMI.size()];

        int i = 0;
        for(Entry e : systolic){
            systolicData[i++] = e.getY();
        }
        i = 0;
        for(Entry e : diastolic){
            diastolicData[i++] = e.getY();
        }
        i = 0;
        for(Entry e : temperature){
            temperatureData[i++] = e.getY();
        }
        i = 0;
        for(Entry e : pressure){
            pressureData[i++] = e.getY();
        }
//        i = 0;
//        for(Entry e : BMI){
//            bmiData[i++] = e.getY();
//        }

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        double temperatureSystolicCorrelationCoefficient = pearsonsCorrelation.correlation(systolicData, temperatureData);
        double temperatureDiastolicCorrelationCoefficient = pearsonsCorrelation.correlation(diastolicData, temperatureData);

        double pressureSystolicCorrelationCoefficient = pearsonsCorrelation.correlation(systolicData, pressureData);
        double pressureDiastolicCorrelationCoefficient = pearsonsCorrelation.correlation(diastolicData, pressureData);

//        double bmiSystolicCorrelationCoefficient = pearsonsCorrelation.correlation(systolicData, bmiData);
//        double bmiDiastolicCorrelationCoefficient = pearsonsCorrelation.correlation(diastolicData, bmiData);

        Log.d(TAG, "\nTEMP-SYS: " + temperatureSystolicCorrelationCoefficient + "\n" + "TEMP-DIA: " + temperatureDiastolicCorrelationCoefficient + "\n" +
                "PRESSURE-SYS: " + pressureSystolicCorrelationCoefficient + "\n" + "PRESSURE-DIA: " + pressureDiastolicCorrelationCoefficient);


        String temperatureSystolicCorrelationCoefficientString = String.valueOf(temperatureSystolicCorrelationCoefficient);
        String temperatureDiastolicCorrelationCoefficientString = String.valueOf(temperatureDiastolicCorrelationCoefficient);
        SpannableStringBuilder tempSpannable = new SpannableStringBuilder();
        tempSpannable.append("Tensiune arterială sistolică - temperatură: ", new StyleSpan(Typeface.NORMAL), 0);
        tempSpannable.append(temperatureSystolicCorrelationCoefficientString, new ForegroundColorSpan(Color.RED),0).append('\n');
        tempSpannable.append("Tensiune arterială diastolică - temperatură: ", new StyleSpan(Typeface.NORMAL), 0);
        tempSpannable.append(temperatureDiastolicCorrelationCoefficientString, new ForegroundColorSpan(Color.BLUE),0);
        bpTempCorrelationTv.setText(tempSpannable);

        String pressureSystolicCorrelationCoefficientString = String.valueOf(pressureSystolicCorrelationCoefficient);
        String pressureDiastolicCorrelationCoefficientString = String.valueOf(pressureDiastolicCorrelationCoefficient);
        SpannableStringBuilder pressureSpannable = new SpannableStringBuilder();
        pressureSpannable.append("Tensiune arterială sistolică - presiune atmosferică: ", new StyleSpan(Typeface.NORMAL), 0);
        pressureSpannable.append(pressureSystolicCorrelationCoefficientString, new ForegroundColorSpan(Color.RED),0).append('\n');
        pressureSpannable.append("Tensiune arterială diastolică - presiune atmosferică: ", new StyleSpan(Typeface.NORMAL), 0);
        pressureSpannable.append(pressureDiastolicCorrelationCoefficientString, new ForegroundColorSpan(Color.BLUE),0);
        bpPressureCorrelationTv.setText(pressureSpannable);

    }

    private void showChartBloodPressure() {
        systolicBPDataSet = new LineDataSet(null, null);
        systolicBPDataSet.setValues(systolic);
        systolicBPDataSet.setValueFormatter(new FloatValueFormatter());
        systolicBPDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        systolicBPDataSet.setColor(Color.RED);
        systolicBPDataSet.setCircleColor(Color.RED);
        systolicBPDataSet.setValueTextSize(10f);
        systolicBPDataSet.setValueTextColor(Color.RED);
        systolicBPDataSet.setLineWidth(2f);
        systolicBPDataSet.setCircleRadius(3f);
        systolicBPDataSet.setFillAlpha(65);
        systolicBPDataSet.setFillColor(ColorTemplate.getHoloBlue());
        systolicBPDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        systolicBPDataSet.setDrawCircleHole(true);
        systolicBPDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        systolicBPDataSet.setDrawValues(false);
        systolicBPDataSet.setLabel("Sistolică");

        diastolicBPDataSet = new LineDataSet(null, null);
        diastolicBPDataSet.setValues(diastolic);
        diastolicBPDataSet.setValueFormatter(new FloatValueFormatter());
        diastolicBPDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        diastolicBPDataSet.setColor(Color.BLUE);
        diastolicBPDataSet.setCircleColor(Color.BLUE);
        diastolicBPDataSet.setValueTextSize(10f);
        diastolicBPDataSet.setValueTextColor(Color.BLUE);
        diastolicBPDataSet.setLineWidth(2f);
        diastolicBPDataSet.setCircleRadius(3f);
        diastolicBPDataSet.setFillAlpha(65);
        diastolicBPDataSet.setFillColor(ColorTemplate.getHoloBlue());
        diastolicBPDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        diastolicBPDataSet.setDrawCircleHole(true);
        diastolicBPDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        diastolicBPDataSet.setDrawValues(false);
        diastolicBPDataSet.setLabel("Diastolică");

        iLineDataSets = new ArrayList<>();
        iLineDataSets.add(systolicBPDataSet);
        iLineDataSets.add(diastolicBPDataSet);
        lineData = new LineData(iLineDataSets);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "mmHg";
            }
        });

        bloodPressureLc.clear();
        bloodPressureLc.setBackgroundColor(Color.WHITE);
        bloodPressureLc.getDescription().setEnabled(false);
        bloodPressureLc.setTouchEnabled(true);
        bloodPressureLc.setDrawGridBackground(false);
        bloodPressureLc.setDragEnabled(false);
        bloodPressureLc.setScaleEnabled(false);
        bloodPressureLc.setPinchZoom(false);
        bloodPressureLc.setData(lineData);
        bloodPressureLc.invalidate();
        bloodPressureLc.setMaxVisibleValueCount(numberOfEntries);

        XAxis xAxisSystolic;
        {
            xAxisSystolic = bloodPressureLc.getXAxis();
            xAxisSystolic.resetAxisMaximum();
            xAxisSystolic.resetAxisMinimum();
            xAxisSystolic.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxisSystolic.setLabelCount(systolic.size(), true);
            xAxisSystolic.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    return simpleDateFormat.format(new Date(millis + 1000000));
                }
            });
            if (numberOfEntries > 8) {
                xAxisSystolic.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxisSystolic;
        {
            yAxisSystolic = bloodPressureLc.getAxisLeft();
            bloodPressureLc.getAxisRight().setEnabled(false);
            yAxisSystolic.disableAxisLineDashedLine();
            yAxisSystolic.resetAxisMaximum();
            yAxisSystolic.resetAxisMinimum();
        }

        Legend legend = bloodPressureLc.getLegend();
        legend.setEnabled(true);

        BloodPressureMarkerView mv = new BloodPressureMarkerView(getContext(), R.layout.custom_markerview_cardio);
        mv.setChartView(bloodPressureLc);
        bloodPressureLc.setMarker(mv);

    }


    private void showChartPulse() {
        pulseDataSet = new LineDataSet(null, null);
        pulseDataSet.setValues(pulse);
        pulseDataSet.setValueFormatter(new FloatValueFormatter());
        pulseDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        pulseDataSet.setColor(Color.RED);
        pulseDataSet.setCircleColor(Color.RED);
        pulseDataSet.setValueTextSize(10f);
        pulseDataSet.setValueTextColor(Color.RED);
        pulseDataSet.setLineWidth(2f);
        pulseDataSet.setCircleRadius(3f);
        pulseDataSet.setFillAlpha(65);
        pulseDataSet.setFillColor(ColorTemplate.getHoloBlue());
        pulseDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        pulseDataSet.setDrawCircleHole(true);
        pulseDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        pulseDataSet.setDrawValues(false);
        pulseDataSet.setLabel("Puls");

        iLineDataSets = new ArrayList<>();
        iLineDataSets.add(pulseDataSet);
        lineData = new LineData(iLineDataSets);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "bpm";
            }
        });

        pulseLc.clear();
        pulseLc.setBackgroundColor(Color.WHITE);
        pulseLc.getDescription().setEnabled(false);
        pulseLc.setTouchEnabled(true);
        pulseLc.setDrawGridBackground(false);
        pulseLc.setDragEnabled(false);
        pulseLc.setScaleEnabled(false);
        pulseLc.setPinchZoom(false);
        pulseLc.setData(lineData);
        pulseLc.invalidate();
        pulseLc.setMaxVisibleValueCount(numberOfEntries);

        XAxis xAxisSystolic;
        {
            xAxisSystolic = pulseLc.getXAxis();
            xAxisSystolic.resetAxisMaximum();
            xAxisSystolic.resetAxisMinimum();
            xAxisSystolic.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxisSystolic.setLabelCount(systolic.size(), true);
            xAxisSystolic.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    Date millisToDate = new Date(millis + 1000000);
                    if (numberOfEntries == 366) {
                        String monthStr = "";
                        LocalDate localDate = millisToDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        int month = localDate.getMonthValue();
                        monthStr = getMonthByInt(month);
                        return monthStr;
                    }
                    return simpleDateFormat.format(millisToDate);
                }
            });
            if (numberOfEntries > 8) {
                xAxisSystolic.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxisSystolic;
        {
            yAxisSystolic = pulseLc.getAxisLeft();
            pulseLc.getAxisRight().setEnabled(false);
            yAxisSystolic.disableAxisLineDashedLine();
            yAxisSystolic.resetAxisMaximum();
            yAxisSystolic.resetAxisMinimum();
        }

        Legend legend = pulseLc.getLegend();
        legend.setEnabled(true);

        PulseMarkerView mv = new PulseMarkerView(getContext(), R.layout.custom_markerview_cardio);
        mv.setChartView(pulseLc);
        pulseLc.setMarker(mv);

    }

    private void showChartBloodPressureAndTemperature() {
        systolicBPDataSet = new LineDataSet(null, null);
        systolicBPDataSet.setValues(systolic);
        systolicBPDataSet.setValueFormatter(new FloatValueFormatter());
        systolicBPDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        systolicBPDataSet.setColor(Color.RED);
        systolicBPDataSet.setCircleColor(Color.RED);
        systolicBPDataSet.setValueTextSize(10f);
        systolicBPDataSet.setValueTextColor(Color.RED);
        systolicBPDataSet.setLineWidth(2f);
        systolicBPDataSet.setCircleRadius(3f);
        systolicBPDataSet.setFillAlpha(65);
        systolicBPDataSet.setFillColor(ColorTemplate.getHoloBlue());
        systolicBPDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        systolicBPDataSet.setDrawCircleHole(true);
        systolicBPDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        systolicBPDataSet.setDrawValues(false);
        systolicBPDataSet.setLabel("Sistolică");
        systolicBPDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "mmHg";
            }
        });

        diastolicBPDataSet = new LineDataSet(null, null);
        diastolicBPDataSet.setValues(diastolic);
        diastolicBPDataSet.setValueFormatter(new FloatValueFormatter());
        diastolicBPDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        diastolicBPDataSet.setColor(Color.BLUE);
        diastolicBPDataSet.setCircleColor(Color.BLUE);
        diastolicBPDataSet.setValueTextSize(10f);
        diastolicBPDataSet.setValueTextColor(Color.BLUE);
        diastolicBPDataSet.setLineWidth(2f);
        diastolicBPDataSet.setCircleRadius(3f);
        diastolicBPDataSet.setFillAlpha(65);
        diastolicBPDataSet.setFillColor(ColorTemplate.getHoloBlue());
        diastolicBPDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        diastolicBPDataSet.setDrawCircleHole(true);
        diastolicBPDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        diastolicBPDataSet.setDrawValues(false);
        diastolicBPDataSet.setLabel("Diastolică");
        diastolicBPDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "mmHg";
            }
        });

        temperatureDataSet = new LineDataSet(null, null);
        temperatureDataSet.setValues(temperature);
        temperatureDataSet.setValueFormatter(new FloatValueFormatter());
        temperatureDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        temperatureDataSet.setColor(Color.GREEN);
        temperatureDataSet.setCircleColor(Color.GREEN);
        temperatureDataSet.setValueTextSize(10f);
        temperatureDataSet.setValueTextColor(Color.GREEN);
        temperatureDataSet.setLineWidth(2f);
        temperatureDataSet.setCircleRadius(3f);
        temperatureDataSet.setFillAlpha(65);
        temperatureDataSet.setFillColor(ColorTemplate.getHoloBlue());
        temperatureDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        temperatureDataSet.setDrawCircleHole(true);
        temperatureDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        temperatureDataSet.setDrawValues(false);
        temperatureDataSet.setLabel("Temperatură");
        temperatureDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + "°C";
            }
        });

        iLineDataSets = new ArrayList<>();
        iLineDataSets.add(systolicBPDataSet);
        iLineDataSets.add(diastolicBPDataSet);
        iLineDataSets.add(temperatureDataSet);
        lineData = new LineData(iLineDataSets);

        bloodPressureAndTemperatureLc.clear();
        bloodPressureAndTemperatureLc.setBackgroundColor(Color.WHITE);
        bloodPressureAndTemperatureLc.getDescription().setEnabled(false);
        bloodPressureAndTemperatureLc.setTouchEnabled(true);
        bloodPressureAndTemperatureLc.setDrawGridBackground(false);
        bloodPressureAndTemperatureLc.setDragEnabled(false);
        bloodPressureAndTemperatureLc.setScaleEnabled(false);
        bloodPressureAndTemperatureLc.setPinchZoom(false);
        bloodPressureAndTemperatureLc.setData(lineData);
        bloodPressureAndTemperatureLc.invalidate();
        bloodPressureAndTemperatureLc.setMaxVisibleValueCount(numberOfEntries);

        XAxis xAxisSystolic;
        {
            xAxisSystolic = bloodPressureAndTemperatureLc.getXAxis();
            xAxisSystolic.resetAxisMaximum();
            xAxisSystolic.resetAxisMinimum();
            xAxisSystolic.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxisSystolic.setLabelCount(systolic.size(), true);
            xAxisSystolic.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    return simpleDateFormat.format(new Date(millis + 1000000));
                }
            });
            if (numberOfEntries > 8) {
                xAxisSystolic.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxisSystolic;
        {
            yAxisSystolic = bloodPressureAndTemperatureLc.getAxisLeft();
            bloodPressureAndTemperatureLc.getAxisRight().setEnabled(false);
            yAxisSystolic.disableAxisLineDashedLine();
            yAxisSystolic.resetAxisMaximum();
            yAxisSystolic.resetAxisMinimum();
        }

        Legend legend = bloodPressureAndTemperatureLc.getLegend();
        legend.setEnabled(true);

        BloodPressureAndTemperatureMarkerView mv = new BloodPressureAndTemperatureMarkerView(getContext(), R.layout.custom_markerview_cardio);
        mv.setChartView(bloodPressureAndTemperatureLc);
        bloodPressureAndTemperatureLc.setMarker(mv);
    }

    private void showChartBloodPressureAndPressure() {
        systolicBPDataSet = new LineDataSet(null, null);
        systolicBPDataSet.setValues(systolic);
        systolicBPDataSet.setValueFormatter(new FloatValueFormatter());
        systolicBPDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        systolicBPDataSet.setColor(Color.RED);
        systolicBPDataSet.setCircleColor(Color.RED);
        systolicBPDataSet.setValueTextSize(10f);
        systolicBPDataSet.setValueTextColor(Color.RED);
        systolicBPDataSet.setLineWidth(2f);
        systolicBPDataSet.setCircleRadius(3f);
        systolicBPDataSet.setFillAlpha(65);
        systolicBPDataSet.setFillColor(ColorTemplate.getHoloBlue());
        systolicBPDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        systolicBPDataSet.setDrawCircleHole(true);
        systolicBPDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        systolicBPDataSet.setDrawValues(false);
        systolicBPDataSet.setLabel("Sistolică");
        systolicBPDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "mmHg";
            }
        });

        diastolicBPDataSet = new LineDataSet(null, null);
        diastolicBPDataSet.setValues(diastolic);
        diastolicBPDataSet.setValueFormatter(new FloatValueFormatter());
        diastolicBPDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        diastolicBPDataSet.setColor(Color.BLUE);
        diastolicBPDataSet.setCircleColor(Color.BLUE);
        diastolicBPDataSet.setValueTextSize(10f);
        diastolicBPDataSet.setValueTextColor(Color.BLUE);
        diastolicBPDataSet.setLineWidth(2f);
        diastolicBPDataSet.setCircleRadius(3f);
        diastolicBPDataSet.setFillAlpha(65);
        diastolicBPDataSet.setFillColor(ColorTemplate.getHoloBlue());
        diastolicBPDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        diastolicBPDataSet.setDrawCircleHole(true);
        diastolicBPDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        diastolicBPDataSet.setDrawValues(false);
        diastolicBPDataSet.setLabel("Diastolică");
        diastolicBPDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "mmHg";
            }
        });

        pressureDataSet = new LineDataSet(null, null);
        pressureDataSet.setValues(pressure);
        pressureDataSet.setValueFormatter(new FloatValueFormatter());
        pressureDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        pressureDataSet.setColor(Color.GREEN);
        pressureDataSet.setCircleColor(Color.GREEN);
        pressureDataSet.setValueTextSize(10f);
        pressureDataSet.setValueTextColor(Color.GREEN);
        pressureDataSet.setLineWidth(2f);
        pressureDataSet.setCircleRadius(3f);
        pressureDataSet.setFillAlpha(65);
        pressureDataSet.setFillColor(ColorTemplate.getHoloBlue());
        pressureDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        pressureDataSet.setDrawCircleHole(true);
        pressureDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        pressureDataSet.setDrawValues(false);
        pressureDataSet.setLabel("Presiune atmosferică");

        iLineDataSets = new ArrayList<>();
        iLineDataSets.add(systolicBPDataSet);
        iLineDataSets.add(diastolicBPDataSet);
        iLineDataSets.add(pressureDataSet);
        lineData = new LineData(iLineDataSets);

        bloodPressureAndPressureLc.clear();
        bloodPressureAndPressureLc.setBackgroundColor(Color.WHITE);
        bloodPressureAndPressureLc.getDescription().setEnabled(false);
        bloodPressureAndPressureLc.setTouchEnabled(true);
        bloodPressureAndPressureLc.setDrawGridBackground(false);
        bloodPressureAndPressureLc.setDragEnabled(false);
        bloodPressureAndPressureLc.setScaleEnabled(false);
        bloodPressureAndPressureLc.setPinchZoom(false);
        bloodPressureAndPressureLc.setData(lineData);
        bloodPressureAndPressureLc.invalidate();
        bloodPressureAndPressureLc.setMaxVisibleValueCount(numberOfEntries);

        XAxis xAxisSystolic;
        {
            xAxisSystolic = bloodPressureAndPressureLc.getXAxis();
            xAxisSystolic.resetAxisMaximum();
            xAxisSystolic.resetAxisMinimum();
            xAxisSystolic.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxisSystolic.setLabelCount(systolic.size(), true);
            xAxisSystolic.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    return simpleDateFormat.format(new Date(millis + 1000000));

                }
            });
            if (numberOfEntries > 8) {
                xAxisSystolic.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxisSystolic;
        {
            yAxisSystolic = bloodPressureAndPressureLc.getAxisLeft();
            bloodPressureAndPressureLc.getAxisRight().setEnabled(false);
            yAxisSystolic.disableAxisLineDashedLine();
            yAxisSystolic.resetAxisMaximum();
            yAxisSystolic.resetAxisMinimum();
        }

        Legend legend = bloodPressureAndPressureLc.getLegend();
        legend.setEnabled(true);

        BloodPressureAndPressureMarkerView mv = new BloodPressureAndPressureMarkerView(getContext(), R.layout.custom_markerview_cardio);
        mv.setChartView(bloodPressureAndPressureLc);
        bloodPressureAndPressureLc.setMarker(mv);

    }

    private void showChartBloodPressureAndBMI() {
        systolicBPDataSet = new LineDataSet(null, null);
        systolicBPDataSet.setValues(systolic);
        systolicBPDataSet.setValueFormatter(new FloatValueFormatter());
        systolicBPDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        systolicBPDataSet.setColor(Color.RED);
        systolicBPDataSet.setCircleColor(Color.RED);
        systolicBPDataSet.setValueTextSize(10f);
        systolicBPDataSet.setValueTextColor(Color.RED);
        systolicBPDataSet.setLineWidth(2f);
        systolicBPDataSet.setCircleRadius(3f);
        systolicBPDataSet.setFillAlpha(65);
        systolicBPDataSet.setFillColor(ColorTemplate.getHoloBlue());
        systolicBPDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        systolicBPDataSet.setDrawCircleHole(true);
        systolicBPDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        systolicBPDataSet.setDrawValues(false);
        systolicBPDataSet.setLabel("Sistolică");
        systolicBPDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "mmHg";
            }
        });

        diastolicBPDataSet = new LineDataSet(null, null);
        diastolicBPDataSet.setValues(diastolic);
        diastolicBPDataSet.setValueFormatter(new FloatValueFormatter());
        diastolicBPDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        diastolicBPDataSet.setColor(Color.BLUE);
        diastolicBPDataSet.setCircleColor(Color.BLUE);
        diastolicBPDataSet.setValueTextSize(10f);
        diastolicBPDataSet.setValueTextColor(Color.BLUE);
        diastolicBPDataSet.setLineWidth(2f);
        diastolicBPDataSet.setCircleRadius(3f);
        diastolicBPDataSet.setFillAlpha(65);
        diastolicBPDataSet.setFillColor(ColorTemplate.getHoloBlue());
        diastolicBPDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        diastolicBPDataSet.setDrawCircleHole(true);
        diastolicBPDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        diastolicBPDataSet.setDrawValues(false);
        diastolicBPDataSet.setLabel("Diastolică");
        diastolicBPDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "mmHg";
            }
        });

        BMIdataSet = new LineDataSet(null, null);
        BMIdataSet.setValues(BMI);
        BMIdataSet.setValueFormatter(new FloatValueFormatter());
        BMIdataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        BMIdataSet.setColor(Color.GREEN);
        BMIdataSet.setCircleColor(Color.GREEN);
        BMIdataSet.setValueTextSize(10f);
        BMIdataSet.setValueTextColor(Color.GREEN);
        BMIdataSet.setLineWidth(2f);
        BMIdataSet.setCircleRadius(3f);
        BMIdataSet.setFillAlpha(65);
        BMIdataSet.setFillColor(ColorTemplate.getHoloBlue());
        BMIdataSet.setHighLightColor(Color.rgb(244, 117, 117));
        BMIdataSet.setDrawCircleHole(true);
        BMIdataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        BMIdataSet.setDrawValues(false);
        BMIdataSet.setLabel("Indice de masă corporală");

        iLineDataSets = new ArrayList<>();
        iLineDataSets.add(systolicBPDataSet);
        iLineDataSets.add(diastolicBPDataSet);
        iLineDataSets.add(BMIdataSet);
        lineData = new LineData(iLineDataSets);

        bloodPressureAndBMILc.clear();
        bloodPressureAndBMILc.setBackgroundColor(Color.WHITE);
        bloodPressureAndBMILc.getDescription().setEnabled(false);
        bloodPressureAndBMILc.setTouchEnabled(true);
        bloodPressureAndBMILc.setDrawGridBackground(false);
        bloodPressureAndBMILc.setDragEnabled(false);
        bloodPressureAndBMILc.setScaleEnabled(false);
        bloodPressureAndBMILc.setPinchZoom(false);
        bloodPressureAndBMILc.setData(lineData);
        bloodPressureAndBMILc.invalidate();
        bloodPressureAndBMILc.setMaxVisibleValueCount(numberOfEntries);

        XAxis xAxisSystolic;
        {
            xAxisSystolic = bloodPressureAndBMILc.getXAxis();
            xAxisSystolic.resetAxisMaximum();
            xAxisSystolic.resetAxisMinimum();
            xAxisSystolic.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxisSystolic.setLabelCount(systolic.size(), true);
            xAxisSystolic.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    return simpleDateFormat.format(new Date(millis + 1000000));
                }
            });
            if (numberOfEntries > 8) {
                xAxisSystolic.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxisSystolic;
        {
            yAxisSystolic = bloodPressureAndBMILc.getAxisLeft();
            bloodPressureAndBMILc.getAxisRight().setEnabled(false);
            yAxisSystolic.disableAxisLineDashedLine();
            yAxisSystolic.resetAxisMaximum();
            yAxisSystolic.resetAxisMinimum();
        }

        Legend legend = bloodPressureAndBMILc.getLegend();
        legend.setEnabled(true);

        BloodPressureAndBMIMarkerView mv = new BloodPressureAndBMIMarkerView(getContext(), R.layout.custom_markerview_cardio);
        mv.setChartView(bloodPressureAndBMILc);
        bloodPressureAndBMILc.setMarker(mv);

    }

    private void showChartHypertensionStages() {

    }

    private void createCharts() {
        if (!isDetached() && getContext() != null) {
            showChartBloodPressure();
            showChartPulse();
            showChartBloodPressureAndTemperature();
            showChartBloodPressureAndPressure();
            showChartBloodPressureAndBMI();
        }
        loadingDialog.dismissDialog();
    }

    private String getMonthByInt(int month) {
        String monthStr = "";
        String[] monthArray = new String[]{"Ian", "Feb", "Mar", "Apr", "Mai", "Iun", "Iul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        switch (month) {
            case 1:
                monthStr = monthArray[0];
                break;
            case 2:
                monthStr = monthArray[1];
                break;
            case 3:
                monthStr = monthArray[2];
                break;
            case 4:
                monthStr = monthArray[3];
                break;
            case 5:
                monthStr = monthArray[4];
                break;
            case 6:
                monthStr = monthArray[5];
                break;
            case 7:
                monthStr = monthArray[6];
                break;
            case 8:
                monthStr = monthArray[7];
                break;
            case 9:
                monthStr = monthArray[8];
                break;
            case 10:
                monthStr = monthArray[9];
                break;
            case 11:
                monthStr = monthArray[10];
                break;
            case 12:
                monthStr = monthArray[11];
                break;
        }
        return monthStr;
    }

    private class PumpDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loadingDialog.showDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            retrieveUserProfile();
            return null;
        }
    }
}

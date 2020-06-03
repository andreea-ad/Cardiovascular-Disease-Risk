package ro.uvt.asavoaei.andreea.cardiovascularapp.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.LoadingDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.CardioAndWeatherRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.CardioRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.UserProfile;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.WeatherRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureAndBMIMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureAndHumidityMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureAndPressureMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureAndTemperatureMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureAndWindSpeedMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.BloodPressureMarkerView;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.FloatValueFormatter;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.PulseMarkerView;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;


public class StatisticsFragment extends Fragment {
    private static final String TAG = StatisticsFragment.class.getName();
    private static final String dateFormat = "dd-MM-yyyy";
    private static final int LINEAR_REGRESSION = 1;
    private static final int MULTILAYER_PERCEPTRON = 2;
    private static final int SMO_REG = 3;
    private LoadingDialog loadingDialog;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private TextView bpTempCorrelationTv, bpPressureCorrelationTv, bpHumidityCorrelationTv, bpWindSpeedCorrelationTv;
    private RadioGroup timeRg;
    private Button linearRegressionBtn, multilayerPerceptronBtn, SMORegBtn;
    private String emailAddress = "", city = "";
    private int height = 0;
    private Date startingDate;
    private LocalDate currentDate = LocalDate.now();
    private int numberOfEntries = 7;
    private int numberOfLabels = numberOfEntries;
    private int normalBp = 0;
    private int elevatedBp = 0;
    private int hypertensionStageI = 0;
    private int hypertensionStageII = 0;
    private int hypertensiveCrisis = 0;

    private Set<CardioRecord> cardioRecordSet = new HashSet<>();
    private Set<WeatherRecord> weatherRecordSet = new HashSet<>();

    private List<CardioRecord> cardioRecordList = new ArrayList<>();
    private List<WeatherRecord> weatherRecordList = new ArrayList<>();

    private Map<String, Float> bpAveragesOnCategories = new HashMap();

    private ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    private LineData lineData;
    private PieData pieData;

    private LineChart bloodPressureLc, pulseLc, bloodPressureAndTemperatureLc, bloodPressureAndPressureLc, bloodPressureAndHumidityLc, bloodPressureAndWindSpeedLc;
    private PieChart hypertensionStagesPc;

    private LineDataSet systolicBPDataSet = new LineDataSet(null, null);
    private LineDataSet diastolicBPDataSet = new LineDataSet(null, null);
    private LineDataSet pulseDataSet = new LineDataSet(null, null);
    private LineDataSet temperatureDataSet = new LineDataSet(null, null);
    private LineDataSet pressureDataSet = new LineDataSet(null, null);
    private LineDataSet humidityDataSet = new LineDataSet(null, null);
    private LineDataSet windSpeedDataSet = new LineDataSet(null,null);
    private PieDataSet bloodPressurePieDataSet = new PieDataSet(null, null);

    private ArrayList<Entry> systolic = new ArrayList<>();
    private ArrayList<Entry> diastolic = new ArrayList<>();
    private ArrayList<Entry> pulse = new ArrayList<>();
    private ArrayList<Entry> temperature = new ArrayList<>();
    private ArrayList<Entry> pressure = new ArrayList<>();
    private ArrayList<Entry> humidity = new ArrayList<>();
    private ArrayList<Entry> windSpeed = new ArrayList<>();
    private ArrayList<PieEntry> bloodPressurePieEntry = new ArrayList<>();

    private List<CardioAndWeatherRecord> instances = new ArrayList<>();
    private final Attribute systolicBloodPressureAttribute = new Attribute("systolicbp");
    private final Attribute diastolicBloodPressureAttribute = new Attribute("diastolicbp");
    private final Attribute pulseAttribute = new Attribute("pulse");
    private final Attribute temperatureAttribute = new Attribute("temperature");
    private final Attribute pressureAttribute = new Attribute("pressure");
    private final Attribute humidityAttribute = new Attribute("humidity");
    private ArrayList<Attribute> attributeList = new ArrayList<>();
    private WeatherRecord currentWeather = new WeatherRecord();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, null);
        loadingDialog = new LoadingDialog(getContext());
        bloodPressureLc = view.findViewById(R.id.bpChart);
        pulseLc = view.findViewById(R.id.pulseChart);
        bloodPressureAndTemperatureLc = view.findViewById(R.id.bpTempChart);
        bloodPressureAndPressureLc = view.findViewById(R.id.bpPressureChart);
        bloodPressureAndHumidityLc = view.findViewById(R.id.bpHumidityChart);
        bloodPressureAndWindSpeedLc = view.findViewById(R.id.bpWindSpeedChart);
        hypertensionStagesPc = view.findViewById(R.id.bpStagesChart);
        timeRg = view.findViewById(R.id.timeRg);
        bpTempCorrelationTv = view.findViewById(R.id.bpTempCorrelationTv);
        bpPressureCorrelationTv = view.findViewById(R.id.bpPressureCorrelationTv);
        bpHumidityCorrelationTv = view.findViewById(R.id.bpHumidityCorrelationTv);
        bpWindSpeedCorrelationTv = view.findViewById(R.id.bpWindSpeedCorrelationTv);
        linearRegressionBtn = view.findViewById(R.id.linearRegressionBtn);
        multilayerPerceptronBtn = view.findViewById(R.id.multilayerPerceptronBtn);
        SMORegBtn = view.findViewById(R.id.SMORegBtn);

        attributeList.add(temperatureAttribute);
        attributeList.add(pressureAttribute);
        attributeList.add(humidityAttribute);
        attributeList.add(systolicBloodPressureAttribute);
        attributeList.add(diastolicBloodPressureAttribute);
        attributeList.add(pulseAttribute);

        linearRegressionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayVariablesPickers(v.getId());
            }
        });

        multilayerPerceptronBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                displayVariablesPickers(v.getId());
            }
        });

        SMORegBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                displayVariablesPickers(v.getId());
            }
        });

        if (firebaseAuth.getCurrentUser() != null) {
            emailAddress = firebaseAuth.getCurrentUser().getEmail();
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
                    setData();
                }
            });
        }
        return view;
    }

    private void setData(){
        if(dataExists()) {
            createEntryLists();
            createCharts();
            bpTempCorrelationTv.setText(null);
            bpPressureCorrelationTv.setText(null);
            bpHumidityCorrelationTv.setText(null);
            bpWindSpeedCorrelationTv.setText(null);
            setClickableButtons(false);
            if(isDataValid()){
                computeCorrelationCoefficient();
                setClickableButtons(true);
            }
            loadingDialog.dismissDialog();
        }
    }

    private void displayVariablesPickers(int viewId){
        new GetWeather().execute();
        String[] predictorVariables = new String[]{"Temperatură", "Presiune atmosferică", "Umiditate"};
        String[] predictedVariables = new String[]{"Tensiune arterială sistolică", "Tensiune arterială diastolică", "Puls"};
        List<String> selectedPredictors = new ArrayList<>();
        boolean[] checkedPredictors = new boolean[predictorVariables.length];

        AlertDialog.Builder predictorsPicker = new AlertDialog.Builder(getActivity());
        predictorsPicker.setTitle("Alegeți variabilele predictori");
        predictorsPicker.setMultiChoiceItems(predictorVariables, checkedPredictors, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedPredictors[which] = isChecked;
                selectedPredictors.add(predictorVariables[which]);
            }
        });
        predictorsPicker.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final int[] checkedPredicted = {-1};
                AlertDialog.Builder predictedPicker = new AlertDialog.Builder(getContext());
                predictedPicker.setTitle("Alegeți variabila pentru prezis");
                predictedPicker.setSingleChoiceItems(predictedVariables, checkedPredicted[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedPredicted[0] = which;
                    }
                });
                predictedPicker.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean[] checked = new boolean[6];
                        for(String selectedPredictor : selectedPredictors){
                            if(selectedPredictor.equals("Temperatură")){
                                checked[0] = true;
                            }else if(selectedPredictor.equals("Presiune atmosferică")){
                                checked[1] = true;
                            }else if(selectedPredictor.equals("Umiditate")){
                                checked[2] = true;
                            }
                        }
                        String predictedVariableString = predictedVariables[checkedPredicted[0]];
                        if(predictedVariableString.equals("Tensiune arterială sistolică")){
                            checked[3] = true;
                        }else if(predictedVariableString.equals("Tensiune arterială diastolică")){
                            checked[4] = true;
                        }else if(predictedVariableString.equals("Puls")){
                            checked[5] = true;
                        }

                        if(viewId == R.id.linearRegressionBtn) {
                            Log.d(TAG, "REG");
                            classify(LINEAR_REGRESSION,checked);
                        }else if(viewId == R.id.multilayerPerceptronBtn){
                            Log.d(TAG, "MULTI");
                            classify(MULTILAYER_PERCEPTRON, checked);
                        }else if(viewId == R.id.SMORegBtn){
                            Log.d(TAG, "SMO");
                            classify(SMO_REG, checked);
                        }
                    }
                });
                predictedPicker.setNegativeButton("Anulează", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        predictedPicker.create().dismiss();
                    }
                });
                predictedPicker.create().show();
            }
        });
        predictorsPicker.setNegativeButton("Anulează", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                predictorsPicker.create().dismiss();
            }
        });
        predictorsPicker.create().show();
    }

    private Instances getInstances(){
        Instances dataSet = new Instances("Data", attributeList, 365);
        for(CardioAndWeatherRecord c : instances){
            Instance currentInstance = new DenseInstance(6);
            currentInstance.setValue(temperatureAttribute, c.getTemperature());
            currentInstance.setValue(pressureAttribute, c.getPressure());
            currentInstance.setValue(humidityAttribute, c.getHumidity());
            currentInstance.setValue(systolicBloodPressureAttribute, c.getSystolicBP());
            currentInstance.setValue(diastolicBloodPressureAttribute, c.getDiastolicBP());
            currentInstance.setValue(pulseAttribute, c.getPulse());
            Log.d(TAG, "CARDIO: " + c);
            dataSet.add(currentInstance);
        }
        return dataSet;
    }

    private void classify(int type, boolean[] checked){
        try{
            int j = 0;
            for(int i = 0; i < 6; i++) {
                if (checked[i]) {
                    j++;
                }
            }
            int[] indicesToKeep = new int[j];
            j = 0;
            for(int i = 0; i < 6; i++){
                if(checked[i]){
                    indicesToKeep[j++] = i;
                }
            }
            Instances data = getInstances();
            data.setClassIndex(data.numAttributes() - 1);
            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndicesArray(indicesToKeep);
            removeFilter.setInvertSelection(true);
            removeFilter.setInputFormat(data);
            Instances newData = Filter.useFilter(data, removeFilter);
            newData.setClassIndex(newData.numAttributes() - 1);
            Log.d(TAG, "Num instances: " + newData.numInstances());
            int percent70 = (int)(newData.numInstances()*0.7);
            Log.d(TAG, "70% = " + percent70);
            Instances trainingData = new Instances(newData, 0, percent70);
            int percent30 = (int)(newData.numInstances()*0.3);
            Log.d(TAG, "30% = " + percent30);
            Instances testingData = new Instances(newData, percent70, percent30);

            Log.d(TAG, "Training data: " + trainingData);
            Log.d(TAG, "Testing data: " + testingData);

            Evaluation evaluation = new Evaluation(trainingData);
            Classifier model = new LinearRegression();
            switch(type){
                case LINEAR_REGRESSION:
                    model = new LinearRegression();
                    break;
                case MULTILAYER_PERCEPTRON:
                    model = new MultilayerPerceptron();
                    break;
                case SMO_REG:
                    model = new SMOreg();
                    break;
            }
            model.buildClassifier(trainingData);
            evaluation.evaluateModel(model, testingData);

            String statsStr = model.toString() + "\n" +
                    evaluation.toSummaryString() + "\n";

            statsStr += "           Actual Predicted\n";
            Log.d(TAG, "Evaluation: " + evaluation.toSummaryString());

            for(Object prediction : evaluation.predictions()){
                statsStr += prediction + "\n";
                Log.d(TAG, "Predictions: " + prediction);
            }

            // Test instance
            Instance instance = new DenseInstance(newData.numAttributes());
            for(int i = 0; i < indicesToKeep.length; i++){
                if(indicesToKeep[i] == 0){
                    Log.d(TAG, "TEMPERATURA");
                    instance.setValue(newData.attribute("temperature"), currentWeather.getTemperature());
                }else if(indicesToKeep[i] == 1){
                    Log.d(TAG, "PRESIUNE ATMOSFERICA");
                    instance.setValue(newData.attribute("pressure"), currentWeather.getPressure());
                }else if(indicesToKeep[i] == 2){
                    Log.d(TAG, "UMIDITATE");
                    instance.setValue(newData.attribute("humidity"), currentWeather.getHumidity());
                }
            }
            instance.setDataset(newData);

            statsStr += "Last instance: " + instance + "\n";
            Log.d(TAG, "Last instance " + instance);
            statsStr += "Predicted value: " + model.classifyInstance(instance);
            Log.d(TAG, "Systolic BP value " + model.classifyInstance(instance));
            statsStr += "\n";
            Dialog displaySets = new Dialog(getContext());
            displaySets.setContentView(R.layout.dialog_weka);
            final TextView statsTv = displaySets.findViewById(R.id.statisticsTv);
            statsTv.setMovementMethod(new ScrollingMovementMethod());
            statsTv.setText(statsStr);
            displaySets.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            displaySets.create();
            displaySets.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean dataExists(){
        if(cardioRecordList.isEmpty() && weatherRecordList.isEmpty())
            return false;
        return true;
    }

    private boolean isDataValid(){
        if(systolic.isEmpty() && diastolic.isEmpty() && pulse.isEmpty() && temperature.isEmpty() && pressure.isEmpty() && humidity.isEmpty() && windSpeed.isEmpty())
            return false;
        return true;
    }

    private void setClickableButtons(boolean clickable){
        linearRegressionBtn.setClickable(clickable);
        multilayerPerceptronBtn.setClickable(clickable);
        SMORegBtn.setClickable(clickable);
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
                }else{
                    setClickableButtons(false);
                    loadingDialog.dismissDialog();
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
                    cardioRecordSet = new HashSet<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CardioRecord cardioRecord = snapshot.getValue(CardioRecord.class);
                        if (cardioRecord != null) {
                            cardioRecordSet.add(cardioRecord);
                        }
                    }
                    cardioRecordList = new ArrayList<>(cardioRecordSet);
                    Collections.sort(cardioRecordList, (CardioRecord c1, CardioRecord c2) -> sortCardioRecordByDate(c1, c2));
                    setData();
                }else{
                    setClickableButtons(false);
                    loadingDialog.dismissDialog();
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
                    weatherRecordSet = new HashSet<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        WeatherRecord weatherRecord = snapshot.getValue(WeatherRecord.class);
                        if (weatherRecord != null) {
                            weatherRecordSet.add(weatherRecord);
                        }
                    }
                    weatherRecordList = new ArrayList<>(weatherRecordSet);
                    Collections.sort(weatherRecordList, (WeatherRecord w1, WeatherRecord w2) -> sortWeatherRecordByDate(w1, w2));
                    retrieveCardioRecords();
                }else{
                    setClickableButtons(false);
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int sortWeatherRecordByDate(WeatherRecord w1, WeatherRecord w2) {
        String time1 = w1.getRecordingDate() + " " + w1.getRecordingHour();
        String time2 = w2.getRecordingDate() + " " + w2.getRecordingHour();

        try {
            Date recordingDate1 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(time1);
            Date recordingDate2 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(time2);

            boolean d2Befored1 = recordingDate2.before(recordingDate1);
            boolean d2Afterd1 = recordingDate2.after(recordingDate1);
            if (!d2Befored1 && !d2Afterd1) {
                return 0;
            } else if (d2Befored1) {
                return 1;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int sortCardioRecordByDate(CardioRecord c1, CardioRecord c2) {
        String time1 = c1.getRecordingDate() + " " + c1.getRecordingHour();
        String time2 = c2.getRecordingDate() + " " + c2.getRecordingHour();

        try {
            Date recordingDate1 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(time1);
            Date recordingDate2 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(time2);

            boolean d2Befored1 = recordingDate2.before(recordingDate1);
            boolean d2Afterd1 = recordingDate2.after(recordingDate1);
            if (!d2Befored1 && !d2Afterd1) {
                return 0;
            } else if (d2Befored1) {
                return 1;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void createEntryLists() {
        initializeUtils();
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

                            Log.d(TAG, "C: " + c);
                            Log.d(TAG, "W:" + w);

                            temperature.add(new Entry(timeMillis, (float) w.getTemperature()));
                            pressure.add(new Entry(timeMillis, (float) w.getPressure()/10));
                            humidity.add(new Entry(timeMillis, (float) w.getHumidity()));
                            windSpeed.add(new Entry(timeMillis, (float) w.getWindSpeed()));

                            systolic.add(new Entry(timeMillis, c.getSystolicBP()));
                            diastolic.add(new Entry(timeMillis, c.getDiastolicBP()));
                            pulse.add(new Entry(timeMillis, c.getPulse()));

                            addBloodPressureToCategory(c);
                            addInstanceToInstancesList(c, w);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if(isDataValid()) {
            computeAveragesForBloodPressure();
            createEntryListForBPPieChart();
        }
    }

    private void initializeUtils(){
        systolic = new ArrayList<>();
        diastolic = new ArrayList<>();
        pulse = new ArrayList<>();

        temperature = new ArrayList<>();
        pressure = new ArrayList<>();
        humidity = new ArrayList<>();
        windSpeed = new ArrayList<>();

        bloodPressurePieEntry = new ArrayList<>();
        bpAveragesOnCategories = new HashMap<>();

        instances = new ArrayList<>();

        normalBp = 0;
        elevatedBp = 0;
        hypertensionStageI = 0;
        hypertensionStageII = 0;
        hypertensiveCrisis = 0;

        bpAveragesOnCategories.put("Normală", 0f);
        bpAveragesOnCategories.put("Crescută", 0f);
        bpAveragesOnCategories.put("Hipertensiune - stadiul I", 0f);
        bpAveragesOnCategories.put("Hipertensiune - stadiul II", 0f);
        bpAveragesOnCategories.put("Criză hipertensivă", 0f);
    }

    private void addBloodPressureToCategory(CardioRecord c){
        if(c.getSystolicBP() < 120 && c.getDiastolicBP() < 80){
            bpAveragesOnCategories.put("Normală", bpAveragesOnCategories.get("Normală") + c.getSystolicBP());
            normalBp++;
            Log.d(TAG, "Normala: " + c.getSystolicBP() + "." + c.getDiastolicBP());
        }else if(c.getSystolicBP() >= 120 && c.getSystolicBP() <= 129 && c.getDiastolicBP() < 80){
            bpAveragesOnCategories.put("Crescută", bpAveragesOnCategories.get("Crescută") + c.getSystolicBP());
            elevatedBp++;
            Log.d(TAG, "Crescuta: " + c.getSystolicBP() + "." + c.getDiastolicBP());
        }else if((c.getSystolicBP() >= 130 && c.getSystolicBP() <= 139) || (c.getDiastolicBP() >= 80 && c.getDiastolicBP() <= 89)){
            bpAveragesOnCategories.put("Hipertensiune - stadiul I", bpAveragesOnCategories.get("Hipertensiune - stadiul I") + c.getSystolicBP());
            hypertensionStageI++;
            Log.d(TAG, "Hipertensiune - stadiul I: " + c.getSystolicBP() + "." + c.getDiastolicBP());
        }else if(c.getSystolicBP() >= 140 || c.getDiastolicBP() >= 90){
            bpAveragesOnCategories.put("Hipertensiune - stadiul II", bpAveragesOnCategories.get("Hipertensiune - stadiul II") + c.getSystolicBP());
            hypertensionStageII++;
            Log.d(TAG, "Hipertensiune - stadiul II: " + c.getSystolicBP() + "." + c.getDiastolicBP());
        }else if(c.getSystolicBP() > 180 || c.getDiastolicBP() > 120){
            bpAveragesOnCategories.put("Criză hipertensivă", bpAveragesOnCategories.get("Criză hipertensivă") + c.getSystolicBP());
            hypertensiveCrisis++;
            Log.d(TAG, "Criza hipertensiva: " + c.getSystolicBP() + "." + c.getDiastolicBP());
        }
    }

    private void addInstanceToInstancesList(CardioRecord c, WeatherRecord w){
        CardioAndWeatherRecord cardioAndWeatherRecord = new CardioAndWeatherRecord();
        cardioAndWeatherRecord.setSystolicBP(c.getSystolicBP());
        cardioAndWeatherRecord.setDiastolicBP(c.getDiastolicBP());
        cardioAndWeatherRecord.setPulse(c.getPulse());
        cardioAndWeatherRecord.setTemperature(w.getTemperature());
        cardioAndWeatherRecord.setPressure(w.getPressure());
        cardioAndWeatherRecord.setHumidity(w.getHumidity());
        instances.add(cardioAndWeatherRecord);
    }

    private void computeAveragesForBloodPressure(){
        for(String category : bpAveragesOnCategories.keySet()){
            if(category.equals("Normală") && normalBp > 0) {
                bpAveragesOnCategories.put(category, bpAveragesOnCategories.get(category) / normalBp);
            }else if(category.equals("Crescută") && elevatedBp > 0){
                bpAveragesOnCategories.put(category, bpAveragesOnCategories.get(category) / elevatedBp);
            }else if(category.equals("Hipertensiune - stadiul I") && hypertensionStageI > 0){
                bpAveragesOnCategories.put(category, bpAveragesOnCategories.get(category) / hypertensionStageI);
            }else if(category.equals("Hipertensiune - stadiul II") && hypertensionStageII > 0){
                bpAveragesOnCategories.put(category, bpAveragesOnCategories.get(category) / hypertensionStageII);
            }else if(category.equals("Criza hipertensivă") && hypertensiveCrisis > 0){
                bpAveragesOnCategories.put(category, bpAveragesOnCategories.get(category) / hypertensiveCrisis);
            }
        }
    }

    private void createEntryListForBPPieChart(){
        for(String category : bpAveragesOnCategories.keySet()){
            if(category.equals("Normală") && normalBp > 0) {
                bloodPressurePieEntry.add(new PieEntry(normalBp, category));
            }else if(category.equals("Crescută") && elevatedBp > 0){
                bloodPressurePieEntry.add(new PieEntry(elevatedBp, category));
            }else if(category.equals("Hipertensiune - stadiul I") && hypertensionStageI > 0){
                bloodPressurePieEntry.add(new PieEntry(hypertensionStageI, category));
            }else if(category.equals("Hipertensiune - stadiul II") && hypertensionStageII > 0){
                bloodPressurePieEntry.add(new PieEntry(hypertensionStageII, category));
            }else if(category.equals("Criza hipertensivă") && hypertensiveCrisis > 0){
                bloodPressurePieEntry.add(new PieEntry(hypertensiveCrisis, category));
            }
        }
    }

    private void computeCorrelationCoefficient(){
        if(isDataValid()) {
            double[] systolicData = new double[systolic.size()];
            double[] diastolicData = new double[diastolic.size()];
            double[] temperatureData = new double[temperature.size()];
            double[] pressureData = new double[pressure.size()];
            double[] humidityData = new double[humidity.size()];
            double[] windSpeedData = new double[windSpeed.size()];

            int i = 0;
            for (Entry e : systolic) {
                systolicData[i++] = e.getY();
            }
            i = 0;
            for (Entry e : diastolic) {
                diastolicData[i++] = e.getY();
            }
            i = 0;
            for (Entry e : temperature) {
                temperatureData[i++] = e.getY();
            }
            i = 0;
            for (Entry e : pressure) {
                pressureData[i++] = e.getY();
            }
            i = 0;
            for (Entry e : humidity) {
                humidityData[i++] = e.getY();
            }
            i = 0;
            for (Entry e : windSpeed) {
                windSpeedData[i++] = e.getY();
            }

            PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
            double temperatureSystolicCorrelationCoefficient = pearsonsCorrelation.correlation(systolicData, temperatureData);
            double temperatureDiastolicCorrelationCoefficient = pearsonsCorrelation.correlation(diastolicData, temperatureData);

            double pressureSystolicCorrelationCoefficient = pearsonsCorrelation.correlation(systolicData, pressureData);
            double pressureDiastolicCorrelationCoefficient = pearsonsCorrelation.correlation(diastolicData, pressureData);

            double humiditySystolicCorrelationCoefficient = pearsonsCorrelation.correlation(systolicData, humidityData);
            double humidityDiastolicCorrelationCoefficient = pearsonsCorrelation.correlation(diastolicData, humidityData);

            double windSpeedSystolicCorrelationCoefficient = pearsonsCorrelation.correlation(systolicData, windSpeedData);
            double windSpeedDiastolicCorrelationCoefficient = pearsonsCorrelation.correlation(diastolicData, windSpeedData);

            DecimalFormat decimalFormatter = new DecimalFormat("#.###");

            String temperatureSystolicCorrelationCoefficientString = String.valueOf(decimalFormatter.format(temperatureSystolicCorrelationCoefficient));
            String temperatureDiastolicCorrelationCoefficientString = String.valueOf(decimalFormatter.format(temperatureDiastolicCorrelationCoefficient));
            SpannableStringBuilder tempSpannable = new SpannableStringBuilder();
            tempSpannable.append("Tensiune arterială sistolică - temperatură: ", new StyleSpan(Typeface.NORMAL), 0);
            tempSpannable.append(temperatureSystolicCorrelationCoefficientString, new ForegroundColorSpan(Color.RED), 0).append('\n');
            tempSpannable.append("Tensiune arterială diastolică - temperatură: ", new StyleSpan(Typeface.NORMAL), 0);
            tempSpannable.append(temperatureDiastolicCorrelationCoefficientString, new ForegroundColorSpan(Color.BLUE), 0);
            bpTempCorrelationTv.setText(tempSpannable);

            String pressureSystolicCorrelationCoefficientString = String.valueOf(decimalFormatter.format(pressureSystolicCorrelationCoefficient));
            String pressureDiastolicCorrelationCoefficientString = String.valueOf(decimalFormatter.format(pressureDiastolicCorrelationCoefficient));
            SpannableStringBuilder pressureSpannable = new SpannableStringBuilder();
            pressureSpannable.append("Tensiune arterială sistolică - presiune atmosferică: ", new StyleSpan(Typeface.NORMAL), 0);
            pressureSpannable.append(pressureSystolicCorrelationCoefficientString, new ForegroundColorSpan(Color.RED), 0).append('\n');
            pressureSpannable.append("Tensiune arterială diastolică - presiune atmosferică: ", new StyleSpan(Typeface.NORMAL), 0);
            pressureSpannable.append(pressureDiastolicCorrelationCoefficientString, new ForegroundColorSpan(Color.BLUE), 0);
            bpPressureCorrelationTv.setText(pressureSpannable);

            String humiditySystolicCorrelationCoefficientString = String.valueOf(decimalFormatter.format(humiditySystolicCorrelationCoefficient));
            String humidityDiastolicCorrelationCoefficientString = String.valueOf(decimalFormatter.format(humidityDiastolicCorrelationCoefficient));
            SpannableStringBuilder humiditySpannable = new SpannableStringBuilder();
            humiditySpannable.append("Tensiune arterială sistolică - umiditate: ", new StyleSpan(Typeface.NORMAL), 0);
            humiditySpannable.append(humiditySystolicCorrelationCoefficientString, new ForegroundColorSpan(Color.RED), 0).append('\n');
            humiditySpannable.append("Tensiune arterială diastolică - umiditate: ", new StyleSpan(Typeface.NORMAL), 0);
            humiditySpannable.append(humidityDiastolicCorrelationCoefficientString, new ForegroundColorSpan(Color.BLUE), 0);
            bpHumidityCorrelationTv.setText(humiditySpannable);

            String windSpeedSystolicCorrelationCoefficientString = String.valueOf(decimalFormatter.format(windSpeedSystolicCorrelationCoefficient));
            String windSpeedDiastolicCorrelationCoefficientString = String.valueOf(decimalFormatter.format(windSpeedDiastolicCorrelationCoefficient));
            SpannableStringBuilder windSpeedSpannable = new SpannableStringBuilder();
            windSpeedSpannable.append("Tensiune arterială sistolică - viteza vântului: ", new StyleSpan(Typeface.NORMAL), 0);
            windSpeedSpannable.append(windSpeedSystolicCorrelationCoefficientString, new ForegroundColorSpan(Color.RED), 0).append('\n');
            windSpeedSpannable.append("Tensiune arterială diastolică - viteza vântului: ", new StyleSpan(Typeface.NORMAL), 0);
            windSpeedSpannable.append(windSpeedDiastolicCorrelationCoefficientString, new ForegroundColorSpan(Color.BLUE), 0);
            bpWindSpeedCorrelationTv.setText(windSpeedSpannable);
        }
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

        XAxis xAxis;
        {
            xAxis = bloodPressureLc.getXAxis();
            xAxis.resetAxisMaximum();
            xAxis.resetAxisMinimum();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(systolic.size(), true);
            xAxis.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    return simpleDateFormat.format(new Date(millis + 1000000));
                }
            });
            if (numberOfEntries > 8) {
                xAxis.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxis;
        {
            yAxis = bloodPressureLc.getAxisLeft();
            bloodPressureLc.getAxisRight().setEnabled(false);
            yAxis.disableAxisLineDashedLine();
            yAxis.resetAxisMaximum();
            yAxis.resetAxisMinimum();

            LimitLine normalBloodPressure = new LimitLine(70, "TA sistolică normală");
            normalBloodPressure.setLineColor(ContextCompat.getColor(getContext(), R.color.normalBP));
            normalBloodPressure.setLineWidth(2f);
            normalBloodPressure.setTextColor(ContextCompat.getColor(getContext(), R.color.normalBP));
            yAxis.addLimitLine(normalBloodPressure);

            LimitLine elevatedBloodPressure = new LimitLine(120, "TA sistolică crescută");
            elevatedBloodPressure.setLineColor(ContextCompat.getColor(getContext(), R.color.elevatedBP));
            elevatedBloodPressure.setLineWidth(2f);
            elevatedBloodPressure.setTextColor(ContextCompat.getColor(getContext(), R.color.elevatedBP));
            yAxis.addLimitLine(elevatedBloodPressure);

            LimitLine hypertensionStage1 = new LimitLine(130, "Hipertensiune - stadiul I");
            hypertensionStage1.setLineColor(ContextCompat.getColor(getContext(), R.color.hypertensionStageI));
            hypertensionStage1.setLineWidth(2f);
            hypertensionStage1.setTextColor(ContextCompat.getColor(getContext(), R.color.hypertensionStageI));
            yAxis.addLimitLine(hypertensionStage1);

            LimitLine hypertensionStage2 = new LimitLine(140, "Hipertensiune - stadiul II");
            hypertensionStage2.setLineColor(ContextCompat.getColor(getContext(), R.color.hypertensionStageII));
            hypertensionStage2.setLineWidth(2f);
            hypertensionStage2.setTextColor(ContextCompat.getColor(getContext(), R.color.hypertensionStageII));
            yAxis.addLimitLine(hypertensionStage2);

            LimitLine hypertensiveCrisis = new LimitLine(180, "Criză hipertensivă");
            hypertensiveCrisis.setLineColor(ContextCompat.getColor(getContext(), R.color.hypertensiveCrisis));
            hypertensiveCrisis.setLineWidth(2f);
            hypertensiveCrisis.setTextColor(ContextCompat.getColor(getContext(), R.color.hypertensiveCrisis));
            yAxis.addLimitLine(hypertensiveCrisis);
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

        XAxis xAxis;
        {
            xAxis = pulseLc.getXAxis();
            xAxis.resetAxisMaximum();
            xAxis.resetAxisMinimum();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(systolic.size(), true);
            xAxis.setValueFormatter(new ValueFormatter() {
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
                xAxis.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxis;
        {
            yAxis = pulseLc.getAxisLeft();
            pulseLc.getAxisRight().setEnabled(false);
            yAxis.disableAxisLineDashedLine();
            yAxis.resetAxisMaximum();
            yAxis.resetAxisMinimum();
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

        XAxis xAxis;
        {
            xAxis = bloodPressureAndTemperatureLc.getXAxis();
            xAxis.resetAxisMaximum();
            xAxis.resetAxisMinimum();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(systolic.size(), true);
            xAxis.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    return simpleDateFormat.format(new Date(millis + 1000000));
                }
            });
            if (numberOfEntries > 8) {
                xAxis.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxis;
        {
            yAxis = bloodPressureAndTemperatureLc.getAxisLeft();
            bloodPressureAndTemperatureLc.getAxisRight().setEnabled(false);
            yAxis.disableAxisLineDashedLine();
            yAxis.resetAxisMaximum();
            yAxis.resetAxisMinimum();
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

        XAxis xAxis;
        {
            xAxis = bloodPressureAndPressureLc.getXAxis();
            xAxis.resetAxisMaximum();
            xAxis.resetAxisMinimum();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(systolic.size(), true);
            xAxis.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    return simpleDateFormat.format(new Date(millis + 1000000));

                }
            });
            if (numberOfEntries > 8) {
                xAxis.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxis;
        {
            yAxis = bloodPressureAndPressureLc.getAxisLeft();
            bloodPressureAndPressureLc.getAxisRight().setEnabled(false);
            yAxis.disableAxisLineDashedLine();
            yAxis.resetAxisMaximum();
            yAxis.resetAxisMinimum();
        }

        Legend legend = bloodPressureAndPressureLc.getLegend();
        legend.setEnabled(true);

        BloodPressureAndPressureMarkerView mv = new BloodPressureAndPressureMarkerView(getContext(), R.layout.custom_markerview_cardio);
        mv.setChartView(bloodPressureAndPressureLc);
        bloodPressureAndPressureLc.setMarker(mv);
    }

    private void showChartBloodPressureAndHumidity() {
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

        humidityDataSet = new LineDataSet(null, null);
        humidityDataSet.setValues(humidity);
        humidityDataSet.setValueFormatter(new FloatValueFormatter());
        humidityDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        humidityDataSet.setColor(Color.GREEN);
        humidityDataSet.setCircleColor(Color.GREEN);
        humidityDataSet.setValueTextSize(10f);
        humidityDataSet.setValueTextColor(Color.GREEN);
        humidityDataSet.setLineWidth(2f);
        humidityDataSet.setCircleRadius(3f);
        humidityDataSet.setFillAlpha(65);
        humidityDataSet.setFillColor(ColorTemplate.getHoloBlue());
        humidityDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        humidityDataSet.setDrawCircleHole(true);
        humidityDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        humidityDataSet.setDrawValues(false);
        humidityDataSet.setLabel("Umiditate");
        humidityDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + "%";
            }
        });

        iLineDataSets = new ArrayList<>();
        iLineDataSets.add(systolicBPDataSet);
        iLineDataSets.add(diastolicBPDataSet);
        iLineDataSets.add(humidityDataSet);
        lineData = new LineData(iLineDataSets);

        bloodPressureAndHumidityLc.clear();
        bloodPressureAndHumidityLc.setBackgroundColor(Color.WHITE);
        bloodPressureAndHumidityLc.getDescription().setEnabled(false);
        bloodPressureAndHumidityLc.setTouchEnabled(true);
        bloodPressureAndHumidityLc.setDrawGridBackground(false);
        bloodPressureAndHumidityLc.setDragEnabled(false);
        bloodPressureAndHumidityLc.setScaleEnabled(false);
        bloodPressureAndHumidityLc.setPinchZoom(false);
        bloodPressureAndHumidityLc.setData(lineData);
        bloodPressureAndHumidityLc.invalidate();
        bloodPressureAndHumidityLc.setMaxVisibleValueCount(numberOfEntries);

        XAxis xAxis;
        {
            xAxis = bloodPressureAndHumidityLc.getXAxis();
            xAxis.resetAxisMaximum();
            xAxis.resetAxisMinimum();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(systolic.size(), true);
            xAxis.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    return simpleDateFormat.format(new Date(millis + 1000000));
                }
            });
            if (numberOfEntries > 8) {
                xAxis.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxis;
        {
            yAxis = bloodPressureAndHumidityLc.getAxisLeft();
            bloodPressureAndHumidityLc.getAxisRight().setEnabled(false);
            yAxis.disableAxisLineDashedLine();
            yAxis.resetAxisMaximum();
            yAxis.resetAxisMinimum();
        }

        Legend legend = bloodPressureAndHumidityLc.getLegend();
        legend.setEnabled(true);

        BloodPressureAndHumidityMarkerView mv = new BloodPressureAndHumidityMarkerView(getContext(), R.layout.custom_markerview_cardio);
        mv.setChartView(bloodPressureAndHumidityLc);
        bloodPressureAndHumidityLc.setMarker(mv);
    }

    private void showChartBloodPressureAndWindSpeed() {
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

        windSpeedDataSet = new LineDataSet(null, null);
        windSpeedDataSet.setValues(windSpeed);
        windSpeedDataSet.setValueFormatter(new FloatValueFormatter());
        windSpeedDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        windSpeedDataSet.setColor(Color.GREEN);
        windSpeedDataSet.setCircleColor(Color.GREEN);
        windSpeedDataSet.setValueTextSize(10f);
        windSpeedDataSet.setValueTextColor(Color.GREEN);
        windSpeedDataSet.setLineWidth(2f);
        windSpeedDataSet.setCircleRadius(3f);
        windSpeedDataSet.setFillAlpha(65);
        windSpeedDataSet.setFillColor(ColorTemplate.getHoloBlue());
        windSpeedDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        windSpeedDataSet.setDrawCircleHole(true);
        windSpeedDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        windSpeedDataSet.setDrawValues(false);
        windSpeedDataSet.setLabel("Umiditate");
        windSpeedDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + "m/s";
            }
        });

        iLineDataSets = new ArrayList<>();
        iLineDataSets.add(systolicBPDataSet);
        iLineDataSets.add(diastolicBPDataSet);
        iLineDataSets.add(windSpeedDataSet);
        lineData = new LineData(iLineDataSets);

        bloodPressureAndWindSpeedLc.clear();
        bloodPressureAndWindSpeedLc.setBackgroundColor(Color.WHITE);
        bloodPressureAndWindSpeedLc.getDescription().setEnabled(false);
        bloodPressureAndWindSpeedLc.setTouchEnabled(true);
        bloodPressureAndWindSpeedLc.setDrawGridBackground(false);
        bloodPressureAndWindSpeedLc.setDragEnabled(false);
        bloodPressureAndWindSpeedLc.setScaleEnabled(false);
        bloodPressureAndWindSpeedLc.setPinchZoom(false);
        bloodPressureAndWindSpeedLc.setData(lineData);
        bloodPressureAndWindSpeedLc.invalidate();
        bloodPressureAndWindSpeedLc.setMaxVisibleValueCount(numberOfEntries);

        XAxis xAxis;
        {
            xAxis = bloodPressureAndWindSpeedLc.getXAxis();
            xAxis.resetAxisMaximum();
            xAxis.resetAxisMinimum();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(systolic.size(), true);
            xAxis.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

                @Override
                public String getFormattedValue(float value) {
                    long millis = (long) value;
                    return simpleDateFormat.format(new Date(millis + 1000000));
                }
            });
            if (numberOfEntries > 8) {
                xAxis.setLabelCount(numberOfLabels);
            }
        }

        YAxis yAxis;
        {
            yAxis = bloodPressureAndWindSpeedLc.getAxisLeft();
            bloodPressureAndWindSpeedLc.getAxisRight().setEnabled(false);
            yAxis.disableAxisLineDashedLine();
            yAxis.resetAxisMaximum();
            yAxis.resetAxisMinimum();
        }

        Legend legend = bloodPressureAndWindSpeedLc.getLegend();
        legend.setEnabled(true);

        BloodPressureAndWindSpeedMarkerView mv = new BloodPressureAndWindSpeedMarkerView(getContext(), R.layout.custom_markerview_cardio);
        mv.setChartView(bloodPressureAndWindSpeedLc);
        bloodPressureAndWindSpeedLc.setMarker(mv);
    }

    private void showChartHypertensionStages() {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        bloodPressurePieDataSet.setValues(bloodPressurePieEntry);
        bloodPressurePieDataSet.setDrawIcons(false);
        bloodPressurePieDataSet.setSliceSpace(3f);
        bloodPressurePieDataSet.setIconsOffset(new MPPointF(0, 40));
        bloodPressurePieDataSet.setSelectionShift(5f);
        bloodPressurePieDataSet.setColors(colors);

        pieData = new PieData(bloodPressurePieDataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int)value + "%";
            }
        });
        pieData.setValueTextSize(15f);
        pieData.setValueTextColor(Color.WHITE);
        pieData.setDrawValues(true);

        hypertensionStagesPc.setEntryLabelColor(Color.BLACK);
        hypertensionStagesPc.getDescription().setEnabled(false);
        hypertensionStagesPc.setUsePercentValues(true);

        Legend legend = hypertensionStagesPc.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setWordWrapEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);

        hypertensionStagesPc.setDrawEntryLabels(false);
        hypertensionStagesPc.setData(pieData);
        hypertensionStagesPc.highlightValues(null);
        hypertensionStagesPc.invalidate();
    }

    private void createCharts() {
        if (!isDetached() && getContext() != null) {
            showChartBloodPressure();
            showChartPulse();
            showChartBloodPressureAndTemperature();
            showChartBloodPressureAndPressure();
            showChartBloodPressureAndHumidity();
            showChartBloodPressureAndWindSpeed();
            showChartHypertensionStages();
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class GetWeather extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            retrieveCurrentWeather("http://www.meteoromania.ro/wp-json/meteoapi/v2/starea-vremii");
            return null;
        }

        private void retrieveCurrentWeather(String urlString){
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try{
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                JSONObject jsonObject = new JSONObject(buffer.toString());
                parseItems(jsonObject);
            }catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void parseItems(JSONObject jsonObject) throws IOException, JSONException {
            JSONArray weatherRecordJsonArray = jsonObject.getJSONArray("features");
            for(int i = 0; i < weatherRecordJsonArray.length(); i++){
                JSONObject currentFeatureObject = weatherRecordJsonArray.getJSONObject(i);
                JSONObject geometryObject = currentFeatureObject.getJSONObject("geometry");
                JSONArray coordinatesArray = geometryObject.getJSONArray("coordinates");
                JSONObject propertiesObject = currentFeatureObject.getJSONObject("properties");
                if(propertiesObject.getString("nume").equals(city)) {
                    currentWeather.setLatitude(Double.valueOf(coordinatesArray.getString(0)));
                    currentWeather.setLongitude(Double.valueOf(coordinatesArray.getString(1)));
                    currentWeather.setCity(propertiesObject.getString("nume"));
                    currentWeather.setHumidity(propertiesObject.getInt("umezeala"));
                    currentWeather.setNebulosity(propertiesObject.getString("nebulozitate"));
                    currentWeather.setTemperature(Double.valueOf(propertiesObject.getString("tempe")));
                    String windSpeedStr = propertiesObject.getString("vant").replace("\\", ")");
                    if (windSpeedStr.matches("\\w*")) {
                        currentWeather.setWindSpeed(0.0);
                    } else {
                        currentWeather.setWindSpeed(Double.valueOf(windSpeedStr.split("m")[0]));
                    }
                    currentWeather.setPressure(Double.valueOf(propertiesObject.getString("presiunetext").split("mb")[0]));
                    String[] date = propertiesObject.getString("actualizat").split("&nbsp;ora&nbsp;");
                    currentWeather.setRecordingDate(date[0]);
                    currentWeather.setRecordingHour(date[1]);
                }
            }
        }

    }
}

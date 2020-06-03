package ro.uvt.asavoaei.andreea.cardiovascularapp.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.LoadingDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.CardioRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.UserProfile;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.WeatherRecord;
import ro.uvt.asavoaei.andreea.cardiovascularapp.utils.FloatValueFormatter;

public class DashboardFragment extends Fragment {
    private static final String TAG = DashboardFragment.class.getName();
    private LoadingDialog loadingDialog;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private String emailAddress = "", firstname = "";
    private TextView welcomeTv, systolicTv, diastolicTv, pulseTv, cholesterolTv;
    private LineChart systolicLc;
    private LineChart diastolicLc;
    private LineDataSet systolicDataSet = new LineDataSet(null, null);
    private LineDataSet diastolicDataSet = new LineDataSet(null, null);
    private ArrayList<ILineDataSet> iLineSystolicDataSets = new ArrayList<>();
    private ArrayList<ILineDataSet> iLineDiastolicDataSets = new ArrayList<>();
    private LineData lineData;
    private List<Integer> systolicValues = new ArrayList<>();
    private List<Integer> diastolicValues = new ArrayList<>();
    private int maxSystolic = 0;
    private int maxDiastolic = 0;
    private int lastPulse = 0;
    private int lastCholesterol = 0;
    private ArrayList<Entry> systolic = new ArrayList<>();
    private ArrayList<Entry> diastolic = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, null);
        loadingDialog = new LoadingDialog(getContext());
        welcomeTv = view.findViewById(R.id.greetingTv);
        systolicTv = view.findViewById(R.id.systolicValueTv);
        diastolicTv = view.findViewById(R.id.diastolicValueTv);
        pulseTv = view.findViewById(R.id.pulseValueTv);
        cholesterolTv = view.findViewById(R.id.cholesterolValueTv);
        systolicLc = view.findViewById(R.id.systolicLc);
        diastolicLc = view.findViewById(R.id.diastolicLc);
        if (firebaseAuth.getCurrentUser() != null) {
            emailAddress = firebaseAuth.getCurrentUser().getEmail();
            new PumpDataTask().execute();
        }
        return view;
    }

    private void setFirstname() {
        Query getUserProfile = databaseReference.child("user-profile").orderByChild("emailAddress").equalTo(emailAddress);
        getUserProfile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserProfile currentUserProfile = snapshot.getValue(UserProfile.class);
                        if (currentUserProfile != null) {
                            firstname = currentUserProfile.getFirstname();
                            welcomeTv.setText(getGreeting(firstname));
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

    private String getGreeting(String firstname) {
        int hour = LocalTime.now().getHour();
        String greeting = "";
        if (hour >= 5 && hour <= 10) {
            greeting += "Bună dimineața,";
        } else if (hour >= 11 && hour <= 17) {
            greeting += "Bună ziua,";
        } else if (hour >= 17 && hour <= 21) {
            greeting += "Bună seara,";
        } else {
            greeting += "Bun venit,";
        }
        greeting += "\n" + firstname;
        return greeting;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    private void retrieveBloodPressureData() {
        Query getCardioRecordsByEmail = databaseReference.child("cardio-record").orderByChild("emailAddress").equalTo(emailAddress).limitToLast(3);
        getCardioRecordsByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    systolicValues = new ArrayList<>();
                    diastolicValues = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CardioRecord cardioRecord = snapshot.getValue(CardioRecord.class);
                        if (cardioRecord != null) {
                            systolicValues.add(cardioRecord.getSystolicBP());
                            diastolicValues.add(cardioRecord.getDiastolicBP());
                        }
                    }
                    for (int i = 0; i < systolicValues.size(); i++) {
                        systolic.add(new Entry(i, systolicValues.get(i)));
                    }
                    showChartSystolic();

                    for (int i = 0; i < diastolicValues.size(); i++) {
                        diastolic.add(new Entry(i, diastolicValues.get(i)));
                    }
                    showChartDiastolic();
                }else{
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showChartSystolic() {
        if (!isDetached() && getContext() != null) {
            systolicDataSet.setValues(systolic);
            systolicDataSet.setValueFormatter(new FloatValueFormatter());
            systolicDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            systolicDataSet.setColor(getResources().getColor(R.color.colorPrimary));
            systolicDataSet.setCircleColor(getResources().getColor(R.color.colorPrimary));
            systolicDataSet.setValueTextSize(10f);
            systolicDataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryDark));
            systolicDataSet.setLineWidth(2f);
            systolicDataSet.setCircleRadius(3f);
            systolicDataSet.setFillAlpha(65);
            systolicDataSet.setFillColor(ColorTemplate.getHoloBlue());
            systolicDataSet.setHighLightColor(Color.rgb(244, 117, 117));
            systolicDataSet.setDrawCircleHole(true);
            systolicDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            iLineSystolicDataSets.clear();
            iLineSystolicDataSets.add(systolicDataSet);
            lineData = new LineData(iLineSystolicDataSets);
            systolicLc.clear();
            systolicLc.setBackgroundColor(Color.WHITE);
            systolicLc.getDescription().setEnabled(false);
            systolicLc.setTouchEnabled(false);
            systolicLc.setDrawGridBackground(false);
            systolicLc.setDragEnabled(false);
            systolicLc.setScaleEnabled(false);
            systolicLc.setPinchZoom(false);

            XAxis xAxisSystolic;
            {
                xAxisSystolic = systolicLc.getXAxis();
                xAxisSystolic.setEnabled(false);
                xAxisSystolic.resetAxisMaximum();
                xAxisSystolic.resetAxisMinimum();
            }

            YAxis yAxisSystolic;
            {
                yAxisSystolic = systolicLc.getAxisLeft();
                systolicLc.getAxisRight().setEnabled(false);
                yAxisSystolic.disableAxisLineDashedLine();
                yAxisSystolic.resetAxisMaximum();
                yAxisSystolic.resetAxisMinimum();
            }

            Legend legend = systolicLc.getLegend();
            legend.setEnabled(false);

            systolicLc.setData(lineData);
            systolicLc.invalidate();
        }

    }

    private void showChartDiastolic() {
        if (!isDetached() && getContext() != null) {
            diastolicDataSet.setValues(diastolic);
            diastolicDataSet.setValueFormatter(new FloatValueFormatter());
            diastolicDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            diastolicDataSet.setColor(getResources().getColor(R.color.colorPrimary));
            diastolicDataSet.setCircleColor(getResources().getColor(R.color.colorPrimary));
            diastolicDataSet.setValueTextSize(10f);
            diastolicDataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryDark));
            diastolicDataSet.setLineWidth(2f);
            diastolicDataSet.setCircleRadius(3f);
            diastolicDataSet.setFillAlpha(65);
            diastolicDataSet.setFillColor(ColorTemplate.getHoloBlue());
            diastolicDataSet.setHighLightColor(Color.rgb(244, 117, 117));
            diastolicDataSet.setDrawCircleHole(true);
            diastolicDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            iLineDiastolicDataSets.clear();
            iLineDiastolicDataSets.add(diastolicDataSet);
            lineData = new LineData(iLineDiastolicDataSets);
            diastolicLc.clear();
            diastolicLc.setBackgroundColor(Color.WHITE);
            diastolicLc.getDescription().setEnabled(false);
            diastolicLc.setTouchEnabled(false);
            diastolicLc.setDrawGridBackground(false);
            diastolicLc.setDragEnabled(false);
            diastolicLc.setScaleEnabled(false);
            diastolicLc.setPinchZoom(false);

            XAxis xAxisDiastolic;
            {
                xAxisDiastolic = diastolicLc.getXAxis();
                xAxisDiastolic.setEnabled(false);
                xAxisDiastolic.resetAxisMaximum();
                xAxisDiastolic.resetAxisMinimum();
            }

            YAxis yAxisDiastolic;
            {
                yAxisDiastolic = diastolicLc.getAxisLeft();
                diastolicLc.getAxisRight().setEnabled(false);
                yAxisDiastolic.disableAxisLineDashedLine();
                yAxisDiastolic.resetAxisMaximum();
                yAxisDiastolic.resetAxisMinimum();
            }

            Legend legend = diastolicLc.getLegend();
            legend.setEnabled(false);

            diastolicLc.setData(lineData);
            diastolicLc.invalidate();
        }
    }


    private void computeMaximumValues() {
        Query getCardioRecordsByEmail = databaseReference.child("cardio-record").orderByChild("emailAddress").equalTo(emailAddress);
        getCardioRecordsByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CardioRecord cardioRecord = snapshot.getValue(CardioRecord.class);
                        if (cardioRecord != null) {
                            if (cardioRecord.getSystolicBP() > maxSystolic) {
                                maxSystolic = cardioRecord.getSystolicBP();
                            }

                            if (cardioRecord.getDiastolicBP() > maxDiastolic) {
                                maxDiastolic = cardioRecord.getDiastolicBP();
                            }
                        }
                    }
                    systolicTv.setText(String.valueOf(maxSystolic));
                    diastolicTv.setText(String.valueOf(maxDiastolic));
                }else{
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLastPulseAndCholesterol() {
        Query getCardioRecordsByEmail = databaseReference.child("cardio-record").orderByChild("emailAddress").equalTo(emailAddress).limitToLast(1);
        getCardioRecordsByEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CardioRecord cardioRecord = snapshot.getValue(CardioRecord.class);
                        if (cardioRecord != null) {
                            lastPulse = cardioRecord.getPulse();
                            lastCholesterol = cardioRecord.getCholesterol();
                        }
                    }
                    pulseTv.setText(String.valueOf(lastPulse));
                    cholesterolTv.setText(String.valueOf(lastCholesterol));
                }
                loadingDialog.dismissDialog();
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
            setFirstname();
            retrieveBloodPressureData();
            computeMaximumValues();
            getLastPulseAndCholesterol();
            return null;
        }

    }


}

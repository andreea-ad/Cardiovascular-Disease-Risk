package ro.uvt.asavoaei.andreea.cardiovasculardiseaserisk.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import ro.uvt.asavoaei.andreea.cardiovasculardiseaserisk.R;
import ro.uvt.asavoaei.andreea.cardiovasculardiseaserisk.dialog.InternetConnectionDialog;

public class CreateAccount25Activity extends AppCompatActivity implements LocationListener {
    private static final int firstNameMinimumLength = 2;
    private static final int lastNameMinimumLength = 3;
    private InternetConnectionDialog dialog;
    private EditText firstNameEt, lastNameEt, cityEt;
    private FloatingActionButton fabNext;
    private CircularProgressIndicator circularProgressIndicator;
    private LocationManager locationManager;
    private String locationProvider;
    private boolean isFirstNameValid = false;
    private boolean isLastNameValid = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_25);
        dialog = new InternetConnectionDialog(this);
        final Intent intent = getIntent();
        fabNext = findViewById(R.id.fabNext50);
        firstNameEt = findViewById(R.id.firstNameEt);
        lastNameEt = findViewById(R.id.lastNameEt);
        cityEt = findViewById(R.id.cityEt);
        circularProgressIndicator = findViewById(R.id.createAccountHeaderCpi);

        firstNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isFirstNameValid = checkFirstName(s.toString());
            }
        });

        lastNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isLastNameValid = checkLastName(s.toString());
            }
        });

        fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFirstNameValid && isLastNameValid && !cityEt.getText().toString().isEmpty()) {
                    Intent open = new Intent(getApplicationContext(), CreateAccount50Activity.class);
                    open.putExtra("emailAddress", intent.getStringExtra("emailAddress"));
                    open.putExtra("password", intent.getStringExtra("password"));

                    open.putExtra("firstname", firstNameEt.getText().toString());
                    open.putExtra("lastname", lastNameEt.getText().toString());
                    open.putExtra("city", cityEt.getText().toString());
                    startActivity(open);
                }
            }
        });

        circularProgressIndicator.setProgress(25, 100);

        checkLocationPermission();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        locationProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(locationProvider);

        if (location != null) {
            onLocationChanged(location);
        } else {
            Toast.makeText(getApplicationContext(), "Vă rugăm să activați locația.", Toast.LENGTH_SHORT).show();
            cityEt.setText(getResources().getText(R.string.city_ro));
        }
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreateAccount25Activity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locationManager.requestLocationUpdates(locationProvider, 400, 1, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.stopMonitor();
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> address = geocoder.getFromLocation(latitude, longitude, 1);
            cityEt.setText(address.get(0).getLocality());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String locationProvider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String locationProvider) {

    }

    @Override
    public void onProviderDisabled(String locationProvider) {

    }

    private boolean checkFirstName(String firstName) {
        boolean isValid = false;
        if (firstName.length() >= firstNameMinimumLength) {
            isValid = true;
        }

        if (!isValid) {
            firstNameEt.setError("Prenumele trebuie să conțină minim " + firstNameMinimumLength + " caractere.");
            firstNameEt.requestFocus();
        } else {
            firstNameEt.setError(null);
        }

        return isValid;
    }

    private boolean checkLastName(String lastName) {
        boolean isValid = false;
        if (lastName.length() >= lastNameMinimumLength) {
            isValid = true;
        }

        if (!isValid) {
            lastNameEt.setError("Numele trebuie să conțină minim " + lastNameMinimumLength + " caractere.");
            lastNameEt.requestFocus();
        } else {
            lastNameEt.setError(null);
        }

        return isValid;
    }

}

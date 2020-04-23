package ro.uvt.asavoaei.andreea.cardiovasculardiseaserisk.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import ro.uvt.asavoaei.andreea.cardiovasculardiseaserisk.R;
import ro.uvt.asavoaei.andreea.cardiovasculardiseaserisk.dialog.InternetConnectionDialog;

public class CreateAccount50Activity extends AppCompatActivity {
    private static final String TAG = CreateAccount50Activity.class.getCanonicalName();
    private static final int heightMinimumValue = 55;
    private static final int heightMaximumValue = 272;
    private final Calendar myCalendar = Calendar.getInstance();
    private InternetConnectionDialog dialog;
    private EditText birthdayEt, heightEt;
    private Spinner genderSpinner;
    private CheckBox smokerCheckbox, pregnantCheckbox;
    private FloatingActionButton fabNext;
    private CircularProgressIndicator circularProgressIndicator;
    private boolean isBirthdayValid = false;
    private boolean isHeightValid = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_50);
        dialog = new InternetConnectionDialog(this);
        birthdayEt = findViewById(R.id.birthdayEt);
        final Intent intent = getIntent();
        fabNext = findViewById(R.id.fabNext75);
        circularProgressIndicator = findViewById(R.id.createAccountHeaderCpi);
        birthdayEt = findViewById(R.id.birthdayEt);
        genderSpinner = findViewById(R.id.genderSpinner);
        ArrayAdapter<CharSequence> genderSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.genders_array_ro, R.layout.custom_spinner_item_gender);
        heightEt = findViewById(R.id.heightEt);
        smokerCheckbox = findViewById(R.id.smokerCb);
        pregnantCheckbox = findViewById(R.id.pregnantCb);

        birthdayEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    isBirthdayValid = true;
                }
            }
        });

        heightEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isHeightValid = checkHeight(Integer.parseInt(s.toString()));
            }
        });

        fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBirthdayValid && isGenderValid() && isHeightValid) {
                    Intent open = new Intent(getApplicationContext(), CreateAccount75Activity.class);
                    open.putExtra("emailAddress", intent.getStringExtra("emailAddress"));
                    open.putExtra("password", intent.getStringExtra("password"));
                    open.putExtra("firstname", intent.getStringExtra("firstname"));
                    open.putExtra("lastname", intent.getStringExtra("lastname"));
                    open.putExtra("city", intent.getStringExtra("city"));

                    open.putExtra("dob", birthdayEt.getText().toString());
                    open.putExtra("gender", genderSpinner.getSelectedItem().toString());
                    open.putExtra("height", heightEt.getText().toString());
                    open.putExtra("smoker", smokerCheckbox.isChecked());
                    if (genderSpinner.getSelectedItem().toString().equals("Bărbat")) {
                        open.putExtra("pregnant", false);
                    } else {
                        open.putExtra("pregnant", pregnantCheckbox.isChecked());
                    }
                    startActivity(open);
                }
            }
        });


        circularProgressIndicator.setProgress(50, 100);


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateBirthday();
            }

        };

        birthdayEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(CreateAccount50Activity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        genderSpinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_item_gender);
        genderSpinner.setAdapter(genderSpinnerAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGeneder = parent.getItemAtPosition(position).toString();
                if (selectedGeneder.equals("Femeie")) {
                    pregnantCheckbox.setVisibility(View.VISIBLE);
                } else {
                    pregnantCheckbox.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void updateBirthday() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        birthdayEt.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.stopMonitor();
    }

    private boolean isGenderValid() {
        boolean isValid = false;
        String genderStr = genderSpinner.getSelectedItem().toString();

        if (genderStr.equals("Femeie") || genderStr.equals("Bărbat")) {
            isValid = true;
        }

        return isValid;
    }

    private boolean checkHeight(int height) {
        boolean isValid = false;
        if (height >= 55 && height <= 272) {
            isValid = true;
        }

        if (!isValid) {
            heightEt.setError("Înălțimea trebuie să fie între " + heightMinimumValue + " cm și " + heightMaximumValue + " cm.");
            heightEt.requestFocus();
        } else {
            heightEt.setError(null);
        }

        return isValid;
    }
}

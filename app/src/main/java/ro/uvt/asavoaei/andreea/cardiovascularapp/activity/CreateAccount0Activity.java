package ro.uvt.asavoaei.andreea.cardiovascularapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.InternetConnectionDialog;

public class CreateAccount0Activity extends AppCompatActivity {
    private static final String TAG = CreateAccount0Activity.class.getCanonicalName();
    private static final int passwordMinimumLength = 10;
    private InternetConnectionDialog dialog;
    private EditText emailAddressEt;
    private EditText passwordEt;
    private FloatingActionButton fabNext;
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_0);
        dialog = new InternetConnectionDialog(this);
        emailAddressEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        Intent intent = getIntent();
        fabNext = findViewById(R.id.fabNext25);

        emailAddressEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isEmailValid = checkEmail(s.toString());
            }
        });

        passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isPasswordValid = checkPassword(s.toString());
            }
        });

        fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmailValid && isPasswordValid) {
                    Intent open = new Intent(getApplicationContext(), CreateAccount25Activity.class);
                    open.putExtra("emailAddress", emailAddressEt.getText().toString());
                    open.putExtra("password", passwordEt.getText().toString());
                    startActivity(open);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.stopMonitor();
    }

    private boolean checkEmail(String emailAddress) {
        boolean isValid = Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();

        if (!isValid) {
            emailAddressEt.setError("Adresa de email introdusă nu este validă.");
            emailAddressEt.requestFocus();
        } else {
            emailAddressEt.setError(null);
        }

        return isValid;
    }

    private boolean checkPassword(String password) {
        boolean isValid = false;
        if (password.length() >= passwordMinimumLength) {
            isValid = true;
        }

        if (!isValid) {
            passwordEt.setError("Parola trebuie să conțină minim " + passwordMinimumLength + " caractere.");
            passwordEt.requestFocus();
        } else {
            passwordEt.setError(null);
        }

        return isValid;
    }


}
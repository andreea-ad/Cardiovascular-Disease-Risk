package ro.uvt.asavoaei.andreea.cardiovascularapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.InternetConnectionDialog;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private InternetConnectionDialog dialog;
    private EditText emailAddressEt, passwordEt;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dialog = new InternetConnectionDialog(this);
        emailAddressEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        loginButton = findViewById(R.id.loginBtn);
        registerButton = findViewById(R.id.registerBtn);
        firebaseAuth = FirebaseAuth.getInstance();

        SpannableString registerString = new SpannableString("Nu aveți un cont? Înregistrați-vă");
        registerString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)), 18, 33, 0);
        registerButton.setText(registerString, TextView.BufferType.SPANNABLE);
        registerString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)), 18, 33, 0);
        registerButton.setText(registerString, TextView.BufferType.SPANNABLE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signInWithEmailAndPassword(emailAddressEt.getText().toString(), passwordEt.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent openMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                    openMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(openMainActivity);
                                } else {
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
        registerButton.setOnClickListener(v -> {
            Intent openCreateAccountActivity = new Intent(getApplicationContext(), CreateAccount0Activity.class);
            startActivity(openCreateAccountActivity);
        });
    }

}

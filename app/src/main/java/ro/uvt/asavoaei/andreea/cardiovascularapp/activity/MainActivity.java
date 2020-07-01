package ro.uvt.asavoaei.andreea.cardiovascularapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.dialog.InternetConnectionDialog;
import ro.uvt.asavoaei.andreea.cardiovascularapp.fragment.CollectDataFragment;
import ro.uvt.asavoaei.andreea.cardiovascularapp.fragment.DashboardFragment;
import ro.uvt.asavoaei.andreea.cardiovascularapp.fragment.HistoryFragment;
import ro.uvt.asavoaei.andreea.cardiovascularapp.fragment.ProfileFragment;
import ro.uvt.asavoaei.andreea.cardiovascularapp.fragment.StatisticsFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Fragment active = new Fragment();
    private InternetConnectionDialog dialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                Intent openLoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                openLoginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(openLoginActivity);
            }
        });
        dialog = new InternetConnectionDialog(this);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.stopMonitor();
    }

    /**
     * Initialize the active fragment with the corresponding type of fragment based on tapped icon
     * @param menuItem
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_home:
                active = new DashboardFragment();
                break;
            case R.id.action_stats:
                active = new StatisticsFragment();
                break;
            case R.id.action_add:
                active = new CollectDataFragment();
                break;
            case R.id.action_history:
                active = new HistoryFragment();
                break;
            case R.id.action_profile:
                active = new ProfileFragment();
                break;
        }
        return loadFragment(active);
    }

    /**
     * Load the active fragment that was initialized
     * @param fragment
     * @return
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()   // start a series of operations to edit the fragment manager state
                    .replace(R.id.fragment_container, fragment)   // replace the fragment container with the selected fragment
                    .commit();
            return true;
        }
        return false;
    }

}
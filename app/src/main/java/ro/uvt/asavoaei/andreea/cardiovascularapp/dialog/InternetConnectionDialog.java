package ro.uvt.asavoaei.andreea.cardiovascularapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.androidstudy.networkmanager.Monitor;
import com.androidstudy.networkmanager.Tovuti;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;


public class InternetConnectionDialog {
    private Context context;
    private Dialog internetConnectionDialog;

    public InternetConnectionDialog(Context context) {
        this.context = context;
        createDialog();
        monitor();
    }

    private void monitor() {
        Tovuti.from(context).monitor(new Monitor.ConnectivityListener() {
            @Override
            public void onConnectivityChanged(int connectionType, boolean isConnected, boolean isFast) {
                if (isConnected) {
                    dismissDialog();
                } else {
                    showDialog();
                }
            }
        });
    }

    private void createDialog() {
        internetConnectionDialog = new Dialog(context);
        internetConnectionDialog.setContentView(R.layout.dialog_no_internet_connection);
        internetConnectionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        internetConnectionDialog.create();
    }

    private void showDialog() {
        if (internetConnectionDialog != null) {
            internetConnectionDialog.show();
        }
    }

    private void dismissDialog() {
        if (internetConnectionDialog != null) {
            internetConnectionDialog.dismiss();
        }
    }

    public void stopMonitor() {
        Tovuti.from(context).stop();
    }


}

package ro.uvt.asavoaei.andreea.cardiovascularapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;


public class LoadingDialog {
    private Context context;
    private Dialog loadingDialog;

    public LoadingDialog(Context context) {
        this.context = context;
        createDialog();
    }


    private void createDialog() {
        loadingDialog = new Dialog(context);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.create();
    }

    public void showDialog() {
        if (loadingDialog != null) {
            loadingDialog.show();
        }
    }

    public void dismissDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

}

package com.example.healthlineadminapp;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import androidx.annotation.NonNull;

public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_progressbar);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
}
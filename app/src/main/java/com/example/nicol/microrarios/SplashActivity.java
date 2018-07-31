package com.example.nicol.microrarios;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load Timetable
        TimetableAsyncTask asyncTask = new TimetableAsyncTask(this, getFilesDir().getAbsolutePath()+"/", getFilesDir().getAbsolutePath()+"/");
        asyncTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("TEST", "SPLASH DESTROYED");
    }
}

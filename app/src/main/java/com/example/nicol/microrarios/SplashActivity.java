package com.example.nicol.microrarios;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import bus.InformationLoadException;
import bus.timetable.TimetablePABB;

public class SplashActivity extends AppCompatActivity {

    // Shared preferences
    public static final String PREFERENCES = "com.verali.apps.Preferences";
    public static final String DEPARTURE_STOP_KEY = "com.verali.apps.Preferences.departureStop";
    public static final String ARRIVAL_STOP_KEY = "com.verali.apps.Preferences.arrivalStop";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Try to load timetables from storage
        try{
            new TimetablePABB.Builder(TimetablePABB.PUNTA_ALTA_TIMETABLE_ID).forceLoadFromStorage(getFilesDir().getAbsolutePath()).build();
            new TimetablePABB.Builder(TimetablePABB.BAHIA_BLANCA_TIMETABLE_ID).forceLoadFromStorage(getFilesDir().getAbsolutePath()).build();
            // If it work, start HomeActivity
            startActivity(new Intent(this, HomeActivity.class));
        }catch (InformationLoadException e){
            // It didn't work, try to load them from web and serialize them
            TimetableAsyncTask asyncTask = new TimetableAsyncTask(this, getFilesDir().getAbsolutePath(), getFilesDir().getAbsolutePath());
            asyncTask.execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("TEST", "SPLASH DESTROYED");
    }
}

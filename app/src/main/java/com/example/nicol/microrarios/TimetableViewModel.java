package com.example.nicol.microrarios;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import bus.BusStop;
import bus.timetable.BusTimetable;
import bus.timetable.TimetablePABB;

public class TimetableViewModel extends AndroidViewModel {
    // Shared preference keys
    public static final String PUNTA_ALTA_TIMETABLE_PATH = "com.verali.apps.TimetableViewModel.puntaAltaStoragePath";
    public static final String BAHIA_BLANCA_TIMETABLE_PATH = "com.verali.apps.TimetableViewModel.bahiaBlancaStoragePath";

    // Attributes
    private BusTimetable PuntaAltaTimetable;
    private BusTimetable BahiaBlancaTimetable;
    private BusStop departureStop;
    private BusStop arrivalStop;

    // Constructor
    public TimetableViewModel(Application application){
        super(application);
        PuntaAltaTimetable = new TimetablePABB.Builder(TimetablePABB.PUNTA_ALTA_TIMETABLE_ID).forceLoadFromStorage(getApplication().getFilesDir().getAbsolutePath()).build();
        BahiaBlancaTimetable = new TimetablePABB.Builder(TimetablePABB.BAHIA_BLANCA_TIMETABLE_ID).forceLoadFromStorage(getApplication().getFilesDir().getAbsolutePath()).build();
        departureStop = TimetablePABB.getStopFromName(getApplication().getSharedPreferences(SplashActivity.PREFERENCES, 0).getString(SplashActivity.DEPARTURE_STOP_KEY, null));
        arrivalStop = TimetablePABB.getStopFromName(getApplication().getSharedPreferences(SplashActivity.PREFERENCES,0).getString(SplashActivity.ARRIVAL_STOP_KEY, null));

    }

    // Methods
    public BusTimetable getTimetable() {
        // TODO return the timetable that corresponds to the user selection
        return PuntaAltaTimetable;
    }

    public BusStop getDepartureStop(){
        return departureStop;
    }

    public BusStop getArrivalStop() {
        return arrivalStop;
    }
}

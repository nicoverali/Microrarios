package com.example.nicol.microrarios;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.SharedPreferences;

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
    private boolean informationLoaded = false;

    // Constructor
    public TimetableViewModel(Application application){
        super(application);
        PuntaAltaTimetable = new TimetablePABB.Builder(TimetablePABB.PUNTA_ALTA_TIMETABLE_ID).forceLoadFromStorage((String) getApplication().getSharedPreferences(PUNTA_ALTA_TIMETABLE_PATH,0).getString());
    }

    // Methods
    public BusTimetable getTimetable() {
        if(!informationLoaded)
            loadInformation();
        return PuntaAltaTimetable;
    }

    // Private loader method
    private void loadInformation(){

        PuntaAltaTimetable = new TimetablePABB.Builder(TimetablePABB.PUNTA_ALTA_TIMETABLE_ID).forceLoadFromStorage()
    }
}

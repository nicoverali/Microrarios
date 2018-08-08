package com.example.nicol.microrarios;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bus.BusStop;
import bus.timetable.BusTimetable;
import bus.timetable.TimetablePABB;

public class TimetableViewModel extends AndroidViewModel {
    // Shared preference keys
    public static final String PUNTA_ALTA_TIMETABLE_PATH = "com.verali.apps.TimetableViewModel.puntaAltaStoragePath";
    public static final String BAHIA_BLANCA_TIMETABLE_PATH = "com.verali.apps.TimetableViewModel.bahiaBlancaStoragePath";

    // Attributes
    private Map<BusStop, Integer> stopsHierarchy;
    private BusTimetable PuntaAltaTimetable;
    private BusTimetable BahiaBlancaTimetable;
    private SharedPreferences sharedPreferences;

    // Mutable information
    private MutableLiveData<BusTimetable> currentTimetable = new MutableLiveData<>();
    private MutableLiveData<BusStop> departureStop = new MutableLiveData<>();
    private MutableLiveData<BusStop> arrivalStop = new MutableLiveData<>();

    // Spinners adapters
    private ArrayAdapter<BusStop> departureAdapter;
    private ArrayAdapter<BusStop> arrivalAdapter;

    // Constructor
    public TimetableViewModel(Application application){
        super(application);
        // Set initial values
        sharedPreferences = getApplication().getSharedPreferences(SplashActivity.PREFERENCES,0);
        PuntaAltaTimetable = new TimetablePABB.Builder(TimetablePABB.PUNTA_ALTA_TIMETABLE_ID).forceLoadFromStorage(getApplication().getFilesDir().getAbsolutePath()).build();
        BahiaBlancaTimetable = new TimetablePABB.Builder(TimetablePABB.BAHIA_BLANCA_TIMETABLE_ID).forceLoadFromStorage(getApplication().getFilesDir().getAbsolutePath()).build();
        departureStop.setValue(TimetablePABB.getStopFromName(sharedPreferences.getString(SplashActivity.DEPARTURE_STOP_KEY, null)));
        arrivalStop.setValue(TimetablePABB.getStopFromName(sharedPreferences.getString(SplashActivity.ARRIVAL_STOP_KEY, null)));

        // Set bus stops hierarchy
        stopsHierarchy = new HashMap<>();
        List<BusStop> stops = PuntaAltaTimetable.getBusStops();
        for(int i = 0; i < stops.size(); i++){
            stopsHierarchy.put(stops.get(i), i);
            if(TimetablePABB.getInverse(stops.get(i)) != null){
                stopsHierarchy.put(TimetablePABB.getInverse(stops.get(i)), i);
            }
        }

        // Set current timetable
        currentTimetable.setValue(getCorrectTimetable(departureStop.getValue(), arrivalStop.getValue()));

        // Create spinners adapters
        departureAdapter = new ArrayAdapter<>(getApplication().getApplicationContext(), R.layout.spinner_selector_item);
        departureAdapter.setDropDownViewResource(R.layout.spinner_selector_dropdown_item);
        departureAdapter.setNotifyOnChange(false);
        arrivalAdapter = new ArrayAdapter<>(getApplication().getApplicationContext(), R.layout.spinner_selector_item);
        arrivalAdapter.setDropDownViewResource(R.layout.spinner_selector_dropdown_item);
        arrivalAdapter.setNotifyOnChange(false);
        fillAdapters(departureStop.getValue(), arrivalStop.getValue());
    }

    // Methods
    public BusTimetable getTimetable() {
        return currentTimetable.getValue();
    }

    public BusStop getDepartureStop(){
        return departureStop.getValue();
    }

    public BusStop getArrivalStop() {
        return arrivalStop.getValue();
    }

    public void observeTimetable(LifecycleOwner owner, Observer<BusTimetable> observer){
        currentTimetable.observe(owner, observer);
    }

    public void observeDepartureStop(LifecycleOwner owner, Observer<BusStop> observer){
        departureStop.observe(owner, observer);
    }

    public void observeArrivalStop(LifecycleOwner owner, Observer<BusStop> observer){
        arrivalStop.observe(owner, observer);
    }

    public void observeAnyStopChange(LifecycleOwner owner, Observer<BusStop> observer){
        departureStop.observe(owner, observer);
        arrivalStop.observe(owner, observer);
    }

    public ArrayAdapter<BusStop> getDepartureAdapter(){
        return departureAdapter;
    }

    public ArrayAdapter<BusStop> getArrivalAdapter(){
        return arrivalAdapter;
    }

    public void invertStops(){
        BusStop newDepartureStop = arrivalStop.getValue();
        BusStop newArrivalStop = departureStop.getValue();
        currentTimetable.setValue((currentTimetable.getValue() == PuntaAltaTimetable ? BahiaBlancaTimetable : PuntaAltaTimetable));

        // Check if new stops have an inverse
        newDepartureStop = TimetablePABB.getInverse(newDepartureStop) != null ? TimetablePABB.getInverse(newDepartureStop) : newDepartureStop;
        newArrivalStop = TimetablePABB.getInverse(newArrivalStop) != null ? TimetablePABB.getInverse(newArrivalStop) : newArrivalStop;

        // Modify adapters
        fillAdapters(newDepartureStop, newArrivalStop);

        // Set current bus stops
        departureStop.setValue(newDepartureStop);
        arrivalStop.setValue(newArrivalStop);
    }

    // TODO Make possible to choose any stop
    public void setDepartureStop(BusStop stop){
        boolean dataChanged = false;
        List<BusStop> stops = currentTimetable.getValue().getBusStops();
        if(stops.indexOf(stop) > stops.indexOf(departureStop.getValue())){
            for(int i = stops.indexOf(departureStop.getValue())+1; i <= stops.indexOf(stop); i++){
                arrivalAdapter.remove(stops.get(i));
            }
            dataChanged = true;
        }
        else if(stops.indexOf(stop) < stops.indexOf(departureStop.getValue())){
            for(int i = stops.indexOf(departureStop.getValue()); i > stops.indexOf(stop); i--){
                arrivalAdapter.add(stops.get(i));
            }
            dataChanged = true;
        }
        if(dataChanged){
            arrivalAdapter.notifyDataSetChanged();
            departureStop.setValue(stop);
            sharedPreferences.edit().putString(SplashActivity.DEPARTURE_STOP_KEY, stop.getName()).apply();
        }
    }

    public void setArrivalStop(BusStop stop){
        boolean dataChanged = false;
        List<BusStop> stops = currentTimetable.getValue().getBusStops();
        if(stops.indexOf(stop) < stops.indexOf(arrivalStop.getValue())){
            for(int i = stops.indexOf(arrivalStop.getValue())-1; i >= stops.indexOf(stop); i--){
                departureAdapter.remove(stops.get(i));
            }
            dataChanged = true;
        }
        else if(stops.indexOf(stop) > stops.indexOf(arrivalStop.getValue())){
            for(int i = stops.indexOf(arrivalStop.getValue()); i < stops.indexOf(stop); i++){
                departureAdapter.add(stops.get(i));
            }
            dataChanged = true;
        }
        if(dataChanged){
            departureAdapter.notifyDataSetChanged();
            arrivalStop.setValue(stop);
            sharedPreferences.edit().putString(SplashActivity.ARRIVAL_STOP_KEY, stop.getName()).apply();
        }
    }

    // Private methods
    private BusTimetable getCorrectTimetable(BusStop departureStop, BusStop arrivalStop){
        int stopsDifference = stopsHierarchy.get(arrivalStop) - stopsHierarchy.get(departureStop);
        return (stopsDifference < 0 ? BahiaBlancaTimetable : (stopsDifference > 0 ? PuntaAltaTimetable : null));
    }

    private void fillAdapters(BusStop departureStop, BusStop arrivalStop){
        // Clear adapters
        departureAdapter.clear();
        arrivalAdapter.clear();

        // Fill adapters
        List<BusStop> busStops = currentTimetable.getValue().getBusStops();
        for(int i = 0; busStops.get(i) != arrivalStop; i++){
            departureAdapter.add(busStops.get(i));
        }
        for(int i = busStops.size()-1; busStops.get(i) != departureStop; i--){
            arrivalAdapter.add(busStops.get(i));
        }

        // Notify changes
        departureAdapter.notifyDataSetChanged();
        arrivalAdapter.notifyDataSetChanged();
    }
}

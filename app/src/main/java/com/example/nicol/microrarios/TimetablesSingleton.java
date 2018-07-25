package com.example.nicol.microrarios;

import bus.InformationLoadException;
import bus.timetable.BahiaBlancaTimetable;
import bus.timetable.BusTimeTable;
import bus.timetable.PuntaAltaTimetable;

class TimetablesSingleton {
    private static TimetablesSingleton ourInstance;

    // States
    public static final int STATE_LOADED = 1;
    public static final int STATE_NOT_LOADED = -1;

    // Tables ID
    public static final int PUNTA_ALTA_TIMETABLE_ID = 2932;
    public static final int BAHIA_BLANCA_TIMETABLE_ID = 291;

    // Files path
    private static String mPuntaAltaPath;
    private static String mBahiaBlancaPath;

    // Timetables
    private BusTimeTable mPuntaAltaTimetable;
    private BusTimeTable mBahiaBlancaTimetable;

    // State
    private static int mTimetablesState = STATE_NOT_LOADED;

    static TimetablesSingleton getInstance() {
        if(ourInstance == null){
            ourInstance = new TimetablesSingleton();
        }
        return ourInstance;
    }

    private TimetablesSingleton() {
        try{
            mPuntaAltaTimetable = mPuntaAltaPath == null ? new PuntaAltaTimetable() : new PuntaAltaTimetable(mPuntaAltaPath);
            mBahiaBlancaTimetable = mBahiaBlancaPath == null ? new BahiaBlancaTimetable() : new BahiaBlancaTimetable(mBahiaBlancaPath);
            mTimetablesState = STATE_LOADED;
        }catch (InformationLoadException e){
            mTimetablesState = STATE_NOT_LOADED;
        }
    }


    static void setPuntaAltaPath(String path){
        mPuntaAltaPath = path;
    }

    static void setBahiaBlancaPath(String path){
        mBahiaBlancaPath = path;
    }

    public int getTimetablesState(){
        return mTimetablesState;
    }
    public BusTimeTable getTimetable(int tableId) throws IllegalArgumentException{
        if(tableId != PUNTA_ALTA_TIMETABLE_ID && tableId != BAHIA_BLANCA_TIMETABLE_ID)
            throw new IllegalArgumentException("The ID given is not valid.");
        return (tableId == PUNTA_ALTA_TIMETABLE_ID ? mPuntaAltaTimetable : mBahiaBlancaTimetable);
    }
}

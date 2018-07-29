package com.example.nicol.microrarios;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import bus.timetable.BahiaBlancaTimetable;
import bus.timetable.BusTimeTable;
import bus.timetable.PuntaAltaTimetable;

class TimetableAsyncTask extends AsyncTask<Void, Void, BusTimeTable[]> {
    // Keys for intent arguments (onPostExecute)
    public static final String PUNTA_ALTA_KEY = "com.verali.apps.timetables.puntaalta";
    public static final String BAHIA_BLANCA_KEY = "com.verali.apps.timetables.bahiablanca";

    // Files path
    private String mPuntaAltaPath;
    private String mBahiaBlancaPath;

    // Context
    private Context appContext;

    public TimetableAsyncTask(Context context, String mPuntaAltaPath, String mBahiaBlancaPath){
        this.appContext = context.getApplicationContext();
        this.mPuntaAltaPath = mPuntaAltaPath;
        this.mBahiaBlancaPath = mBahiaBlancaPath;
    }

    @Override
    protected BusTimeTable[] doInBackground(Void... voids) {
        TimetablesSingleton.getInstance();
        return new BusTimeTable[]{new PuntaAltaTimetable(mPuntaAltaPath), new BahiaBlancaTimetable(mBahiaBlancaPath)};
    }

    @Override
    protected void onPostExecute(BusTimeTable[] tables) {
        super.onPostExecute(tables);
        Intent intent = new Intent(appContext, HomeActivity.class);
        intent.putExtra(PUNTA_ALTA_KEY, tables[0]);
        intent.putExtra(BAHIA_BLANCA_KEY, tables[1]);
        appContext.startActivity(intent);
    }

}

package com.example.nicol.microrarios;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import bus.InformationLoadException;
import bus.timetable.TimetablePABB;

class TimetableAsyncTask extends AsyncTask<Void, Void, Void> {
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
    protected Void doInBackground(Void... voids) {
        // Load timetables from web and serialize them.
        new TimetablePABB.Builder(TimetablePABB.PUNTA_ALTA_TIMETABLE_ID).serialize(mPuntaAltaPath);
        new TimetablePABB.Builder(TimetablePABB.BAHIA_BLANCA_TIMETABLE_ID).serialize(mBahiaBlancaPath);
        return null;
    }

    @Override
    protected void onPostExecute(Void voidd) {
        super.onPostExecute(voidd);
        // Test that tables are correctly serialized
        try{
            new TimetablePABB.Builder(TimetablePABB.PUNTA_ALTA_TIMETABLE_ID).forceLoadFromStorage(mPuntaAltaPath);
            new TimetablePABB.Builder(TimetablePABB.BAHIA_BLANCA_TIMETABLE_ID).forceLoadFromStorage(mBahiaBlancaPath);
            Intent intent = new Intent(appContext, HomeActivity.class);
            appContext.startActivity(intent);
        }catch (InformationLoadException e){
            // TODO This behaviour should be different
            throw e;
        }
    }

}

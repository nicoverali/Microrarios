package com.example.nicol.microrarios;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

class TimetableAsyncTask extends AsyncTask<Void, Void, TimetablesSingleton> {
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
    protected TimetablesSingleton doInBackground(Void... voids) {
        TimetablesSingleton.setPuntaAltaPath(mPuntaAltaPath);
        TimetablesSingleton.setBahiaBlancaPath(mBahiaBlancaPath);
        return TimetablesSingleton.getInstance();
    }

    @Override
    protected void onPostExecute(TimetablesSingleton tables) {
        super.onPostExecute(tables);
        if(tables.getTimetablesState() == TimetablesSingleton.STATE_LOADED){
            appContext.startActivity(new Intent(appContext, HomeActivity.class));
        }
    }

}

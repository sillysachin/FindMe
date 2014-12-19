package com.appbootup.findme;

import android.os.AsyncTask;
import android.util.Log;

import retrofit.RetrofitError;

public class OpenCellIdRestService {

    private final static String API_KEY = "722bb384-578f-417d-bf12-0b36050862dd";
    final String TAG = "com.appbootup.findme.OurRestService";

    public void fetch(
            final OpenCellIdService service, final String endPoint,
            final int cid, final int lac, final int mnc, final int mcc) {
        new AsyncTask<Void, Void, OpenCell>() {
            @Override
            protected OpenCell doInBackground(Void... params) {
                try {
                    Log.d(TAG, "Attempting to fetch result from base url: " + endPoint);
                    OpenCell res = service.getCell(API_KEY, mcc, mnc, lac, cid, "json");
                    if (res != null) {
                        Log.d(TAG, "Fetched : " + res.toString() + " from " + endPoint);
                    }
                    return res;
                } catch (RetrofitError retroError) {
                    Log.e(TAG, "RetrofitError error", retroError);
                    return null;
                } catch (Exception e) {
                    Log.e(TAG, "Unknown error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(OpenCell res) {
                if (res != null) {
                    Application.getEventBus().post(res);
                }
            }
        }.execute();
    }
}
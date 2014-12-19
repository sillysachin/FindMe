package com.appbootup.findme;

import retrofit.http.GET;
import retrofit.http.Query;

public interface OpenCellIdService {
    @GET("/cell/get")
    OpenCell getCell(@Query("key") String key, @Query("mcc") int mcc, @Query("mnc") int mnc, @Query("lac") int lac, @Query("cellid") int cellid, @Query("format") String format);
}

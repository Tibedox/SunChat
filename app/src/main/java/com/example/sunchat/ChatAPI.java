package com.example.sunchat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ChatAPI {
    @GET("/sunchat.php")
    Call<List<DataFromDB>> sendQuery(@Query("q") String s);

    @GET("/sunchat.php")
    Call<List<DataFromDB>> sendQuery(@Query("name") String n, @Query("message") String message);
}

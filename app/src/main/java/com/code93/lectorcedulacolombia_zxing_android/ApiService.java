package com.code93.lectorcedulacolombia_zxing_android;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/{document}")
    Call<Customer> obtenerDatos(@Path("document") String document);
}

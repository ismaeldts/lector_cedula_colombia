package com.code93.lectorcedulacolombia_zxing_android;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://192.168.1.6:8080/";
    private Retrofit retrofit;
    private ApiService apiService;

    public ApiClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public void obtenerDatos(final ApiResponseListener listener, String documento) {
        Call<Customer> call = apiService.obtenerDatos(documento); // Obtén el cliente por documento
        call.enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if (response.isSuccessful()) {
                    System.out.println(response.body().getNombre());
                    listener.onSuccess(response.body());
                    // El cliente se pasa al listener
                } else {
                    listener.onError("Error en la respuesta");
                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                listener.onError("Error en la petición: " + t.getMessage());
            }
        });
    }

    // Listener para manejar los resultados
    public interface ApiResponseListener {
        void onSuccess(Customer response);

        void onError(String errorMessage);
    }
}

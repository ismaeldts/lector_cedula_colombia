package com.code93.lectorcedulacolombia_zxing_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;
    TextView tvCedula, tvName, tvLastName, tvEmail;
    Retrofit retrofit;
    ApiService apiService;
    private static final String BASE_URL = "http://192.168.1.6:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvCedula = findViewById(R.id.tvDocumentID);
        tvLastName = findViewById(R.id.tvLastName);
        tvCedula = findViewById(R.id.tvDocumentID);
        tvName = findViewById(R.id.tvFirstName); // Asegúrate de tener el id correcto
        tvLastName = findViewById(R.id.tvLastName); // Asegúrate de tener el id correcto

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

    public void onClick(View view) {
        if (view.getId() == R.id.btnScan) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.PDF_417);
            integrator.setPrompt("Acerca el codigo de barras de la cedula");
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setTorchEnabled(true);
            integrator.initiateScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != CUSTOMIZED_REQUEST_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if (result.getContents() == null) {
            Log.d("MainActivity", "Cancelled scan");
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        } else {
            Log.d("MainActivity", "Scanned: " + result.getContents());
            // Llamada a la función que solo extrae la cédula
            String cedula = parseCedulaFromBarcode(result.getContents());
            String cedulaProcesada = procesarCedulaSegunLongitud(cedula);
            consultarBackend(cedulaProcesada);
            Toast.makeText(this, "Cedula escaneada con exito.", Toast.LENGTH_LONG).show();
            if (cedula != null) {
                Log.d("MainActivity", "Cédula extraída: " + cedula);
            }
        }
    }

    private String parseCedulaFromBarcode(String barcode) {
        if (barcode == null || barcode.length() < 150) {
            return null; // Si el código es demasiado corto o nulo, no procesamos
        }

        String alphaAndDigits = barcode.replaceAll("[^\\p{Alpha}\\p{Digit}\\+\\_]+", " ");
        String[] splitStr = alphaAndDigits.split("\\s+");

        // Usamos la lógica de extracción para encontrar la cédula en el código
        String cedula = null;
        int corrimiento = 0;
        Pattern pat = Pattern.compile("[A-Z]");
        Matcher match = pat.matcher(splitStr[2 + corrimiento]);
        int lastCapitalIndex = -1;

        if (match.find()) {
            lastCapitalIndex = match.start();
            cedula = splitStr[2 + corrimiento].substring(lastCapitalIndex - 10, lastCapitalIndex);
        }

        return cedula;
    }

    private String procesarCedulaSegunLongitud(String cedula) {
        // Si la cédula tiene 10 caracteres, la retornamos tal cual
        String cedulaProcesada = cedula;

        // Si la cédula tiene 10 caracteres y empieza con "0" seguido de un número mayor que "0"
        if (cedula.length() == 10 && cedula.startsWith("0") && cedula.charAt(1) == '0') {
            // Retornamos la cédula sin el primer "0"
            cedulaProcesada = cedula.substring(2);
        }

        if (cedula.length() == 10 && cedula.startsWith("00") && cedula.charAt(1) > '0') {
            // Retornamos la cédula sin el primer "0"
            cedulaProcesada = cedula.substring(1);
        }

        if (cedula.length() == 10 && !cedula.startsWith("0") && cedula.charAt(0) > '0') {
            // Retornamos la cédula sin el primer "0"
            cedulaProcesada = cedula;
        }

        // Si la cédula tiene 8 caracteres y empieza con "00", eliminamos los dos primeros caracteres
        else if (cedula.length() == 8 && cedula.startsWith("00")) {
            cedulaProcesada = cedula.substring(2);
        }

        return cedulaProcesada;
    }

    private void actualizarCampos(String cedula) {

        tvCedula.setText(cedula);

    }

    private void consultarBackend(String cedula) {
        ApiClient apiClient = new ApiClient();
        apiClient.obtenerDatos(new ApiClient.ApiResponseListener() {
            @Override
            public void onSuccess(Customer response) {
                Log.d("API_RESPONSE", "Received customer: " + response.getNombre());
                runOnUiThread(() -> {
                    if (response != null) {
                        tvName.setText(response.getNombre());
                        tvCedula.setText(response.getDocumento());
                        showSuccessDialog();
                    } else {
                        Log.e("API_RESPONSE", "Received null customer");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("API_ERROR", "Error: " + errorMessage);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show()
                );
            }
        }, cedula);
    }

    private void showSuccessDialog() {
        // Crear un AlertDialog para mostrar el mensaje
        new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("Ingreso Permitido")
                .setMessage("El usuario puede ingresar.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

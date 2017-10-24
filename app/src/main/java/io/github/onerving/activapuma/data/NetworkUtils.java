package io.github.onerving.activapuma.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static android.content.ContentValues.TAG;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by onerving on 19/10/17.
 */

public final class NetworkUtils {

    private static String SPORTS_URL = "http://mobile.unam.mx/app2016/deporte/deporte.json";
    private static String CACHE_FILENAME = "lastquery";

    public static String getSportsInfo(Context context) {
        String response = null;
        try {
            // Intentamos obtener la info del servidor
            URL url = new URL(SPORTS_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            urlConnection.disconnect();

            OutputStreamWriter osw =
                    new OutputStreamWriter(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            osw.write(response);
            osw.close();

        } catch (Exception e) {
            // Si no funciona, intentamos obtener la info de un archivo
            Log.d(TAG, "getSportsInfo: cannot load data from server, trying to retrieve from file");
            try {
                InputStream inputStream = context.openFileInput(CACHE_FILENAME);

                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    response = stringBuilder.toString();
                }
            } catch (Exception ee) {
                Log.d(TAG, "getSportsInfo: couldn't open file either, defaulting to empty screen");
            }
        }
        return response;
    }
}

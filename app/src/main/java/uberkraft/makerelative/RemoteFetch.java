package uberkraft.makerelative;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

/**
 * Created by Azalea on 15-02-03.
 */
public class RemoteFetch {

    private static final String FORECAST_API = "https://api.forecast.io/forecast/94554c8a6559d0c2c5cd86c818780f32/%s,%s?units=si";
    private static final String GEOCODE_API = "https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyD7n4UdliKbLCTfpZ6D-mwERJqs8Ro-2Gw&address=%s&sensor=true";

    public static JSONObject getGeocodeJSON(String city) {
        try {
            URL url = new URL(String.format(GEOCODE_API, city));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp = "";

            while ((tmp = reader.readLine()) != null) {
                json.append(tmp).append('\n');
            }
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            return data;
        }
        catch(Exception e) {
            return null;
        }
    }

    public static JSONObject getForecastJSON(String lat, String lng) {
        /*try {
            URL url = new URL(String.format(FORECAST_API, lat, lng));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp = "";

            while ((tmp = reader.readLine()) != null) {
                json.append(tmp).append('\n');
            }

            reader.close();

            JSONObject data = new JSONObject(json.toString());

            if (data.getInt("code") == 400) {
                return null;
            }

            return data;
        }
        catch(Exception e) {
            return null;
        }*/
        HttpURLConnection con = null;
        String responseString = "";
        try {
            URL forecastUrl = new URL(String.format(FORECAST_API, lat, lng));
            con = (HttpURLConnection) forecastUrl.openConnection();
            con.setDoOutput(false);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    responseString = line;
                }
            }
            catch (IOException e) {
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                        reader = null;
                    } catch (IOException e) {
                    }
                }
            }
        }
        catch (IOException e1) {
        }
        try {
            JSONObject json = new JSONObject(responseString);
            return json;
        }
        catch(JSONException e) {
            return null;
        }
    }

}

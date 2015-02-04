package uberkraft.makerelative;

import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.app.Fragment;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by Azalea on 2015-02-03.
 */
public class WeatherFragment extends Fragment{

    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;

    Handler handler;

    public WeatherFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView)rootView.findViewById(R.id.city_field);
        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
        detailsField = (TextView)rootView.findViewById(R.id.details_field);
        currentTemperatureField = (TextView)rootView.findViewById(R.id.current_temperature_field);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateWeatherData(new CityPreference(getActivity()).getCity());
    }

    private void updateWeatherData(final String city){

        new Thread(){
            public void run(){
                final JSONObject geocodeJSON = RemoteFetch.getGeocodeJSON(city);
                if(geocodeJSON == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            new Thread() {
                                public void run() {
                                    long lat;
                                    long lng;
                                    lat = getLat(geocodeJSON);
                                    lng = getLng(geocodeJSON);
                                    cityField.setText(getAddress(geocodeJSON));
                                    final JSONObject forecastJSON = RemoteFetch.getForecastJSON(lat, lng);
                                    renderWeather(forecastJSON);
                                }
                            }.start();
                        }
                    });
                }
            }
        }.start();

    }

    private long getLat(JSONObject json) {
        try {
            return json.getJSONObject("results").getJSONObject("geometry").getJSONObject("location").getLong("lat");
        }
        catch(Exception e) {
            Log.e("MakeRelative", "Failed to get lat from geocode JSON");
            return 0;
        }
    }

    private long getLng(JSONObject json) {
        try {
            return json.getJSONObject("results").getJSONObject("geometry").getJSONObject("location").getLong("lng");
        }
        catch(Exception e) {
            Log.e("MakeRelative", "Failed to get lng from geocode JSON");
            return 0;
        }
    }

    private String getAddress(JSONObject json) {
        try {
            return json.getJSONObject("results").getString("formatted_address");
        }
        catch(Exception e) {
            Log.e("MakeRelative", "Failed to get formatted address from geocode JSON");
            return null;
        }
    }

    private void renderWeather(JSONObject json){
        try {
            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsField.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa");

            currentTemperatureField.setText(
                    String.format("%.2f", main.getDouble("temp"))+ " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            updatedField.setText("Last update: " + updatedOn);

        }catch(Exception e){
            Log.e("MakeRelative", "One or more fields not found in the JSON data");
        }
    }
}

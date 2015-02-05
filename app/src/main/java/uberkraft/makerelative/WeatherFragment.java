package uberkraft.makerelative;

import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
                final JSONObject geocodeJSON = RemoteFetch.getGeocodeJSON(city.replaceAll("\\s",""));
                //final JSONObject geocodeJSON = RemoteFetch.getGeocodeJSON("Toronto");
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
                            cityField.setText(getAddress(geocodeJSON));
                            final String lat = getLat(geocodeJSON);
                            final String lng = getLng(geocodeJSON);
                            new Thread() {
                                public void run() {
                                    final JSONObject forecastJSON = RemoteFetch.getForecastJSON(lat, lng);
                                    if (forecastJSON == null) {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Toast.makeText(getActivity(),
                                                        getActivity().getString(R.string.forecast_not_found),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                    else {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                renderWeather(forecastJSON);
                                            }
                                        });
                                    }
                                }
                            }.start();
                        }
                    });
                }
            }
        }.start();
    }

    private String getLat(JSONObject json) {
        try {
            return json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat");
        }
        catch(Exception e) {
            Log.e("MakeRelative", "Failed to get lat from geocode JSON");
            return null;
        }
    }

    private String getLng(JSONObject json) {
        try {
            return json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng");
        }
        catch(Exception e) {
            Log.e("MakeRelative", "Failed to get lng from geocode JSON");
            return null;
        }
    }

    private String getAddress(JSONObject json) {
        try {
            return json.getJSONArray("results").getJSONObject(0).getString("formatted_address");
        }
        catch(Exception e) {
            Log.e("MakeRelative", "Failed to get formatted address from geocode JSON");
            return null;
        }
    }

    private void renderWeather(JSONObject json){
        try {
            JSONObject currently = json.getJSONObject("currently");
            detailsField.setText(
                    currently.getString("summary").toUpperCase(Locale.US) +
                            "\n" + "Precipitation: " + currently.getString("precipProbability") + "%" +
                            "\n" + "Humidity: " + currently.getString("humidity") + " %");

            currentTemperatureField.setText(
                    String.format("%.2f", currently.getDouble("temperature"))+ " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(currently.getLong("time")*1000));
            updatedField.setText("Last update: " + updatedOn);

        }catch(Exception e){
            Log.e("MakeRelative", "One or more fields not found in the JSON data");
        }
    }

    public void changeCity(String city){
        updateWeatherData(city);
    }
}

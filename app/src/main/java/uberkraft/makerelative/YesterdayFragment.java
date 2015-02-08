package uberkraft.makerelative;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Azalea on 2015-02-05.
 */
public class YesterdayFragment extends Fragment{

    TextView yesterdayCityField;
    TextView yesterdayUpdatedField;
    TextView yesterdayTemperatureField;
    TextView yesterdayHumidityField;
    TextView yesterdayPrecipitationField;
    TextView yesterdayWindField;

    Handler handler;

    public YesterdayFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_yesterday, container, false);
        yesterdayCityField = (TextView)rootView.findViewById(R.id.yesterday_city_field);
        yesterdayUpdatedField = (TextView)rootView.findViewById(R.id.yesterday_updated_field);
        yesterdayTemperatureField = (TextView)rootView.findViewById(R.id.yesterday_temperature_field);
        yesterdayHumidityField = (TextView)rootView.findViewById(R.id.yesterday_humidity_field);
        yesterdayPrecipitationField = (TextView)rootView.findViewById(R.id.yesterday_precipitation_field);
        yesterdayWindField = (TextView)rootView.findViewById(R.id.yesterday_wind_field);

        return rootView;
    }

    public static YesterdayFragment newInstance(String text) {
        YesterdayFragment yesterday = new YesterdayFragment();
        //Bundle b = new Bundle();

        return yesterday;
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
                            yesterdayCityField.setText(getAddress(geocodeJSON));
                            final String lat = getLat(geocodeJSON);
                            final String lng = getLng(geocodeJSON);
                            new Thread() {
                                public void run() {
                                    final JSONObject forecastJSON = RemoteFetch.getYesterdayForecastJSON(lat, lng);
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

            yesterdayHumidityField.setText((int)(currently.getDouble("humidity") * 100) + "%");
            yesterdayPrecipitationField.setText((int)(currently.getDouble("precipProbability") * 100) + "%");
            yesterdayWindField.setText((int)(currently.getDouble("windSpeed")) + "mph");

            yesterdayTemperatureField.setText((int)(currently.getDouble("temperature")) + "˚");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(currently.getLong("time")*1000));
            yesterdayUpdatedField.setText("Last update: " + updatedOn);

        }catch(Exception e){
            Log.e("MakeRelative", "One or more fields not found in the JSON data");
        }
    }

    public void changeCity(String city){
        updateWeatherData(city);
    }
}

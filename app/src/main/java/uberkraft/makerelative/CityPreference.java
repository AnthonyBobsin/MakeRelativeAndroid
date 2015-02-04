package uberkraft.makerelative;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by Azalea on 2015-02-03.
 */
public class CityPreference {

    SharedPreferences prefs;

    public CityPreference(Activity activity) {
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    String getCity() {
        return prefs.getString("city", "Toronto, ON");
    }

    void setCity(String city) {
        prefs.edit().putString("city", city).commit();
    }


}

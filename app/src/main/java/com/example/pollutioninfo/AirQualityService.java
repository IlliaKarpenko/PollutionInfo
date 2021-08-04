package com.example.pollutioninfo;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class AirQualityService {
    String AqiAPI = "b6bcc8809a08a6995ce55b9a266ff860e6097f2e";
    String AQI;
    Context context;

    public AirQualityService(Context context) {
        this.context = context;
    }

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String AQI);
    }

    public void getAQI(String cityName, final VolleyResponseListener volleyResponseListener) { //AQI - Air Quality Index
        String url = "https://api.waqi.info/feed/"+ cityName +"/?token=" + AqiAPI;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //AQI - Air Quality Index
                AQI = "";
                try {
                    JSONObject AQIinfo = response.getJSONObject("data");
                    AQI = AQIinfo.getString("aqi");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                volleyResponseListener.onResponse(AQI);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AQI = "error";
                volleyResponseListener.onError("Error :(");
            }
        });

        // Add a request to the RequestQueue.
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

}

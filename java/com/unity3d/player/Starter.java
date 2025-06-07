package com.unity3d.player;

import static com.unity3d.play.Ember.URL_JSON;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.craftsmanx.lancarjaya.R;
import com.unity3d.appscreen.SplashActivity;
import com.unity3d.play.Ember;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Starter extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        loadUrlData();
        Button playGameButton = findViewById(R.id.play_game);
        playGameButton.setOnClickListener(v -> {
            startSplashActivity();
        });
    }

    private void loadUrlData() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                URL_JSON,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("Koki");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject c = array.getJSONObject(i);

                            Ember.ADMOB_INTERS = c.optString("admob_inters");
                            Ember.ADMOB_INTERS_DUA = c.optString("admob_inters_dua");
                            Ember.DELAY_INTER_FIRST = c.optLong("inter_first");
                            Ember.DELAY_INTER_REPEAT = c.optLong("inter_repeat");

                            Ember.AD_UNIT_ID = c.optString("unity_inter");
                            Ember.UNITY_GAME_ID = c.optString("unity_games");
                            Ember.FIRST_AD_DELAY = c.optLong("first_unity_ads");
                            Ember.REPEAT_AD_DELAY = c.optLong("repeat_unity_ads");
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Error parsing JSON", e);
                    }
                },
                error -> {
                    Log.e("VolleyError", "Volley error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Check Internet Connection: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void startSplashActivity() {
        Intent intent = new Intent(Starter.this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
}

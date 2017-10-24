package io.github.onerving.activapuma.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by onerving on 19/10/17.
 */

public final class JsonUtils{
    public static JSONArray getJsonSportsInfo(Context context){
        String info = NetworkUtils.getSportsInfo(context);
        try{
            JSONObject temp = new JSONObject(info);
            return temp.getJSONArray("Deporte").getJSONObject(0).getJSONArray("events");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

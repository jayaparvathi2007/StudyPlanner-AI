package com.studyplanner.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Thread-safe JSON utility using Gson with custom SQL Date/Time/Timestamp adapters.
 */
public class JsonUtils {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) ->
                    Date.valueOf(json.getAsString()))
            .registerTypeAdapter(Time.class, (JsonSerializer<Time>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Time.class, (JsonDeserializer<Time>) (json, typeOfT, context) -> {
                String val = json.getAsString();
                if (val.length() == 5) val = val + ":00";
                return Time.valueOf(val);
            })
            .registerTypeAdapter(Timestamp.class, (JsonSerializer<Timestamp>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(src)))
            .setPrettyPrinting()
            .create();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static Gson getGson() {
        return gson;
    }
}

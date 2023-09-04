package com.benicio.geradordekmz.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.benicio.geradordekmz.model.PointerModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class PointerStorageUtil {
    private static final String PREF_NAME = "pointer_preferences";
    private static final String KEY = "pointers";

    public static void savePointer(Context context, List<PointerModel> lista) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(lista);
        editor.putString(KEY, json);
        editor.apply();
    }

    public static List<PointerModel> loadPointers(Context context) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString(KEY, "");
            Type type = new TypeToken<List<PointerModel>>() {
            }.getType();
            return gson.fromJson(json, type);
        }catch (Exception e){
            return new ArrayList<>();
        }
    }
}

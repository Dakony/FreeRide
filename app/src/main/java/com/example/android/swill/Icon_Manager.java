package com.example.android.swill;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * Created by DAKONY on 10/18/2018.
 */

public class Icon_Manager {
    private static Hashtable<String, Typeface> cached_icons = new Hashtable<>();
    public static Typeface get_icons(String path, Context context){
        Typeface icons = cached_icons.get(path);
        if(icons == null){
            icons = Typeface.createFromAsset(context.getAssets(),path);
            cached_icons.put(path,icons);
        }
        return icons;
    }
}

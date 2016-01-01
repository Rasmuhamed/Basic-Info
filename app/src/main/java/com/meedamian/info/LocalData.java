package com.meedamian.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;

public class LocalData {
    public static final String PHONE   = "phone";
    public static final String COUNTRY = "country";
    public static final String CITY    = "city";
    public static final String VANITY  = "vanity";


    public static final String LOCATION      = "location";
    public static final String SUBSCRIBER_ID = "subscriber";

    private static final String KEY_UPDATED_SUFFIX  = "_updated";
//    private static final String KEY_REPLACER_SUFFIX = "_replacer";

    private Context c;


    // Lazy singleton stuff
    private static LocalData instance = null;
    private LocalData(@NonNull Context context) {
        this.c = context;
    }
    public static LocalData getInstance(@NonNull Context context) {
        if (instance == null)
            instance = new LocalData(context);
        return instance;
    }

    public void fetchFresh(@NonNull RemoteData.DataCallback dc) {
        RemoteData.fetchFresh(c, dc);
    }

    public String getPublicId() {
        return RemoteData.getPublicId(c);
    }
    public String getPrettyUrl(@Nullable String vanity) {
        return RemoteData.getPrettyUrl(c, vanity);
    }


    private void save(@Nullable String vanity, @Nullable String phone, @Nullable String country, @Nullable String city) {

        // TODO: process and save

        RemoteData.upload(c, vanity, phone, country, city);
    }

    public void saveLocation(@NonNull String country, @NonNull String city) {
        save(null, null, country, city);
    }
    public void saveUserData(@NonNull String vanity, @NonNull String phone) {
        save(vanity, phone, null, null);
    }


    // Shared Preferences stuff
    private SharedPreferences getSp() {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }
    private SharedPreferences.Editor getSpEditor() {
        return getSp().edit();
    }
    public String getString(@NonNull String key) {
        return getSp().getString(key, null);
    }
    public void cacheString(@NonNull String key, @NonNull String val) {
        getSpEditor()
            .putString(key, val)
            .putLong(getUpdatesKey(key), System.currentTimeMillis())
            .apply();
    }


    // Replacing
//    private static String getReplacerKey(String key) {
//        return key + KEY_REPLACER_SUFFIX;
//    }
//    public void setReplacer(String what, String from, String to) {
//        if (!what.equals(CITY) && !what.equals(COUNTRY)) {
//            Log.w("Basic Data", "Attempt to set illegal 'replacer' blocked");
//            return;
//        }
//
//        Replacer oldReplacer = getReplacer(what);
//        Replacer newReplacer = new Replacer(from, to);
//
//        // Fix `from` if another `Replacer` was already set
//        if (oldReplacer.exists() && oldReplacer.to.equals(from))
//            newReplacer.from = oldReplacer.from;
//
//        saveReplacer(what, newReplacer);
//
//        if (what.equals(CITY))
//            this.city = to;
//
//        if (what.equals(COUNTRY))
//            this.country = to;
//    }
//    private Replacer getReplacer(String what) {
//        return new Replacer(
//            getSp().getString(getReplacerKey(what), null)
//        );
//    }
//    private void saveReplacer(String what, Replacer r) {
//        getSpEditor()
//            .putString(getReplacerKey(what), r.toJsonString())
//            .apply();
//    }
//    private String checkReplace(String what, String from) {
//        Replacer r = getReplacer(what);
//        if (r.exists() && from.equals(r.from))
//            return r.to;
//
//        return from;
//    }



    // Magical helpers
    @Contract(pure = true)
    private static String getUpdatesKey(@NonNull String key) {
        return key + KEY_UPDATED_SUFFIX;
    }

    @Nullable
    private static String getStringFromJson(@NonNull JsonObject json, @NonNull String name) {
        JsonElement tmp = json.get(name);
        return (tmp == null) ? null : tmp.getAsString();
    }


    // because inner classes are cool
//    private class Replacer {
//        private static final String FROM = "from";
//        private static final String TO   = "to";
//
//        public String from;
//        public String to;
//
//        public Replacer(@Nullable String jsonString) {
//            if (jsonString != null) {
//                JsonObject json = new JsonParser()
//                    .parse(jsonString)
//                    .getAsJsonObject();
//
//                from = getStringFromJson(json, Replacer.FROM);
//                to = getStringFromJson(json, Replacer.TO);
//            }
//        }
//        public Replacer(@NonNull String from, @NonNull String to) {
//            this.from = from.trim();
//            this.to = to.trim();
//        }
//
//        public String toJsonString() {
//            if (!exists())
//                return null;
//
//            JsonObject jo = new JsonObject();
//            jo.addProperty(Replacer.FROM, from);
//            jo.addProperty(Replacer.TO, to);
//
//            return jo.toString();
//        }
//
//        private boolean exists() {
//            return from != null && to != null;
//        }
//    }
}

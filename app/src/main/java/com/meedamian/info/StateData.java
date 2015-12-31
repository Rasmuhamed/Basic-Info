package com.meedamian.info;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableField;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.julian.locationservice.GeoChecker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.meedamian.info.meh.TextWatcherAdapter;

import java.util.Objects;

public class StateData extends BaseObservable {

    private LocalData  ld;
    private GeoChecker gc;
    private Context    c;

    public ObservableField<String> phone = new ObservableField<>();
    public void setPhone(String phone) {
        this.phone.set(phone);
    }
    public TextWatcher phoneWatcher = new TextWatcherAdapter() {
        @Override public void afterTextChanged(Editable s) {
        if (!Objects.equals(phone.get(), s.toString()))
            phone.set(s.toString());
        }
    };

    public ObservableField<String> vanity = new ObservableField<>();
    public void setVanity(String vanity) {
        this.vanity.set(vanity);
    }
    public TextWatcher vanityWatcher = new TextWatcherAdapter() {
        @Override public void afterTextChanged(Editable s) {
        if (!Objects.equals(vanity.get(), s.toString()))
            vanity.set(s.toString());
        }
    };

    public ObservableField<String> country = new ObservableField<>();
    public void setCountry(String country) {
        this.country.set(country);
    }

    public ObservableField<String> city = new ObservableField<>();
    public void setCity(String city) {
        this.country.set(city);
    }


    public String getVanityHint() {
        return ld.getPublicId();
    }

    public void onVanityFocusChange(final View v, final boolean hasFocus) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            ((EditText)v).setHint(hasFocus ? "Set your vanity" : "");
            }
        }, 200);
    }

    public void onCopyVanity(View v) {
        ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData cd = ClipData.newPlainText("Basic Data user URL", ld.getPrettyUrl(vanity.get()));
        cm.setPrimaryClip(cd);
        Toast.makeText(c, "URL copied to clipboard", Toast.LENGTH_LONG).show();
    }

    public void initGeo() {
        gc.init();
    }

    public void save(@Nullable View v) {
        ld.save(
            phone.get(),
            vanity.get(),
            null,
            null
        );
    }

    private GoogleMap googleMap;
    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        stupidChecker();
    }
    private LatLng position;
    private void stupidChecker() {
        if (googleMap != null && position != null) {
            googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(GeoChecker.getLocationQuery(country.get(), city.get())));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 11));
        }
    }

    // Lazy singleton stuff
    private StateData(Context context, LocalData ld, final GeoChecker gc) {
        this.c = context;
        this.ld = ld;
        this.gc = gc;

        ld.fetchFresh(new RemoteData.DataCallback() {
            @Override
            public void onDataReady(@Nullable String vanity, @Nullable String phone, @Nullable String country, @Nullable String city) {
            setVanity(vanity);
            setPhone(phone);
            setCountry(country);
            setCity(city);

            position = gc.getCoords(country, city);
            if (position != null)
                stupidChecker();
            }
        });
    }
    private static StateData instance = null;
    public static StateData getInstance(Context context) {
        if (instance == null) {
            instance = new StateData(
                context,
                LocalData.getInstance(context),
                new GeoChecker(context)
            );
        }
        return instance;
    }
}

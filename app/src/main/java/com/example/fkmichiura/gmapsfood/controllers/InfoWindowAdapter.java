package com.example.fkmichiura.gmapsfood.controllers;

import android.app.Activity;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.fkmichiura.gmapsfood.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    public InfoWindowAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.custom_info_window, null);

        //Associa as variáveis aos seus respectivos IDs na Layout
        TextView tvName = view.findViewById(R.id.marker_name);
        TextView tvAddress = view.findViewById(R.id.marker_address);
        //RatingBar ratingBar = view.findViewById(R.id.ratingBar);

        //Atribui os dados do marcador às variáveis
        tvName.setText(marker.getTitle());
        tvAddress.setText(marker.getSnippet());
        //ratingBar.setRating(Float.valueOf(marker.getSnippet()));

        return view;
    }
}

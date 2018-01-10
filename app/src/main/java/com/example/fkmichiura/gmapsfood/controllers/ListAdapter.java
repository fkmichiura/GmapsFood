package com.example.fkmichiura.gmapsfood.controllers;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.fkmichiura.gmapsfood.R;
import com.example.fkmichiura.gmapsfood.models.Emporium;

import java.util.ArrayList;

/**
 * Created by Fabio on 09/01/2018.
 */

public class ListAdapter extends RecyclerView.Adapter {

    private ArrayList<Emporium> emporiums;
    private Activity context;

    public ListAdapter(ArrayList<Emporium> emporiums, Activity context) {
        this.emporiums = emporiums;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_layout, parent, false);

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ListViewHolder viewHolder = (ListViewHolder)holder;
        Emporium emporium = emporiums.get(position);

        viewHolder.name.setText(emporium.getNome());
        viewHolder.address.setText(emporium.getEndereco());
        viewHolder.phone.setText(emporium.getTelefone());
        viewHolder.rating.setText(String.valueOf(emporium.getAvaliacao()));
        viewHolder.bar.setRating(emporium.getAvaliacao());
    }

    @Override
    public int getItemCount() {
        return emporiums.size();
    }

    private class ListViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView address;
        private TextView phone;
        private TextView rating;
        private RatingBar bar;

        public ListViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_emporium_name);
            address = itemView.findViewById(R.id.tv_emporium_address);
            phone = itemView.findViewById(R.id.tv_emporium_phone);
            rating = itemView.findViewById(R.id.tv_rating);
            bar = itemView.findViewById(R.id.ratingBar);
        }
    }
}

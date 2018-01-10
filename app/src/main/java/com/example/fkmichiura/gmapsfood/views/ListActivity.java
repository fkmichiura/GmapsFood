package com.example.fkmichiura.gmapsfood.views;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.example.fkmichiura.gmapsfood.R;
import com.example.fkmichiura.gmapsfood.controllers.ListAdapter;
import com.example.fkmichiura.gmapsfood.models.Emporium;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private ArrayList<Emporium> emporiums = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Bundle bundle = getIntent().getExtras();
        emporiums = (ArrayList<Emporium>)bundle.getSerializable("EmporiumList");

        recyclerView = findViewById(R.id.list_recyclerview);
        recyclerView.setAdapter(new ListAdapter(emporiums, ListActivity.this));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                ListActivity.this,
                LinearLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
    }
}

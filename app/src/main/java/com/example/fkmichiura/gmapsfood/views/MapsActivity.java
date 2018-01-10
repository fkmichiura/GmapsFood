package com.example.fkmichiura.gmapsfood.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.fkmichiura.gmapsfood.controllers.InfoWindowAdapter;
import com.example.fkmichiura.gmapsfood.R;
import com.example.fkmichiura.gmapsfood.models.Emporium;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, RoutingListener {

    private GoogleMap mMap;
    private double currentLat, currentLong;

    private ArrayList<Emporium> emporiums = new ArrayList<>();
    private ArrayList<Emporium> selectedEmporiums = new ArrayList<>();
    private ArrayList<Float> distances = new ArrayList<>();
    private List<Polyline> polylines = new ArrayList<>();

    private static final int[] COLORS = new int[]{R.color.colorPrimary};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_nearby:
                getRouteToDestination();
                break;

            case R.id.menu_list:
                selectBestEmporiums();
                Intent intent = new Intent(MapsActivity.this, ListActivity.class);
                intent.putExtra("EmporiumList", selectedEmporiums);
                startActivity(intent);
                break;

            case R.id.menu_logout:
                userSignOut();
                break;
        }
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //InfoWindow Adapter
        InfoWindowAdapter adapter = new InfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(adapter);
        mMap.setOnInfoWindowClickListener(this);

        enableMapControls();

        //Move a câmera para a posição inicial do mapa como sendo as coordenadas do Centro de Presidente Prudente
        LatLng location = new LatLng(-22.123054, -51.388255);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14.0f));

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dataRef = mFirebaseDatabase.getReference();
        dataRef.child("estabelecimentos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getFirebaseData(dataSnapshot);
                showMarkers(mMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Erro: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    //Evento de clique na InfoWindow dos marcadores
    @Override
    public void onInfoWindowClick(final Marker marker) {
        //Indice do marcador selecionado
        //final String index = marker.getId().replace("m", "");

    }

    //---------------------Rota ---------------------
    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Houve um erro inesperado. Tente novamente", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int index) {
        drawRoute(route);
    }

    @Override
    public void onRoutingCancelled() {

    }

    //Recupera os dados da Firebase, adiciona na ArrayList e mostra
    //os marcadores dos respectivos locais na ArrayList
    private void getFirebaseData(DataSnapshot dataSnapshot) {

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Emporium e = ds.getValue(Emporium.class);
            emporiums.add(e);
        }
    }

    //Atribui os dados recuperados na Firebase aos Marcadores no Mapa
    private void showMarkers(GoogleMap googleMap) {
        for (Emporium e : emporiums) {
            // Adiciona um marcador no local
            LatLng location = new LatLng(e.getLatitude(), e.getLongitude());

            String snippetContent = e.getEndereco() + "\n" + e.getTelefone();
            MarkerOptions options = new MarkerOptions()
                    .position(location)
                    .title(e.getNome())
                    .snippet(snippetContent);
            googleMap.addMarker(options);
        }
    }

    //Mostra o botão de capturar posição atual e realiza tal operação
    private void enableMapControls() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    //Encerra sessão do usuário e volta pra Activity de Login
    private void userSignOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    //Envia os dados à Firebase - Obs: Implementar aqui o envio da nota média do estabelecimento
    public void selectBestEmporiums() {

        for(Emporium e : emporiums){
            if(e.getAvaliacao() >= 4.0 && e.getAvaliacao() <= 5.0){
                selectedEmporiums.add(e);
            }
        }
    }

    //Calcula a distância da localização atual com todos os marcadores dos Estabelecimentos e verifica mais próximo
    public Emporium getNearestEmporium() {
        distances.clear();

        if (mMap != null) {
           /* Location location = getCurrentLocation();
            currentLat = location.getLatitude();
            currentLong = location.getLongitude();
*/
            currentLat = mMap.getMyLocation().getLatitude();
            currentLong = mMap.getMyLocation().getLongitude();
        }
        for (Emporium h : selectedEmporiums){
            float[] results = new float[1];
            Location.distanceBetween(currentLat, currentLong, h.getLatitude(), h.getLongitude(), results);
            distances.add(results[0]);
        }
        float distMin = Collections.min(distances);
        return (selectedEmporiums.get(distances.indexOf(distMin)));
    }

    //Captura as coordenadas da posição atual
    public Location getCurrentLocation(){
        Location location;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        assert locationManager != null;
        location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        return location;
    }

    //Configuração da captura dos dados e armazenamento das linhas referentes a rota
    public void getRouteToDestination(){
        deleteRoutes();
        selectBestEmporiums();
        Emporium emporium = getNearestEmporium();

        Log.i("TAG", ">>>>>>>>>>>>>> Nome do estabelecimento: " + emporium.getNome());

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(currentLat, currentLong), new LatLng(emporium.getLatitude(), emporium.getLongitude()))
                .key("AIzaSyCGQTArch3CECItxevhMGebfkWiWNvxheg")
                .build();
        routing.execute();
    }

    //Desenha as linhas da rota, dados os passos armazenados
    public void drawRoute(ArrayList<Route> route){
        if(polylines.size() > 0) {
            deleteRoutes();
        }
        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
        }
    }

    //Remove todos os itens referentes as linhas da rota
    public void deleteRoutes(){
        for(Polyline p : polylines){
            p.remove();
        }
        polylines.clear();
    }
}
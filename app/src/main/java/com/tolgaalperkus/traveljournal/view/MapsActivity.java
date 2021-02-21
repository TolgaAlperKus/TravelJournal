package com.tolgaalperkus.traveljournal.view;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tolgaalperkus.traveljournal.R;
import com.tolgaalperkus.traveljournal.model.Place;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener{

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    SQLiteDatabase database;

    //Kullanıcı geri tuşuna bastığında Listviewı yenileyen
    //onCreate methodunun tekrar çalışmasını sağlamak için onBackPressed oluşturdum.
    @Override
    public void onBackPressed() {

        super.onBackPressed();

        Intent intentToMain = new Intent(this,MainActivity.class);
        startActivity(intentToMain);
        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        //putExtra ile daha önce aktiviteye gönderdiğimiz info keyine ait değeri kontrol ediyorum.
        //Bu kontrol ile kullanıcının listeden veya konum ekle butonu ile mi geldiğini anlıyorum.

        String info = intent.getStringExtra("info");

        if(info.matches("new")){

            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    //Kullanıcıdan konumu yalnızca bir defa alıp sürekli olarak
                    //olduğu konuma sabitlenmesini engellemek için trackboolean ile kontrol ekledim.
                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.tolgaalperkus.traveljournal",MODE_PRIVATE);
                    boolean trackBoolean = sharedPreferences.getBoolean("trackBoolean",false);

                    if (!trackBoolean){
                        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply();
                    }

                }
            };
            //Izinlerin kontrolu yapılıyor kullanıcı bir kere izin verdiyse tekrar sorulmuyor.
            //izin verilmişse kullanıcının en son bilinen konumu gps üzerinden alınarak kaydedilip
            //uygulamayı tekrar açtığında aynı konumdan devam etmesini sağladım.
            if
            (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastLocation != null){
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }
            }
        }else//intent ile aldığımız info "old" ise bu kısım çalışıyor.
            {
                //Liste üzerinde tıklanmış konum bilgisi içindeki enlem ve boylamı alarak
                //map üzerinde konuma odaklanmayı sağladım.
            Place place = (Place)intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(place.latitude,place.longitude);
            String placeName = place.name;

            mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));


        }


    }


    //Daha önceden konum bilgisine erişim izni verildiyse bu kısım çalışıyor ve aynı işlemleri yapıyor.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0){
            if (requestCode == 1){
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Intent intent= getIntent();
                    String info =intent.getStringExtra("info");
                    if (info.matches("new")){
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(lastLocation != null){
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }
                    }else{
                        Place place = (Place)intent.getSerializableExtra("place");
                        LatLng latLng = new LatLng(place.latitude,place.longitude);
                        String placeName = place.name;

                        mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                    }
                }
            }
        }
    }
    //Map üzerinde uzun tıklama gerçekleştirildiğinde alınan latitude ve longitude değerlerine göre
    //Geocoder ile reverse geocoding yaparak enlem boylam değerlerini adres bilgilerine
    //dönüştürüp sonrasında marker ile gösterilmesini sağladım.
    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        String fullAddress ="";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if (addressList != null && addressList.size()>0){
                if (addressList.get(0).getThoroughfare()!= null){
                    address += addressList.get(0).getThoroughfare();
                    if(addressList.get(0).getSubThoroughfare()!=null){
                        address +=" ";
                        address += addressList.get(0).getSubThoroughfare();
                        if(addressList.get(0).getAddressLine(0)!=null){
                            fullAddress += addressList.get(0).getAddressLine(0);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().title(address).position(latLng));

        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;

        final Place place = new Place(address,fullAddress,latitude , longitude);
        //Uzun tıklama hareketinin yanlışlıkla yapılmış olma ihtimalinden dolayı alertDialog ile
        //kullanıcıdan onay istedim.
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Burası kaydedilsin mi?");
        alertDialog.setMessage(place.name);
        alertDialog.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Database yok ise yeni bir database oluşturup var ise üzerine ekleyerek
                //kullanıcının tıkladığı konum bilgilerini kaydediyorum.
                try {
                    database = MapsActivity.this.openOrCreateDatabase("Places", MODE_PRIVATE, null);
                    database.execSQL("CREATE TABLE IF NOT EXISTS places (id INTEGER PRIMARY KEY ,name VARCHAR,address VARCHAR,latitude VARCHAR,longitude VARCHAR)");

                    String toCompile = "INSERT INTO places (name,address,latitude,longitude) VALUES (?,?,?,?)";

                    SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
                    sqLiteStatement.bindString(1,place.name);
                    sqLiteStatement.bindString(2,place.fullAddress);
                    sqLiteStatement.bindString(3, String.valueOf(place.latitude));
                    sqLiteStatement.bindString(4, String.valueOf(place.longitude));
                    sqLiteStatement.execute();

                    Toast.makeText(getApplicationContext(),"Kayıt Başarılı!",Toast.LENGTH_LONG).show();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        alertDialog.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"Iptal Edildi!",Toast.LENGTH_LONG).show();
            }
        });
        alertDialog.show();

    }
}
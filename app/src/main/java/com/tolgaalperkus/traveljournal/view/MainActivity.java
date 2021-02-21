package com.tolgaalperkus.traveljournal.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tolgaalperkus.traveljournal.R;
import com.tolgaalperkus.traveljournal.adapter.CustomAdapter;
import com.tolgaalperkus.traveljournal.model.Place;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase database;
    ArrayList<Place> placeList = new ArrayList<>();
    ListView listView;
    CustomAdapter customAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Oluşturduğumuz menü xmlinin buradan yönetileceğini belirtiyorum.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_place,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Menüde konum ekleye tıklandığında haritaya yönlendiriliyor.
        if (item.getItemId()==R.id.add_place){
            Intent intent = new Intent(this, MapsActivity.class);
            //Putextra ile kullanıcının yeni bir konum oluşturacağı bilgisini gönderiyorum
            intent.putExtra("info","new");
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);

        getData();
    }
    //Database den verilerin ekrana çıkacağı kısım
    public void getData(){

        customAdapter = new CustomAdapter(this,placeList);

        try {
            //sqlite üzerinden aldığımız verileri placesList içine ekliyoruz
            database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM places",null);

            int nameIx = cursor.getColumnIndex("name");
            int addressIx = cursor.getColumnIndex("address");
            int latitudeIx = cursor.getColumnIndex("latitude");
            int longitudeIx= cursor.getColumnIndex("longitude");


            while(cursor.moveToNext()){
                String nameFromDatabase = cursor.getString(nameIx);
                String addressFromDatabase = cursor.getString(addressIx);
                String latitudeFromDatabase = cursor.getString(latitudeIx);
                String longitudeFromDatabase = cursor.getString(longitudeIx);

                Double latitude = Double.parseDouble(latitudeFromDatabase);
                Double longitude = Double.parseDouble(longitudeFromDatabase);

                //Databaseden aldığımız veriler ile place objesi oluşturuyorum.
                //Sonrasında oluşturulan place objesini placeList ArrayListe ekliyorum.
                Place place = new Place (nameFromDatabase,addressFromDatabase,latitude,longitude);
                placeList.add(place);
            }
            customAdapter.notifyDataSetChanged();
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        listView.setAdapter(customAdapter);
        /*
        Listede herhangi bir elemana tıklayan kullanıcının
        listeden tıklayarak geldiğini anlamak için oluşturduğumuz "old" değeri ve
        tıkladığı konum bilgisi ile MapsActiviteye yönlendiriyorum
        */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",placeList.get(position));

                startActivity(intent);
            }
        });

    }
}
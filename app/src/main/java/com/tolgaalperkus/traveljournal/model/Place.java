package com.tolgaalperkus.traveljournal.model;

import java.io.Serializable;
//Serializable listview'a place modelimizin tanıtılması için kullanılıyor.
public class Place implements Serializable {

    //Bu kısımda eklenmek istenen yerlerin isim,konum ve adres bilgilerini tanımlayan model oluşturdum.
    public String name;
    public Double latitude;
    public Double longitude;
    public String fullAddress;

    public Place(String name, String fullAddress, Double latitude,Double longitude){
        this.name = name;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;

    }

}

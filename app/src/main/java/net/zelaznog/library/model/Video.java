package net.zelaznog.library.model;

import android.graphics.Bitmap;

public class Video {
    public String id;
    public String name;
    public String year;
    public String link;
    public String original_title;
    public String image_url;
    public String position;
    public String duration;
    public String image64;
    public String season_id;
    public String description;
    public String overview;
    public String themoviedb_id;
    public String date;
    public String serie_id;
    public Bitmap image;
    public Boolean fixed = false;
    public Video(){}
    public Video(String name, String year, String link) {
        super();
        this.name = name;
        this.year = year;
        this.link = link;
    }
    @Override
    public String toString() {
        //return "Video [id=" + id + ", name=" + name + ", year=" + year + ", link=" + link + "]";
        String ano = year;
        if(ano != null){
            ano = year.split("-")[0];
        }else{
            ano = "";
        }
        String original = original_title;
        if (original == null || original.equals(name) ){
            original = "";
        }
        if(original.equals("") || ano.equals("") ){
            return " " + name + " (" + original +  ano +")";
        }else{
            return " " + name + " (" + original +" - "+  ano +")";
        }
    }
}

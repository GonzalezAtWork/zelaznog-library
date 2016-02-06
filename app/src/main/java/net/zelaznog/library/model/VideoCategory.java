package net.zelaznog.library.model;

import java.util.ArrayList;
import java.util.List;

public class VideoCategory {

    public static String[] COLUMNS = {
            "name","year","network","original_title","image_url", "imdbId","tvdbId"
    };
    public String id;
    public String name;
    public String year;
    public String network;
    public String overview;
    public String original_title;
    public String image_url;
    public String imdbId;
    public String tvdbId;
    public Boolean fixed = false;
    public ArrayList<VideoCollection> seasons;
}

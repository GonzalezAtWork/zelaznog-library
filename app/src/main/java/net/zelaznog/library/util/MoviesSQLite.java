package net.zelaznog.library.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import net.zelaznog.library.MovieActivity;
import net.zelaznog.library.model.Video;
import net.zelaznog.library.model.VideoCategory;

import java.util.ArrayList;

public class MoviesSQLite extends DBSQLite {
    public MoviesSQLite(ActionBarActivity context) {
        super(context);
    }
    private static final String TABLE_MOVIES = "zelaznog_movies";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_YEAR = "year";
    private static final String KEY_LINK = "link";
    private static final String KEY_ORIGINAL_TITLE = "original_title";
    private static final String KEY_IMAGE_URL = "image_url";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_POSITION = "position";
    private static final String[] COLUMNS = {KEY_ID,KEY_NAME,KEY_YEAR,KEY_LINK,KEY_ORIGINAL_TITLE,KEY_IMAGE_URL,KEY_DURATION,KEY_POSITION};

    @Override
    public void updateEpisode(Video video) {
        String query = "UPDATE zelaznog_movies SET position = '"+ video.position +"', duration = '"+ video.duration +"' where id = '"+ String.valueOf(video.id) +"' ";
        try {
            db.rawQuery(query, null);
        } catch(Exception e){
            Log.e("zelaznog", e.toString());
        }
    }

    public void addVideo(Video video){

        String query = "SELECT  * FROM " + TABLE_MOVIES + " where "+ KEY_LINK +" = '"+ video.link +"' ";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            Log.v("zelaznog","Query: " + query);
            Log.v("zelaznog","Exists: " + video.name);
        }else {
            String findVideoId = findVideo(video.link);
            if ( findVideoId.equals("") ) {
                ContentValues values = new ContentValues();
                values.put(KEY_NAME, video.name);
                values.put(KEY_YEAR, video.year);
                values.put(KEY_LINK, video.link);
                values.put(KEY_ORIGINAL_TITLE, video.original_title);
                values.put(KEY_IMAGE_URL, video.image_url);
                values.put(KEY_DURATION, video.duration);
                values.put(KEY_POSITION, video.position);
                db.insert(TABLE_MOVIES, null, values);
            }
        }
    }

    public String findVideo(String link){
        String query = "SELECT  * FROM " + TABLE_MOVIES + " where link = '"+ link +"'";
        String ret = "";
        try {
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                Video video = new Video();
                video.id = cursor.getString(0);
                video.name = cursor.getString(1);
                video.year =cursor.getString(2);
                video.link = cursor.getString(3);
                video.original_title = cursor.getString(4);
                video.image_url =cursor.getString(5);
                video.duration = cursor.getString(6);
                video.position = cursor.getString(7);
                ret = video.id;
            }
        } catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
        return ret;
    }
    public ArrayList<Video> getAllMovies() {
        ArrayList<Video> movies = new ArrayList<Video>();
        String query = "SELECT  * FROM " + TABLE_MOVIES + " order by year desc";
        Cursor cursor = db.rawQuery(query, null);
        Video video = null;
        if (cursor.moveToFirst()) {
            do {
                video = new Video();
                video.id = cursor.getString(0);
                video.name = cursor.getString(1);
                video.year =cursor.getString(2);
                video.link = cursor.getString(3);
                video.original_title = cursor.getString(4);
                video.image_url =cursor.getString(5);
                video.duration = cursor.getString(6);
                video.position = cursor.getString(7);
                movies.add(video);
            } while (cursor.moveToNext());
        }
        return movies;
    }
}

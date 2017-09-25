package net.zelaznog.library.util;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import net.zelaznog.library.model.Video;
import net.zelaznog.library.model.VideoCategory;
import net.zelaznog.library.model.VideoCollection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Properties;

public class SeriesSQLite extends DBSQLite {

    public SeriesSQLite(ActionBarActivity context) {
        super(context);
    }
    private static final String TABLE_NAME = "zelaznog_series_episodes";
    private static final String KEY_LINK = "link";

    @Override
    public void updateEpisode(Video episode) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("position", episode.position);
            cv.put("duration",episode.duration);
            db.update(TABLE_NAME, cv, "id="+ String.valueOf(episode.id), null);
        } catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
    }

    public boolean checkLink(String _link){
        boolean retorno = false;
        String query = "SELECT  * FROM " + TABLE_NAME + " where "+ KEY_LINK +" = '"+ _link +"' ";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            Log.v("zelaznog","Exists: " + _link);
            retorno = true;
        }
        return retorno;
    }
    public void removeLinks( ArrayList<VideoCategory> series){
        String query = "SELECT  * FROM " + TABLE_NAME + " ";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Video video = new Video();
                video.id = cursor.getString(0);
                video.name = cursor.getString(1);
                video.link = cursor.getString(3);
                boolean remove = true;
                for (VideoCategory serie : series){
                    for (VideoCollection season : serie.seasons){
                        for (Video checkVideo : season.files){
                            if(video.link.equals( checkVideo.link )){
                                remove = false;
                            }
                        }
                    }
                }
                if(remove){
                    try {
                        db.execSQL("DELETE FROM " + TABLE_NAME + " where link = '"+ video.link +"'");
                    } catch(Exception e){
                        Log.e("zelaznog", e.toString());
                    }
                }
            } while (cursor.moveToNext());
        }
    }
    public void addSerie(VideoCategory serie){
        long SerieID = 0;
        long SeasonID = 0;
        serie.name = cleanSeriesName(serie.name);
        String findSerieId = findSerie(serie.name);
        if( findSerieId.equals("") ) {
            ContentValues values = new ContentValues();
            for (String ble : VideoCategory.COLUMNS) {
                try {
                    Class<?> c = serie.getClass();
                    Field f = c.getDeclaredField(ble);
                    f.setAccessible(true);
                    String val = (String) f.get(serie);
                    values.put(ble, val);
                } catch (Exception e) {
                    Log.e("zelaznog", e.toString());
                }
            }
            SerieID = db.insert("zelaznog_series", null, values);
        }else{
            SerieID = Long.parseLong(findSerieId);
        }
        for (VideoCollection season : serie.seasons){
            String findSeasonId = findSeason(cleanSeasonName(season.name,false), SerieID);
            if( findSeasonId.equals("") ) {
                ContentValues dbSeason = new ContentValues();
                dbSeason.put("name", cleanSeasonName(season.name, false));
                dbSeason.put("serie_id", String.valueOf(SerieID));
                String imgPath = "http://thetvdb.com/banners/seasons/" + serie.tvdbId + "-" + cleanSeasonName(season.name, true) + ".jpg";
                dbSeason.put("image_url", imgPath);
                SeasonID = db.insert("zelaznog_series_seasons", null, dbSeason);
            }else{
                SeasonID = Long.parseLong(findSeasonId);
            }
            for (Video episode : season.files){
                String findEpisodeId = findEpisode(episode.link);
                if ( findEpisodeId.equals("") ) {
                    ContentValues dbFiles = new ContentValues();
                    episode.name = cleanEpisodeName(episode.name);
                    dbFiles.put("name", episode.name);
                    dbFiles.put("link", episode.link);
                    dbFiles.put("serie_id", String.valueOf(SerieID));
                    dbFiles.put("season_id", String.valueOf(SeasonID));
                    dbFiles.put("image_url", episode.image_url);
                    db.insert(TABLE_NAME, null, dbFiles);
                }else{
                    Log.e("zelaznog","Episode already exists: " + episode.name);
                }
            }
        }
    }

    public ArrayList<Video> getAllEpisodes(VideoCollection season) {
        ArrayList<Video> episodes = new ArrayList<Video>();
        String query = "SELECT  * FROM "+ TABLE_NAME +" where season_id = '"+ String.valueOf(season.id) +"' and serie_id = '"+ String.valueOf(season.serie_id) +"' order by name";
        try {
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    try {
                        Video video = new Video();
                        video.id = cursor.getString(0);
                        video.name = cursor.getString(1);
                        video.link = cursor.getString(2);
                        video.serie_id = cursor.getString(3);
                        video.season_id = cursor.getString(4);
                        video.description = cursor.getString(5);
                        video.image_url = cursor.getString(6);
                        video.duration = cursor.getString(7);
                        video.position = cursor.getString(8);
                        episodes.add(video);
                    } catch (Exception e) {
                        Log.e("zelaznog", e.toString());
                    }
                } while (cursor.moveToNext());
            }
        } catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
        return episodes;
    }
    public ArrayList<VideoCollection> getAllSeasons(VideoCategory serie) {
        ArrayList<VideoCollection> seasons = new ArrayList<VideoCollection>();
        String query = "SELECT  * FROM zelaznog_series_seasons where serie_id = '"+ String.valueOf(serie.id) +"' order by name";
        try {
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    try {
                        VideoCollection season = new VideoCollection();
                        season.id = cursor.getString(0);
                        season.name = cursor.getString(1);
                        season.serie_id = cursor.getString(2);
                        season.image_url = cursor.getString(3);
                        seasons.add(season);
                    } catch (Exception e) {
                        Log.e("zelaznog", e.toString());
                    }
                } while (cursor.moveToNext());
            }
        } catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
        return seasons;
    }
    public String findSeason(String name, Long SerieId){
        String query = "SELECT  * FROM zelaznog_series_seasons where name = '"+ name +"' and serie_id = '"+ String.valueOf(SerieId) +"'";
        String ret = "";
        try {
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                VideoCollection season = new VideoCollection();
                season.id = cursor.getString(0);
                season.name = cursor.getString(1);
                season.serie_id = cursor.getString(2);
                season.image_url = cursor.getString(3);
                ret = season.id;
            }
        } catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
        return ret;
    }
    public String findSerie(String name){
        String query = "SELECT  * FROM zelaznog_series where name = '"+ name +"'";
        String ret = "";
        try {
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                Object serie = new VideoCategory();
                Class<?> c = serie.getClass();
                int conter = 0;
                for (String ble : VideoCategory.COLUMNS) {
                    Field f = c.getDeclaredField(ble);
                    f.setAccessible(true);
                    String val = cursor.getString(conter);
                    f.set(serie, val);
                    conter++;
                }
                Field f = c.getDeclaredField("id");
                f.setAccessible(true);
                String val = cursor.getString(conter);
                f.set(serie, val);
                ret = ((VideoCategory) serie).id;
            }
        } catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
        return ret;
    }

    public String findEpisode(String link){
        String query = "SELECT  * FROM "+ TABLE_NAME +" where link = '"+ link +"'";
        String ret = "";
        try {
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                Video video = new Video();
                video.id = cursor.getString(0);
                video.name = cursor.getString(1);
                video.link = cursor.getString(2);
                video.serie_id = cursor.getString(3);
                video.season_id = cursor.getString(4);
                video.description = cursor.getString(5);
                video.image_url = cursor.getString(6);
                video.duration = cursor.getString(7);
                video.position = cursor.getString(8);
                ret = video.id;
            }
        } catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
        return ret;
    }
    public ArrayList<VideoCategory> getAllSeries() {
        ArrayList<VideoCategory> series = new ArrayList<VideoCategory>();
        String query = "SELECT  * FROM zelaznog_series order by name";
        try {
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    try {
                        Object serie = new VideoCategory();
                        Class<?> c = serie.getClass();

                        int conter = 0;
                        for (String ble : VideoCategory.COLUMNS) {
                            Field f = c.getDeclaredField(ble);
                            f.setAccessible(true);
                            String val = cursor.getString(conter);
                            f.set(serie, val);
                            conter++;
                        }
                        Field f = c.getDeclaredField("id");
                        f.setAccessible(true);
                        String val = cursor.getString(conter);
                        f.set(serie, val);
                        series.add((VideoCategory) serie);
                    } catch (Exception e) {
                        Log.e("zelaznog", e.toString());
                    }
                } while (cursor.moveToNext());
            }
        } catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
        return series;
    }

    private String cleanSeriesName(String name){
        String seriesName = "";
        seriesName = name.replace("'", "");
        return seriesName;
    }

    private String cleanSeasonName(String name, Boolean numeric){
        String seasonName = "";
        for (int i = 0; i < 99; i++){
            for (int a = 0; a < 99; a++){
                seasonName = "S" + right("00" + String.valueOf(i),2) + "E" + right("00" + String.valueOf(a),2);
                if(name.indexOf(seasonName) > 0){
                    name = "Season " + right("00" + String.valueOf(i), 2);
                }
            }
        }
        if(numeric) {
            name = name.replace("Season 0", "").replace("Season ", "");
        }
        return name;
    }

    private String cleanEpisodeName(String name){
        String seasonName = "";
        for (int i = 0; i < 99; i++){
            for (int a = 0; a < 99; a++){
                seasonName = "S" + right("00" + String.valueOf(i),2) + "E" + right("00" + String.valueOf(a),2);
                if(name.indexOf(seasonName) > 0){
                    name = "Episode " + right("00" + String.valueOf(a),2);;
                }
            }
        }
        return name;
    }
}

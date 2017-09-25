package net.zelaznog.library.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import net.zelaznog.library.model.Video;
import net.zelaznog.library.model.VideoCategory;

import java.util.ArrayList;

public class DBSQLite extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ZelaznogDataBase";
    private ActionBarActivity app;
    public SQLiteDatabase db = this.getWritableDatabase();

    public DBSQLite(ActionBarActivity context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        app = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String _sql = "";
            _sql = "CREATE TABLE zelaznog_movies ( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "year TEXT, " +
                    "link TEXT, " +
                    "original_title TEXT, " +
                    "image_url TEXT, " +
                    "duration TEXT, " +
                    "position TEXT, " +
                    "overview TEXT  )";
            db.execSQL(_sql);

            _sql = " CREATE TABLE zelaznog_series ( ";
            for (String ble : VideoCategory.COLUMNS) {
                _sql += ble + " TEXT, ";
            }
            _sql += " id INTEGER PRIMARY KEY AUTOINCREMENT ";
            _sql += ")";
            db.execSQL(_sql);

            _sql = "CREATE TABLE zelaznog_series_seasons ( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "serie_id TEXT, " +
                    "image_url TEXT)";
            db.execSQL(_sql);

            _sql = "CREATE TABLE zelaznog_series_episodes ( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "link TEXT, " +
                    "serie_id TEXT, " +
                    "season_id TEXT, " +
                    "description TEXT, " +
                    "image_url TEXT, " +
                    "duration TEXT, " +
                    "position TEXT )";
            db.execSQL(_sql);

        }catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS zelaznog_movies");
        db.execSQL("DROP TABLE IF EXISTS zelaznog_series");
        db.execSQL("DROP TABLE IF EXISTS zelaznog_series_seasons");
        db.execSQL("DROP TABLE IF EXISTS zelaznog_series_episodes");
        this.onCreate(db);
    }

    public String right(String text, int decimals){
        String retorno = text.substring(text.length() - decimals);
        return retorno;
    }

    public void updateEpisode(Video episode) {

    }
}

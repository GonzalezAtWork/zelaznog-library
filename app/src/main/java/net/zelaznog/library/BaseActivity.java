package net.zelaznog.library;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.zelaznog.library.model.App;
import net.zelaznog.library.model.Version;
import net.zelaznog.library.model.Video;
import net.zelaznog.library.model.VideoCategory;
import net.zelaznog.library.model.VideoCollection;
import net.zelaznog.library.model.ZelaznogDB;
import net.zelaznog.library.util.GridViewMovies;
import net.zelaznog.library.util.MoviesSQLite;
import net.zelaznog.library.util.SeriesSQLite;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class BaseActivity extends ActionBarActivity {

    static public String version = "1.0.0128";
    public BaseActivity activity;

    public GridView gridView;
    public GridViewMovies gridAdapter;
    public String bitrate = "MAX";

    public MoviesSQLite dbMovies;
    public SeriesSQLite dbSeries;

    public VideoCategory currentSerie;
    public VideoCollection currentSeason;
    public Video currentEpisode;
    public Video currentVideo;

    public ProgressDialog ringProgressDialog;
    public String cloudAddress = "http://streams.zelaznog.net?tipo=json";
    public String cloudURL = "";
    public String jsonSupportURL = "";
    public String jsonSupportResponse = "";

    public ZelaznogDB dbmodel;

    public CallBack cbObj;
    public Gson gson = new Gson();

    public int layout = R.layout.activity_filmes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(layout);
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Object obj = parent.getItemAtPosition(position);
                if (obj instanceof VideoCategory) {
                    clickItem((VideoCategory) parent.getItemAtPosition(position));
                }
                if (obj instanceof VideoCollection) {
                    clickItem((VideoCollection) parent.getItemAtPosition(position));
                }
                if (obj instanceof Video) {
                    clickItem((Video) parent.getItemAtPosition(position));
                }
            }
        });
    }
    public void clickItem(Object obj){
        Log.v("zelaznog", "clicou sem objeto1");
    }
    public void clickItem(Video obj){
        Log.v("zelaznog", "clicou sem objeto2");
    }
    public void clickItem(VideoCategory obj){
        Log.v("zelaznog", "clicou sem objeto3");
    }
    public void clickItem(VideoCollection obj){
        Log.v("zelaznog", "clicou sem objeto4");
    }
    public void jsonCallBack(String response){
        try {
            cbObj.exec(response);
        }catch(Exception e){
            Log.e("zelaznog","ErrorX:" + e.toString());
        }
    }

    public void getJson(String url){
        jsonSupportURL = url;
        new Thread(new Runnable() {
            public void run() {
                try {
                    URL _url = new URL(jsonSupportURL);
                    HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    jsonSupportResponse = getResponseText(in);
                }catch(Exception e){
                    Log.e("Zelaznog","Error1: " + e.toString());
                }
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        activity.jsonCallBack(jsonSupportResponse);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Bundle bla = data.getExtras();
            String _position = String.valueOf(bla.getInt("position"));
            String _duration = String.valueOf(bla.getInt("duration"));
            String _end_by = String.valueOf(bla.getString("end_by"));
            if (currentVideo != null) {
                currentVideo.duration = _duration;
                currentVideo.position = _position;
                dbMovies.updateEpisode(currentVideo);
            }
            if (currentEpisode != null) {
                currentEpisode.duration = _duration;
                currentEpisode.position = _position;
                dbSeries.updateEpisode(currentEpisode);
            }
            ((android.widget.ArrayAdapter) gridView.getAdapter()).notifyDataSetChanged();
        }catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
        ringProgressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_make_database, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_settings:
                showMsg("Downloading data ...");
                getJson(cloudAddress);
                updateView();
                break;
            case R.id.action_movies:
                intent = new Intent(this, MovieActivity.class);
                startActivity(intent);
                finalizar();
                break;
            case R.id.action_series:
                intent = new Intent(this, SeriesActivity.class);
                startActivity(intent);
                finalizar();
                break;
            case R.id.action_reset:
                if(activity instanceof MovieActivity) {
                    dbMovies.onUpgrade(dbMovies.getWritableDatabase(), 1, 1);
                }
                if(activity instanceof SeriesActivity) {
                    dbSeries.onUpgrade(dbSeries.getWritableDatabase(), 1, 1);
                }
                updateView();
                break;
            case R.id.action_quit:
                finalizar();
            case R.id.bitrate500:
                item.setChecked(true);
                bitrate = "500";
                break;
            case R.id.bitrate1500:
                item.setChecked(true);
                bitrate = "1500";
                break;
            case R.id.bitrateMAX:
                item.setChecked(true);
                bitrate = "MAX";
                break;
            case R.id.action_update:
                checkUpdate();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMsg(String msg){
        showMsg("Updating Database", msg);
    }
    public void showMsg(String title, String msg){
        try {
            if (ringProgressDialog == null) {
                ringProgressDialog = ProgressDialog.show(activity, title, msg);
            } else {
                ringProgressDialog.setTitle(title);
                ringProgressDialog.setMessage(msg);
                ringProgressDialog.show();
            }
        }catch( Exception e){
            Log.e("Zelanzog","Error2:"+ e.toString());
        }
    }

    public static String getResponseText(InputStream inStream) {
        return new Scanner(inStream).useDelimiter("\\A").next();
    }

    public void updateView(){};

    @Override
    public void onBackPressed() {
        if(ringProgressDialog != null) {
            ringProgressDialog.dismiss();
        }
        if(activity instanceof SeriesActivity && currentSeason != null){
            currentSeason = null;
            clickItem(currentSerie);
            return;
        }
        if(activity instanceof SeriesActivity && currentSerie != null) {
            currentSerie = null;
            updateView();
            return;
        }
        Toast.makeText(this,"Use o menu para sair",Toast.LENGTH_LONG).show();
    }
    private void checkUpdate(){
        showMsg("Checking version...");
        new Thread(new Runnable()
        {
            public void run() {
                try {
                    URL _url = new URL("http://www.zelaznog.net/library/library.json");
                    HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    String response = getResponseText(in);
                    Version objVersion = gson.fromJson(response, Version.class);
                    for(App app: objVersion.apps){
                        if(!isPackageInstalled(app.pkg, app.version)){
                            doUpdate(app.url, app.pkg);
                        }
                    }
                    ringProgressDialog.dismiss();
                }catch(Exception e){
                    ringProgressDialog.dismiss();
                    Log.e("Zelaznog","Error3: " + e.toString());
                }
            }
        }).start();
    }
    public boolean isPackageInstalled(String _packagename, String _versionName) {
        boolean retorno = false;
        try {
            String versionName = activity.getPackageManager().getPackageInfo(_packagename, 0).versionName;
            if(_versionName.equals(versionName)){
                retorno = true;
            }
        } catch (Exception e) {
            Log.e("zelaznog", e.toString());
        }
        return retorno;
    }

    public void doUpdate(String path, String apkName) {
        try {
            URL url = new URL(path);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            String PATH = "/mnt/sdcard/Download/";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, apkName + ".apk");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            InputStream is = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(PATH + apkName +".apk")), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finalizar();
                }
            });
        }catch (Exception e){
            ringProgressDialog.dismiss();
            Log.e("Zelaznog","Error3: " + e.toString());

        }
    }


    public void playVideo(String response){
        String url = "";
        String subtitle = "";
        String file_name = "";
        try {
            JSONObject json = new JSONObject(response);
            url = json.getString("url").toString();
            file_name = json.getString("file_name").toString();
            subtitle = json.getString("subtitle").toString();
        }catch(Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }
        if(url != "") {
            //MXPlayer Intent
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setPackage("com.mxtech.videoplayer.ad");
            //i.setPackage("com.mxtech.videoplayer.pro");
            i.setDataAndType(Uri.parse(url), "video/*");
            i.putExtra("decode_mode", 1);
            i.putExtra("video_zoom", 3);
            i.putExtra("title", file_name);
            i.putExtra("secure_uri", true);
            i.putExtra("subs", new Parcelable[]{Uri.parse(subtitle)});
            i.putExtra("return_result", true);
            if (currentEpisode != null && currentEpisode.position != null && currentEpisode.position != "" && currentEpisode.position != "NULL") {
                i.putExtra("position", Integer.decode(currentEpisode.position));
            }
            if (currentVideo != null && currentVideo.position != null && currentVideo.position != "" && currentVideo.position != "NULL") {
                i.putExtra("position", Integer.decode(currentVideo.position));
            }
            startActivityForResult(i, 111);
        }else{
            ringProgressDialog.dismiss();
        }
    }

    public void processJson(String jsonResponse){
        try {
            dbmodel = gson.fromJson(jsonResponse, ZelaznogDB.class);
            fixDataUsingAPI();
        }catch(Exception e){
            Log.e("Zelaznog","Error4: " + e.toString());
        }
    }
    public void fixDataUsingAPI(){}

    public void finalizar(){
        if(dbMovies != null){
            dbMovies.db.close();
        }
        if(dbSeries != null){
            dbSeries.db.close();
        }
        if(ringProgressDialog != null){
            ringProgressDialog.dismiss();
        }
        finish();
    }
}

interface CallBack {
    void exec(String response);
}
package net.zelaznog.library;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Base64;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class BaseActivity extends ActionBarActivity {

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
    public String strMsg = "";

    public int layout = R.layout.activity_filmes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(layout);

        SharedPreferences settings = getSharedPreferences("zelaznog_prefs", 0);
        useProxy = settings.getBoolean("useProxy", false);
        bitrate = settings.getString("bitrate", "MAX");

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
            if(ringProgressDialog != null) {
                ringProgressDialog.dismiss();
            }
            Log.e("zelaznog","ErrorX:" + e.toString());
        }
    }
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public java.net.CookieManager msCookieManager;

    public void getReal(String url){
        jsonSupportURL = url.replace("getVideo","getVideoUrl");
        new Thread(new Runnable() {
            public void run() {
                try {
                    msCookieManager = new java.net.CookieManager();
                    URL _url = new URL(jsonSupportURL);
                    HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    String zelResponse = getResponseText(in);

                    JSONObject json = new JSONObject(zelResponse);
                    String zelUrl = json.getString("url").toString();
                    String zelFileName = json.getString("file_name").toString();
                    String zelSubtitle = json.getString("subtitle").toString();

                    String COOKIES_HEADER = "Set-Cookie";
                    HashMap<String, String> realParams = new HashMap<String, String>();
                    realParams.put("device_id","");
                    realParams.put("device_name","");
                    realParams.put("_step","auth");
                    realParams.put("rsrc","");
                    realParams.put("pcode","");
                    realParams.put("cpath","");
                    realParams.put("distcode","");
                    realParams.put("origcode","");
                    realParams.put("rpauth","");
                    realParams.put("rpEncodedUser","");
                    realParams.put("rpEncodedPassword","");
                    realParams.put("hashTag","");
                    realParams.put("destination","");
                    realParams.put("close","false");
                    realParams.put("client_version","");
                    realParams.put("facebookCredentials","");
                    realParams.put("facebookEmail","");
                    realParams.put("tos_agree","false");
                    realParams.put("requestSource","WebApp_SignIn");
                    realParams.put("client","browser");
                    realParams.put("rpLoginAccount","");
                    realParams.put("username", json.getString("useremail").toString());
                    realParams.put("password","1EUmesmo");
                    realParams.put("rememberMe","true");
                    URL realUrl = new URL("https://realtimes.real.com/account/auth");
                    HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getPostDataString(realParams));
                    writer.flush();
                    writer.close();
                    os.close();
                    Map<String, List<String>> headerFields = conn.getHeaderFields();
                    List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
                    if(cookiesHeader != null) {
                        for (String cookie : cookiesHeader) {
                            msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                        }
                    }
                    InputStream in1 = new BufferedInputStream(conn.getInputStream());
                    String login = getResponseText(in1);
                    in1.close();
                    String type = "";
                    if(bitrate.equals("MAX")){
                        type = "profile=src&";
                    }else{
                        type = "profile=mp4_h264_aac&bitrate=" + bitrate + '&';
                    }
                    URL url2 = new URL("https://realtimes.real.com/videoStream/"+ json.getString("video").toString() +"?play_original=true&"+ type +"_nocache=" + String.valueOf(System.currentTimeMillis()/1000));
                    HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                    conn2.setRequestMethod("GET");
                    if(msCookieManager.getCookieStore().getCookies().size() > 0) {
                        conn2.setRequestProperty("Cookie", TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
                    }
                    InputStream in2 = new BufferedInputStream(conn2.getInputStream());
                    String realResponse = getResponseText(in2);
                    in2.close();


                    JSONObject realJson = new JSONObject(realResponse);
                    jsonSupportResponse = zelResponse.replace("\"url\":\"\"","\"url\":\""+  realJson.getString("url").toString()  +"\"");

                    Log.v("Zelaznog", jsonSupportResponse);

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
    public String[] proxyCookies;
    public String proxyResponse;
    public Boolean useProxy = false;
    public void getProxyCookies(){
        new Thread(new Runnable() {
            public void run() {
                try {
                    URL _url = new URL("https://www.justproxy.eu/index.php");
                    HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    //proxyResponse = getResponseText(in);
                    proxyResponse = connection.getHeaderField("set-cookie");
                }catch(Exception e){
                    Log.e("Zelaznog","Proxy Error: " + e.toString());
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "Error with proxy", Toast.LENGTH_LONG).show();
                            useProxy = false;
                        }
                    });
                }
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        proxyCookies = new String[] {
                            "Connection","keep-alive",
                            "Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                            "Upgrade-Insecure-Requests","1",
                            "Referer","https://www.justproxy.eu/",
                            "Cookie", proxyResponse
                        };
                        playVideo(strVideo, useProxy);
                    }
                });
            }
        }).start();
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
                    Log.e("Zelaznog", "Error1: " + e.toString());
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

            /*
            new Thread(new Runnable() {
                public void run() {
                    try {
                        msCookieManager = new java.net.CookieManager();
                        URL url2 = new URL("https://realtimes.real.com/signout?_nocache=" + String.valueOf(System.currentTimeMillis()/1000));
                        HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                        conn2.setRequestMethod("GET");
                        if(msCookieManager.getCookieStore().getCookies().size() > 0) {
                            conn2.setRequestProperty("Cookie", TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
                        }
                        msCookieManager = null;
                        InputStream in2 = new BufferedInputStream(conn2.getInputStream());
                        String realResponse = getResponseText(in2);
                        //Log.e("Zelaznog", realResponse);
                        in2.close();
                    }catch(Exception e){
                        Log.e("Zelaznog","Error1: " + e.toString());
                    }
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            //nothing to do
                        }
                    });
                }
            }).start();
            */
            ringProgressDialog.dismiss();
        }catch(Exception e){
            Log.e("zelaznog",e.toString());
        }
    }

    public void loadHistory(String query, Menu menu) {
        Log.d("zelaznog",query);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_make_database, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        final Menu bla = menu;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                loadHistory(query, bla);
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadHistory(query, bla);
                return true;
            }
        });

        MenuItem _useProxy = (MenuItem) menu.findItem(R.id.useProxy);
        if (useProxy) {
            _useProxy.setChecked(true);
        }else{
            _useProxy.setChecked(false);
        }
        MenuItem _bitrate500 = (MenuItem) menu.findItem(R.id.bitrate500);
        MenuItem _bitrate1500 = (MenuItem) menu.findItem(R.id.bitrate1500);
        MenuItem _bitrateMAX = (MenuItem) menu.findItem(R.id.bitrateMAX);
        _bitrate500.setChecked(false);
        _bitrate1500.setChecked(false);
        _bitrateMAX.setChecked(false);
        switch (bitrate){
            case "500":
                _bitrate500.setChecked(true);
                break;
            case "1500":
                _bitrate1500.setChecked(true);
                break;
            case "MAX":
                _bitrateMAX.setChecked(true);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences settings = getSharedPreferences("zelaznog_prefs", 0);
        SharedPreferences.Editor editor = settings.edit();

        //Confirma a gravação dos dados
        editor.commit();

        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_settings:
                showMsg("Downloading data ...");
                cbObj = new CallBack() {
                    @Override
                    public void exec(String response) {
                        processJson(response);
                    }
                };
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
                editor.putString("bitrate", bitrate);
                break;
            case R.id.bitrate1500:
                item.setChecked(true);
                bitrate = "1500";
                editor.putString("bitrate", bitrate);
                break;
            case R.id.bitrateMAX:
                item.setChecked(true);
                bitrate = "MAX";
                editor.putString("bitrate", bitrate);
                break;
            case R.id.useProxy:
                useProxy = (!useProxy);
                item.setChecked(useProxy);
                editor.putBoolean("useProxy", useProxy);
                break;
            case R.id.action_update:
                checkUpdate();
                break;
            default:
                break;
        }
        editor.commit();
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

    public void downloadVideo(String response){
        String _url = "";
        String _subtitle = "";
        String _fileName = "";
        String _bitrate = "";
        try {
            JSONObject json = new JSONObject(response);
            _url = json.getString("url").toString();
            _fileName = json.getString("file_name").toString();
            _subtitle = json.getString("subtitle").toString();
            _bitrate = json.getString("bitrate").toString();
        }catch(Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }
        strMsg += "\nBitrate: " + _bitrate;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                showMsg("Watching", strMsg);
            }
        });
        if(_url != "") {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setPackage("com.okythoos.android.tdmpro");
            i.putExtra(Intent.EXTRA_TEXT, _url);
            i.setType("text/plain");
            startActivity(i);
        }
        if(_subtitle != "") {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setPackage("com.okythoos.android.tdmpro");
            i.putExtra(Intent.EXTRA_TEXT, _subtitle);
            i.setType("text/plain");
            startActivity(i);
        }
        ringProgressDialog.dismiss();
    }
    public String strVideo;
    public void playVideo(String response){
        strVideo = response;
        if(useProxy) {
            getProxyCookies();
        }else{
            playVideo(strVideo, false);
        }
    }
    public void playVideo(String response, Boolean proxy){
        String _url = "";
        String _proxy_url = "";
        String _subtitle = "";
        String _fileName = "";
        String _bitrate = "";

        try {
            JSONObject json = new JSONObject(response);
            _url = json.getString("url").toString();
            _proxy_url = json.getString("proxy_url").toString();
            _fileName = json.getString("file_name").toString();
            _subtitle = json.getString("subtitle").toString();
            _bitrate = json.getString("bitrate").toString();
        }catch(Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }
        strMsg = _fileName;
        strMsg += "\nBitrate: " + _bitrate;
        if (proxy) {
            strMsg += "\nUsing Proxy";
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {
                showMsg("Watching", strMsg );
            }
        });
        if(_url != "") {

            //MXPlayer Intent
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setPackage("com.mxtech.videoplayer.ad");
            //i.setPackage("com.mxtech.videoplayer.pro");
            if(proxy) {
                try {
                    String b64 = Base64.encodeToString(_url.getBytes("UTF-8"), Base64.DEFAULT);
                    b64 = b64.replace("\n","");
                    _url = "https://www.justproxy.eu/index.php?hl=2ed&q=" + b64;
                } catch (Exception e){
                    Log.e("zelaznog",e.getMessage());
                }
            }
            Log.v("zelaznog", "VideoURL: " + _url);
            i.setDataAndType(Uri.parse(_url), "video/*");
            i.putExtra("decode_mode", 1);
            i.putExtra("video_zoom", 3);
            i.putExtra("title", _fileName);
            i.putExtra("secure_uri", true);
            i.putExtra("subs", new Parcelable[]{Uri.parse(_subtitle)});
            if(proxy) {
                i.putExtra("headers", proxyCookies);
            }
            i.putExtra("return_result", true);
            if (currentEpisode != null && currentEpisode.position != null && currentEpisode.position != "" && currentEpisode.position != "NULL") {
                i.putExtra("position", Integer.decode(currentEpisode.position));
            }
            if (currentVideo != null && currentVideo.position != null && currentVideo.position != "" && currentVideo.position != "NULL") {
                i.putExtra("position", Integer.decode(currentVideo.position));
            }
            startActivityForResult(i, 111);
            /*
            //VLC
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("org.videolan.vlc.debug");
            intent.setDataAndType(Uri.parse(_url), "video/*");
            intent.putExtra("item_title", _fileName);
            intent.putExtra("subtitles_location", new Parcelable[]{Uri.parse(_subtitle)});
            startActivityForResult(intent, 111);
            */
            /*
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setPackage("com.okythoos.android.tdmpro");
            i.setDataAndType(Uri.parse(subtitle), "video/*");
            startActivity(i);

            i.setDataAndType(Uri.parse(url), "video/*");
            startActivityForResult(i, 111);
            */
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
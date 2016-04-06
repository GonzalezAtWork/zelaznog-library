package net.zelaznog.library;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;

import net.zelaznog.library.model.TVDB;
import net.zelaznog.library.model.Video;
import net.zelaznog.library.model.VideoCategory;
import net.zelaznog.library.model.VideoCollection;
import net.zelaznog.library.util.GridViewSeries;
import net.zelaznog.library.util.SeriesSQLite;

import org.json.XML;
import org.json.JSONObject;

public class SeriesActivity extends BaseActivity {

    public int layout = R.layout.activity_series;

    public VideoCategory support;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Zelaznog: Seriados");
        dbSeries = new SeriesSQLite(this);

        if(dbSeries.getAllSeries().size() > 0){
            updateView();
        }else{
            showMsg("Downloading data ...");
            cbObj = new CallBack() {
                @Override
                public void exec(String response) {
                    processJson(response);
                }
            };
            getJson(cloudAddress);
        }
    }

    @Override
    public void clickItem(VideoCategory serie){
        currentSerie = serie;
        setTitle(currentSerie.name);
        gridView.setColumnWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
        GridViewSeries gridAdapter = new GridViewSeries(this, R.layout.grid_item_seasons, dbSeries.getAllSeasons(serie));
        gridView.setAdapter(gridAdapter);
    }

    @Override
    public void clickItem(VideoCollection season){
        currentSeason = season;
        setTitle(currentSerie.name + " - " + currentSeason.name);
        gridView.setColumnWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1000, getResources().getDisplayMetrics()));
        GridViewSeries gridAdapter = new GridViewSeries(this, R.layout.grid_item_episodes, dbSeries.getAllEpisodes(season));
        gridView.setAdapter(gridAdapter);
    }

    @Override
    public void clickItem(Video video){
        currentEpisode = video;
        showMsg("Watching " + currentSerie.name, currentSeason.name + " " + currentEpisode.name + "\nBitrate: " + bitrate);
        cloudURL = video.link.replaceAll("&amp;","&") + "&bitrate=" + bitrate;
        cbObj = new CallBack() {
            @Override
            public void exec(String response) {
                playVideo(response);
            }
        };
        //getReal(cloudURL);
        getJson(cloudURL);
    }

    public void updateView(){
        setTitle("Zelaznog: Seriados");
        gridView.setColumnWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics()));
        GridViewSeries gridAdapter = new GridViewSeries(this, R.layout.grid_item_series, dbSeries.getAllSeries());
        gridView.setAdapter(gridAdapter);
    }

    public void buildDb(){
        showMsg("Updating Series Database...");
        Intent intent = new Intent(activity, SeriesActivity.class);
        startActivity(intent);
        finalizar();
    }

    @Override
    public void fixDataUsingAPI(){
        support = null;
        for ( VideoCategory checkSerie : dbmodel.Series){
            if(!checkSerie.fixed) {
                support = checkSerie;
            }
        }
        if(support != null) {
            getAPIJson();
        }else{
            buildDb();
        }
    }

    public void getAPIJson(){
        String query = support.name;
        showMsg("Crawling data ...", query);
        query = query.replace("."," ");
        query = query.replace(" ","%20");
        cloudURL = "http://thetvdb.com/api/GetSeries.php?language=pt&seriesname=" + query;
        cbObj = new CallBack() {
            @Override
            public void exec(String response) {
                cont(response);
            }
        };
        getJson(cloudURL);
    }

    public void cont(String response){
        String jsonPrettyPrintString = "";
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(response);
            jsonPrettyPrintString = xmlJSONObj.toString(4);
        } catch (Exception je) {
            Log.e("zelaznog",je.toString());
        }
        jsonPrettyPrintString = jsonPrettyPrintString.replace("\n","");
        jsonPrettyPrintString = jsonPrettyPrintString.replace("  "," ");
        jsonPrettyPrintString = jsonPrettyPrintString.replace("  "," ");
        jsonPrettyPrintString = jsonPrettyPrintString.replace("  "," ");
        jsonPrettyPrintString = jsonPrettyPrintString.replace("} ","}");
        jsonPrettyPrintString = jsonPrettyPrintString.replace("\"Series\": {","\"Series\": [{");
        jsonPrettyPrintString = jsonPrettyPrintString.replace("}}}","}]}}");
        try {
            TVDB TVDBModel = gson.fromJson(jsonPrettyPrintString, TVDB.class);
            support.name = TVDBModel.Data.Series.get(0).SeriesName;
            support.year = TVDBModel.Data.Series.get(0).FirstAired;
            support.network = TVDBModel.Data.Series.get(0).Network;
            support.overview = TVDBModel.Data.Series.get(0).Overview;
            support.original_title = TVDBModel.Data.Series.get(0).SeriesName;
            String img = TVDBModel.Data.Series.get(0).banner;
            if(img == "null"){
                //Log.v("zelaznog",jsonPrettyPrintString);
            }
            //support.image_url = "http://thetvdb.com/banners/"+ img;
            support.imdbId = TVDBModel.Data.Series.get(0).IMDB_ID;
            //support.tvdbId = TVDBModel.Data.Series.get(0).seriesid;
        }catch(Exception e){
            //Log.v("zelaznog",jsonPrettyPrintString);
            Log.e("Zelaznog",e.toString());
        }
        support.fixed = true;
        new Thread(new Runnable()
        {
            public void run() {
                //Log.v("zelaznog","Testando:" + support.name);
                dbSeries.addSerie(support);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.fixDataUsingAPI();
                    }
                });
            }
        }).start();
    }
}



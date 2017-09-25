package net.zelaznog.library;

import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;

import net.zelaznog.library.model.TVDB;
import net.zelaznog.library.model.Video;
import net.zelaznog.library.model.VideoCategory;
import net.zelaznog.library.model.VideoCollection;
import net.zelaznog.library.util.GridViewSeries;
import net.zelaznog.library.util.SeriesSQLite;

import org.json.XML;
import org.json.JSONObject;

import java.util.ArrayList;

public class SeriesActivity extends BaseActivity {

    public int layout = R.layout.activity_series;

    public VideoCategory support;

    @Override
    public void loadHistory(String query, Menu menu) {
        String[] columns = new String[] { "_id", "text"};
        MatrixCursor cursor = new MatrixCursor(columns);
        ArrayList<VideoCategory> series = dbSeries.getAllSeries();
        for(int i = 0; i < series.size(); i++) {
            String label = series.get(i).toString();
            if(label.toLowerCase().indexOf(query.toLowerCase()) > 0) {
                cursor.addRow( new Object[] { i, label } );
            }
        }
        final SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        SearchAdapter adapter = new SearchAdapter(this, cursor, series);
        search.setSuggestionsAdapter(adapter);
    }

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
        dbSeries.removeLinks(dbmodel.Series);
        ringProgressDialog.dismiss();
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

    public int counter = 0;
    public void getAPIJson(){
        counter++;
        String query = support.name;
        showMsg("Crawling: "+ counter +"/"+ dbmodel.Series.size() +" Series", query);
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



package net.zelaznog.library;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import net.zelaznog.library.model.TMDB;
import net.zelaznog.library.model.Video;
import net.zelaznog.library.util.GridViewMovies;
import net.zelaznog.library.util.MoviesSQLite;

public class MovieActivity extends BaseActivity {

    public int layout = R.layout.activity_filmes;

    public Video support;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Zelaznog: Filmes");
        dbMovies = new MoviesSQLite(this);

        if(dbMovies.getAllMovies().size() > 0){
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
    public void clickItem(Video video){
        currentVideo = video;
        cloudURL = video.link.replaceAll("&amp;","&") + "&bitrate=" + bitrate;
        cbObj = new CallBack() {
            @Override
            public void exec(String response) {
                playVideo(response);
            }
        };
        showMsg("Watching", currentVideo.name + "\nBitrate: " + bitrate);
        getJson(cloudURL);
    }

    @Override
    public void updateView(){
        setTitle("Zelaznog: Filmes");
        gridAdapter = new GridViewMovies(this, R.layout.grid_item_layout, dbMovies.getAllMovies());
        gridView.setAdapter(gridAdapter);
    }

    public void buildDb(){
        showMsg("Updating Movies Database...");
        ringProgressDialog.dismiss();
        Intent intent = new Intent(this, MovieActivity.class);
        startActivity(intent);
        finalizar();
    }

    @Override
    public void fixDataUsingAPI(){
        support = null;
        for (Video checkVideo : dbmodel.Filmes){
            if(!checkVideo.fixed) {
                support = checkVideo;
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
        for (int i = 1900; i <= 2020; i++){
            if(query.indexOf(String.valueOf(i)) > 0) {
                query = query.split(String.valueOf(i))[0] + "&year=" + String.valueOf(i);
            }
        }
        query = query.split("1080")[0];
        query = query.split("720")[0];
        query = query.split("EXTENDED")[0];
        query = query.split("hdtv")[0];
        query = query.split("brrip")[0];
        query = query.split("webrip")[0];
        query = query.replace("."," ");
        query = query.replace(" ","%20");
        cloudURL = "http://api.themoviedb.org/3/search/movie?api_key=a5cd3d9ce62e9f2052446b681dac6943&language=pt&query=" + query;
        cbObj = new CallBack() {
            @Override
            public void exec(String response) {
                cont(response);
            }
        };
        getJson(cloudURL);
    }
    public void cont(String response){
        try {
            TMDB TMDBModel = gson.fromJson(response, TMDB.class);
            support.name = TMDBModel.results.get(0).title;
            support.original_title = TMDBModel.results.get(0).original_title;
            support.image_url = "https://image.tmdb.org/t/p/w185" + TMDBModel.results.get(0).poster_path;
            support.year = TMDBModel.results.get(0).release_date;
            showMsg("Crawling data ...", support.name);
        }catch(Exception e){
            Log.e("Zelaznog","Error5: " + e.toString());
        }
        support.fixed = true;
        new Thread(new Runnable()
        {
            public void run() {
                dbMovies.addVideo(support);
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



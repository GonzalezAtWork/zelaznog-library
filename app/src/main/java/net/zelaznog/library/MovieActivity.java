package net.zelaznog.library;

import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;

import net.zelaznog.library.model.TMDB;
import net.zelaznog.library.model.TMDBItem;
import net.zelaznog.library.model.Video;
import net.zelaznog.library.util.GridViewMovies;
import net.zelaznog.library.util.MoviesSQLite;

import java.util.ArrayList;

public class MovieActivity extends BaseActivity {

    public int layout = R.layout.activity_filmes;

    public Video support;

    @Override
    public void loadHistory(String query, Menu menu) {
        String[] columns = new String[] { "_id", "text"};
        MatrixCursor cursor = new MatrixCursor(columns);
        ArrayList<Video> movies = dbMovies.getAllMovies();
        for(int i = 0; i < movies.size(); i++) {
            String label = movies.get(i).toString();
            if(label.toLowerCase().indexOf(query.toLowerCase()) > 0) {
                cursor.addRow( new Object[] { i, label } );
            }
        }
        final SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        SearchAdapter adapter = new SearchAdapter(this, cursor, movies);
        search.setSuggestionsAdapter(adapter);
    }

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
        strMsg = currentVideo.name;
        showMsg("Watching", strMsg);
        //getReal(cloudURL);
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
        dbMovies.removeLinks(dbmodel.Filmes);
        ringProgressDialog.dismiss();
        Intent intent = new Intent(this, MovieActivity.class);
        startActivity(intent);
        finalizar();
    }
    @Override
    public void fixDataUsingAPI(){
        support = null;
        for (Video checkVideo : dbmodel.Filmes){
            if(!checkVideo.fixed && !dbMovies.checkLink(checkVideo.link) ) {
                support = checkVideo;
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
        showMsg("Crawling: "+ counter +"/"+ dbmodel.Filmes.size() +" Filmes", query);
        if(support.themoviedb_id != null && support.themoviedb_id != ""){
            cloudURL = "https://api.themoviedb.org/3/movie/"+ support.themoviedb_id +"?api_key=a5cd3d9ce62e9f2052446b681dac6943&language=pt-BR";
        }else {
            query = query.toLowerCase();
            query = query.split("1080")[0];
            query = query.split("720")[0];
            query = query.split("10bit")[0];
            query = query.split("EXTENDED")[0];
            query = query.split("hdtv")[0];
            query = query.split("brrip")[0];
            query = query.split("webrip")[0];
            query = query.split("web-dl")[0];
            query = query.split("dvdrip")[0];
            query = query.split("hd")[0];
            query = query.split("dvd")[0];
            for (int i = 1900; i <= 2020; i++){
                if(query.indexOf(String.valueOf(i)) > 0) {
                    query = query.split(String.valueOf(i))[0] + "&year=" + String.valueOf(i);
                }
            }
            query = query.replace("."," ");
            query = query.replace(" ","%20");
            cloudURL = "http://api.themoviedb.org/3/search/movie?api_key=a5cd3d9ce62e9f2052446b681dac6943&language=pt-BR&query=" + query;
        }
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
            TMDB TMDBresults = gson.fromJson(response, TMDB.class);
            TMDBItem item;
            if(TMDBresults.results == null) {
                item = gson.fromJson(response, TMDBItem.class);
            }else{
                item = TMDBresults.results.get(0);
            }
            support.name = item.title;
            support.original_title = item.original_title;
            support.image_url = "https://image.tmdb.org/t/p/w185" + item.poster_path;
            support.year = item.release_date;
            support.overview = item.overview;
            showMsg("Crawling: "+ counter +"/"+ dbmodel.Filmes.size() +" Filmes", support.name);
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



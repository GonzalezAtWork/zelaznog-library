package net.zelaznog.library.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.zelaznog.library.R;
import net.zelaznog.library.model.Video;
import net.zelaznog.library.model.VideoCategory;
import net.zelaznog.library.model.VideoCollection;

import java.util.ArrayList;

public class GridViewSeries extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();
    private ImageLoader imgLoader;
    public GridViewSeries(Context context, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        imgLoader = new ImageLoader(context.getApplicationContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        String holderTitle = "";
        String holderImage = "";

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Object item = data.get(position);
        if(item instanceof VideoCategory) {
            holderTitle = ((VideoCategory) item).name;
            holderImage = ((VideoCategory) item).image_url;
        }
        if(item instanceof VideoCollection) {
            holderTitle = ((VideoCollection) item).name;
            holderImage = ((VideoCollection) item).image_url;
        }
        if(item instanceof Video) {
            Video ble = (Video)item;
            String title = ble.name;
            if(ble.position != null && ble.position != "" && ble.position != "NULL"){
                try {
                    int percent = 100;
                    if(Integer.parseInt(ble.duration) > 0) {
                        percent = Integer.parseInt(ble.position) * 100 / Integer.parseInt(ble.duration);
                    }
                    title += " (" + String.valueOf(percent) + "%)";
                }catch(Exception e){
                    Log.e("zelaznog", e.toString());
                }
            }
            holderTitle = title;
            holderImage = ((Video) item).image_url;
        }
        holder.imageTitle.setText(holderTitle);
        imgLoader.DisplayImage(holderImage, holder.image);
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}

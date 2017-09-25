package net.zelaznog.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zelaznog.library.model.Video;
import net.zelaznog.library.model.VideoCategory;

import java.util.ArrayList;

/**
 * Created by Decaedro on 25/09/2017.
 */

public class SearchAdapter extends CursorAdapter {
    private ArrayList<?> items;
    private Object item;
    BaseActivity activity;
    public SearchAdapter(Context context, Cursor cursor, ArrayList<?> items) {
        super(context, cursor, false);
        activity = (BaseActivity) context;
        this.items = items;
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView strCursor = (TextView) view.findViewById(R.id.cursor);
        TextView strText = (TextView) view.findViewById(R.id.text);
        Object obj = items.get(cursor.getInt(0));
        String label = obj.toString();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txtCursor = (TextView) view.findViewById(R.id.cursor);
                String strCursor = String.valueOf( txtCursor.getText() );
                int intCursor = Integer.parseInt(strCursor);
                Object obj = items.get(intCursor);
                if(obj instanceof Video){
                    activity.clickItem((Video) obj);
                }
                if(obj instanceof VideoCategory){
                    activity.clickItem((VideoCategory) obj);
                }
            }
        });
        strText.setText(label);
        strCursor.setText(String.valueOf(cursor.getInt(0)));
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item, parent, false);
        return view;
    }
}
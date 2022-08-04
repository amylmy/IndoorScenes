package cn.edu.whu.indoorscene.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Mengyun on 2016/12/14.
 * 功能：动态添加修改场景类别
 */

public class SceneAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private int layoutResourceId;
    private String[] data = null;

    public SceneAdapter(Context context, int layoutResourceId, String[] data) {
        super(context, layoutResourceId);
        this.mContext = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SceneHolder holder = null;
        if(row == null) {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new SceneHolder();
            row.setTag(holder);
        } else {
            holder = (SceneHolder)row.getTag();
        }
        String scene = data[position];
        holder.sceneLabel.setText(scene);
        return row;
    }

    static class SceneHolder {
        TextView sceneLabel;
    }
}

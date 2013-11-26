package com.gopivotal.pushlib;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class LogAdapter extends BaseAdapter {

    private final List<Pair<String, String>> messages;
    private final LayoutInflater inflater;
    private int[] rowColours = new int[]{0xffdddeff, 0xffaabbdd};

    public LogAdapter(Context context, List<Pair<String, String>> messages) {
        this.messages = messages;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        final Pair<String, String> pair = (Pair<String, String>) getItem(position);
        viewHolder.timestampView.setText(pair.first);
        viewHolder.messageView.setText(pair.second);
        convertView.setBackgroundColor(rowColours[position % rowColours.length]);
        return convertView;
    }

    private static class ViewHolder {

        public TextView timestampView;
        public TextView messageView;

        public ViewHolder(View v) {
            timestampView = (TextView) v.findViewById(R.id.textview_timestamp);
            messageView = (TextView) v.findViewById(R.id.textview_message);
        }
    }
}

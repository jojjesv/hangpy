package com.hangpy.hangpy.events.filters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hangpy.hangpy.R;

/**
 * Default list adapter for event search filters.
 */
public class DefaultFilterAdapter extends ArrayAdapter<DefaultFilterAdapter.FilterAdapterData> {

    //  Cached views used in getView()
    private static TextView buttonText;
    private static ImageView buttonIcon;

    public DefaultFilterAdapter(Context context) {
        super(context, 0, new FilterAdapterData[]{
                new FilterAdapterData(context, R.string.filter_distance, R.mipmap.ic_launcher),
                new FilterAdapterData(context, R.string.filter_location, R.mipmap.ic_launcher),
                new FilterAdapterData(context, R.string.filter_time, R.mipmap.ic_launcher),
                new FilterAdapterData(context, R.string.filter_attendance_count, R.mipmap.ic_launcher)
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            //  First call
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.filter_button, parent, false);
            buttonText = (TextView)convertView.findViewById(android.R.id.text1);
            buttonIcon = (ImageView)convertView.findViewById(android.R.id.icon);
        }

        FilterAdapterData dataAtPosition = getItem(position);
        buttonText.setText(dataAtPosition.text);
        buttonIcon.setImageBitmap(dataAtPosition.icon);

        return convertView;
    }

    /**
     * Data per filter item.
     */
    public static class FilterAdapterData {
        private String text;
        private Bitmap icon;

        public FilterAdapterData(Context context, int textResId, int iconResId) {
            Resources res = context.getResources();
            this.text = res.getString(textResId);
            this.icon = BitmapFactory.decodeResource(res, iconResId);
        }
    }
}

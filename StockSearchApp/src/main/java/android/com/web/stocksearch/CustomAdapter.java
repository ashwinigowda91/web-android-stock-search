package android.com.web.stocksearch;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.StreamHandler;

/**
 * Created by ashwini on 11-04-2016.
 */
public class CustomAdapter extends ArrayAdapter<HashMap<String, String>> {
    Context context;
    int resource, textViewResourceId;
    List<HashMap<String, String>> rList;

    public CustomAdapter(Context context, int resource, int textViewResourceId, List<HashMap<String, String>> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.resource = resource;
        this.textViewResourceId = textViewResourceId;
        this.rList = objects;
    }

    @Override
    public int getCount() {
        return rList.size();
    }

    @Override
    public HashMap<String,String> getItem(int position) {
        return rList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        View view = convertView;
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_view, parent, false);
        }
        HashMap<String, String> hMap = rList.get(position);
        if(hMap != null)
        {
            TextView uLabel = (TextView) view.findViewById(R.id.list_item_1);
            if(uLabel != null)
            {
                uLabel.setText(hMap.get("Symbol"));
                uLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.f);
            }
            TextView lLabel = (TextView) view.findViewById(R.id.list_item_2);
            if(lLabel != null)
            {
                lLabel.setText(hMap.get("Exchange"));
            }
        }
        return view;
    }
}

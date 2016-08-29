package android.com.web.stocksearch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ashwini on 12-04-2016.
 */
public class CustomStockDetailsAdapter extends ArrayAdapter<HashMap<String, String>> {

    Context context;
    int resource;
    List<HashMap<String, String>> rList = new ArrayList<HashMap<String, String>>();

    public CustomStockDetailsAdapter(Context context, int resource, List<HashMap<String, String>> objects)
    {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.rList = objects;
    }



    @Override
    public int getCount() {
        return rList.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    @Override
    public HashMap<String,String> getItem(int position) {
        return rList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        String name = "";
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.stock_row_layout, parent, false);
        }
        HashMap<String, String> hMap = getItem(position);
        if(hMap != null)
        {
            if(hMap.containsKey("Color") && hMap.get("Name") == "CHANGE")
            {
                ImageView iLabel = (ImageView) view.findViewById(R.id.imgview);
                iLabel.setVisibility(View.VISIBLE);
                String colorVal = hMap.get("Color");
                boolean just = false;
                if(colorVal == null || colorVal == "" || colorVal.isEmpty())
                {
                    just = true;
                }
                else
                {
                    String colName = hMap.get("Color");
                    if(colName.equalsIgnoreCase("RED"))
                    {
                        iLabel.setImageResource(R.drawable.down);
                    }
                    else if(colName.equalsIgnoreCase("GREEN"))
                    {
                        iLabel.setImageResource(R.drawable.up);
                    }
                }
                TextView uLabel = (TextView) view.findViewById(R.id.text_view1);
                if(uLabel != null)
                {
                    name = hMap.get("Name");
                    uLabel.setText(name);
                }

                TextView lLabel = (TextView) view.findViewById(R.id.text_view2);
                if(lLabel != null)
                {
                    lLabel.setText(hMap.get("Value"));
                }
            }
            else if(hMap.containsKey("Color") && hMap.get("Name") == "CHANGEPERCENT")
            {
                ImageView iLabel = (ImageView) view.findViewById(R.id.imgview);
                iLabel.setVisibility(View.VISIBLE);
                String colorVal = hMap.get("Color");
                boolean justLikeThat = false;
                if(colorVal == null || colorVal == "" || colorVal.isEmpty())
                {
                    justLikeThat = true;
                }
                else
                {
                    String colName = hMap.get("Color");
                    if(colName.equalsIgnoreCase("RED"))
                    {
                        iLabel.setImageResource(R.drawable.down);
                    }
                    else if(colName.equalsIgnoreCase("GREEN"))
                    {
                        iLabel.setImageResource(R.drawable.up);
                    }
                }
                TextView uLabel = (TextView) view.findViewById(R.id.text_view1);
                if(uLabel != null)
                {
                    name = hMap.get("Name");
                    uLabel.setText(name);
                }

                TextView lLabel = (TextView) view.findViewById(R.id.text_view2);
                if(lLabel != null)
                {
                    lLabel.setText(hMap.get("Value"));
                }
            }
            else
            {
                ImageView iLabel = (ImageView) view.findViewById(R.id.imgview);
                iLabel.setVisibility(View.INVISIBLE);
                TextView uLabel = (TextView) view.findViewById(R.id.text_view1);
                if(uLabel != null)
                {
                    name = hMap.get("Name");
                    uLabel.setText(name);
                }

                TextView lLabel = (TextView) view.findViewById(R.id.text_view2);
                if(lLabel != null)
                {
                    lLabel.setText(hMap.get("Value"));
                }
            }
        }
        return view;
    }
}

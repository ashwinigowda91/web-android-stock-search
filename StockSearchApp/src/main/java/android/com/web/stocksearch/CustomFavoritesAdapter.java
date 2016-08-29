package android.com.web.stocksearch;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ashwini on 02-05-2016.
 */
public class CustomFavoritesAdapter extends ArrayAdapter<HashMap<String, String>> {

    Context context;
    int resource;
    List<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();

    public CustomFavoritesAdapter(Context context, int resource, List<HashMap<String, String>> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.resultList = objects;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public HashMap<String,String> getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.favlist_layout, parent, false);
        }
        HashMap<String, String> hMap = getItem(position);
        if(hMap != null)
        {
            TextView symLabel = (TextView) convertView.findViewById(R.id.symbolName);
            symLabel.setText(hMap.get("SYMBOL"));

            TextView priceLabel = (TextView) convertView.findViewById(R.id.lastPrice);
            String lastPrice = hMap.get("PRICE");
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            lastPrice = decimalFormat.format(Double.parseDouble(lastPrice));
            priceLabel.setText("$ "+lastPrice);

            TextView changeLabel = (TextView) convertView.findViewById(R.id.change);
            String change = hMap.get("CHANGE");
            change = decimalFormat.format(Double.parseDouble(change));
            if(Double.parseDouble(change) < 0)
            {
                changeLabel.setText(change+"%");
                changeLabel.setBackgroundColor(Color.RED);
            }
            else if(Double.parseDouble(change) > 0)
            {
                changeLabel.setText("+"+change+"%");
                changeLabel.setBackgroundColor(Color.GREEN);
            }
            else
            {
                changeLabel.setText(change+"%");
            }

            TextView nameLabel = (TextView) convertView.findViewById(R.id.cname);
            nameLabel.setText(hMap.get("NAME"));

            TextView marketLabel = (TextView) convertView.findViewById(R.id.marketCap);
            String mCap = hMap.get("MARKET");
            double mCapital = Double.parseDouble(mCap);
            mCapital = mCapital/1000000000;
            if(mCapital < 1)
            {
                mCapital = mCapital * 1000;
                if(mCapital < 1)
                {
                    mCap = "Market Cap: "+decimalFormat.format(Double.parseDouble(mCap))+"";
                }
                else
                {
                    mCap = Double.toString(mCapital);
                    mCap = "Market Cap: "+decimalFormat.format(Double.parseDouble(mCap))+" Million";
                }
            }
            else
            {
                mCap = Double.toString(mCapital);
                mCap = "Market Cap: "+decimalFormat.format(Double.parseDouble(mCap))+" Billion";
            }
            marketLabel.setText(mCap);

            TextView rightLabel = (TextView) convertView.findViewById(R.id.right);
            if(rightLabel != null)
            {
                rightLabel.setText(" ");
            }
        }
        else
        {
            TextView symLabel = (TextView) convertView.findViewById(R.id.symbolName);
            symLabel.setText("");

            TextView priceLabel = (TextView) convertView.findViewById(R.id.lastPrice);
            priceLabel.setText("");

            TextView changeLabel = (TextView) convertView.findViewById(R.id.change);
            changeLabel.setText("");

            TextView nameLabel = (TextView) convertView.findViewById(R.id.cname);
            nameLabel.setText("");

            TextView marketLabel = (TextView) convertView.findViewById(R.id.marketCap);
            marketLabel.setText("");

            TextView rightLabel = (TextView) convertView.findViewById(R.id.right);
            rightLabel.setText(" ");
        }
        return convertView;
    }
}

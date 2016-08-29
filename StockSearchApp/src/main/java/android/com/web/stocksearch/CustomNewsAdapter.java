package android.com.web.stocksearch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ashwini on 14-04-2016.
 */
public class CustomNewsAdapter extends ArrayAdapter<HashMap<String, String>> {

    Context context;
    int resource;
    List<HashMap<String, String>> newsList = new ArrayList<HashMap<String, String>>();

    public CustomNewsAdapter(Context context, int resource, List<HashMap<String, String>> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.newsList = objects;
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    @Override
    public HashMap<String,String> getItem(int position) {
        return newsList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        String name = "";
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.news_row_layout, parent, false);
            HashMap<String, String> hMap = getItem(position);
            TextView textView1 = (TextView) view.findViewById(R.id.news_view1);
            TextView textView2 = (TextView) view.findViewById(R.id.news_view2);
            TextView textView3 = (TextView) view.findViewById(R.id.news_view3);
            TextView textView4 = (TextView) view.findViewById(R.id.news_view4);
            if(!hMap.isEmpty())
            {
                if(textView1 != null)
                {
                    String title = hMap.get("Title");
                    final String url = hMap.get("URL");
                    textView1.setLinkTextColor(Color.BLACK);
                    textView1.setText(Html.fromHtml(title));
                    textView1.setPaintFlags(textView1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    final Uri test = Uri.parse(url);
                    final Intent launchBrowser = new Intent(Intent.ACTION_VIEW,test);
                    launchBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    textView1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            context.startActivity(launchBrowser);
                        }
                    });

                /*textView1.setText(Html.fromHtml(urlTitle),TextView.BufferType.SPANNABLE);
                textView1.setMovementMethod(LinkMovementMethod.getInstance());
                textView1.setLinkTextColor(Color.BLACK);*/
                }
                if(textView2 != null)
                {
                    textView2.setText(Html.fromHtml(hMap.get("Content")).toString());
                }
                if(textView3 != null)
                {
                    textView3.setText("Publisher: "+hMap.get("Publisher"));
                }
                if(textView4 != null)
                {
                    textView4.setText("Date: "+hMap.get("PublishDate"));
                }
            }
            else
            {

            }

        }
        return view;
    }
}

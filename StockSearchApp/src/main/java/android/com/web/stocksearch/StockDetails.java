package android.com.web.stocksearch;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.com.web.stocksearch.R.layout.fragment_stock_details;


/**
 * A simple {@link Fragment} subclass.
 */
public class StockDetails extends Fragment {
    public String symbol = "";
    ImageView imageView;
    String chartURL = "";

    public StockDetails(){
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        List<HashMap<String,String>> responseList = new ArrayList<HashMap<String, String>>();

        // Inflate the layout for this fragment
        View view = inflater.inflate(fragment_stock_details, container, false);
        ListView listView = (ListView) view.findViewById(R.id.listview);

        //include footer
        View footer = inflater.inflate(R.layout.footer, listView, false);
        imageView = (ImageView) footer.findViewById(R.id.yahooimg);
        listView.addFooterView(footer, null, false);

        //include header
        View header = inflater.inflate(R.layout.header, listView, false);
        listView.addHeaderView(header, null, false);


        try {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String response = getArguments().getString("StockResults");
            JSONObject jsonObject = new JSONObject(response);
            symbol = jsonObject.getString("Symbol");
            chartURL = "http://chart.finance.yahoo.com/t?s="+symbol+"&lang=en-US&width=400&height=300";
            new AsyncTaskRunner().execute();

            String colorChange = "";
            String change = jsonObject.getString("Change");
            String chPercent = jsonObject.getString("ChangePercent");
            change = decimalFormat.format(Double.parseDouble(change));
            chPercent = decimalFormat.format(Double.parseDouble(chPercent));
            if(Double.parseDouble(change) < 0 || Double.parseDouble(chPercent) < 0)
            {
                colorChange = "RED";
            }
            else if(Double.parseDouble(change) > 0 && Double.parseDouble(chPercent) > 0)
            {
                colorChange = "GREEN";
            }
            else if(Double.parseDouble(change) == 0 || Double.parseDouble(chPercent) == 0)
            {
                if(Double.parseDouble(change) == 0)
                {
                    if(Double.parseDouble(chPercent) < 0)
                    {
                        colorChange = "RED";
                    }
                    else if(Double.parseDouble(chPercent) > 0)
                    {
                        colorChange = "GREEN";
                    }
                    else if(Double.parseDouble(chPercent) == 0)
                    {
                        colorChange = "";
                    }
                }
                else if(Double.parseDouble(chPercent) == 0)
                {
                    if(Double.parseDouble(change) < 0)
                    {
                        colorChange = "RED";
                    }
                    else if(Double.parseDouble(change) > 0)
                    {
                        colorChange = "GREEN";
                    }
                    else if(Double.parseDouble(change) == 0)
                    {
                        colorChange = "";
                    }
                }
            }
            String chFinal = "";
            if(Double.parseDouble(chPercent) > 0)
            {
                chFinal  = change+" (+"+chPercent+"%)";
            }
            else
            {
                chFinal  = change+" ("+chPercent+"%)";
            }

            String colorChYTD = "";
            String chYTD = jsonObject.getString("ChangeYTD");
            String chPerYTD = jsonObject.getString("ChangePercentYTD");
            chYTD = decimalFormat.format(Double.parseDouble(chYTD));
            chPerYTD = decimalFormat.format(Double.parseDouble(chPerYTD));
            if(Double.parseDouble(chYTD) < 0 || Double.parseDouble(chPerYTD) < 0)
            {
                colorChYTD = "RED";
            }
            else if(Double.parseDouble(chYTD) > 0 && Double.parseDouble(chPerYTD) > 0)
            {
                colorChYTD = "GREEN";
            }
            else if(Double.parseDouble(chYTD) == 0 || Double.parseDouble(chPerYTD) == 0)
            {
                if(Double.parseDouble(chYTD) == 0)
                {
                    if(Double.parseDouble(chPerYTD) < 0)
                    {
                        colorChYTD = "RED";
                    }
                    else if(Double.parseDouble(chPerYTD) > 0)
                    {
                        colorChYTD = "GREEN";
                    }
                    else if(Double.parseDouble(chPerYTD) == 0)
                    {
                        colorChYTD = "";
                    }
                }
                else if(Double.parseDouble(chPerYTD) == 0)
                {
                    if(Double.parseDouble(chYTD) < 0)
                    {
                        colorChYTD = "RED";
                    }
                    else if(Double.parseDouble(chYTD) > 0)
                    {
                        colorChYTD = "GREEN";
                    }
                    else if(Double.parseDouble(chYTD) == 0)
                    {
                        colorChYTD = "";
                    }
                }
            }

            String chYTDFinal = "";
            if(Double.parseDouble(chPerYTD) > 0)
            {
                chYTDFinal = chYTD+" (+"+chPerYTD+"%)";
            }
            else
            {
                chYTDFinal = chYTD+" ("+chPerYTD+"%)";
            }


            String mCap = jsonObject.getString("MarketCap");
            double mCapital = Double.parseDouble(mCap);
            mCapital = mCapital/1000000000;
            if(mCapital < 1)
            {
                mCapital = mCapital * 1000;
                if(mCapital < 1)
                {
                    mCap = decimalFormat.format(Double.parseDouble(mCap))+"";
                }
                else
                {
                    mCap = Double.toString(mCapital);
                    mCap = decimalFormat.format(Double.parseDouble(mCap))+" Million";
                }
            }
            else
            {
                mCap = Double.toString(mCapital);
                mCap = decimalFormat.format(Double.parseDouble(mCap))+" Billion";
            }

            String stockPrice = jsonObject.getString("LastPrice");
            stockPrice = decimalFormat.format(Double.parseDouble(stockPrice));

            String high = jsonObject.getString("High");
            high = decimalFormat.format(Double.parseDouble(high));

            String low = jsonObject.getString("Low");
            low = decimalFormat.format(Double.parseDouble(low));

            String open = jsonObject.getString("Open");
            open = decimalFormat.format(Double.parseDouble(open));

            String timestamp = jsonObject.getString("Timestamp");
            try {
                SimpleDateFormat sourceDateFormat = new SimpleDateFormat("E MMM d HH:mm:ss 'UTC'Z yyyy");
                Date sourceDate = sourceDateFormat.parse(timestamp);

                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss a");
                timestamp = formatter.format(sourceDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            HashMap<String, String> hashMap1 = new HashMap<String, String>();
            HashMap<String, String> hashMap2 = new HashMap<String, String>();
            HashMap<String, String> hashMap3 = new HashMap<String, String>();
            HashMap<String, String> hashMap4 = new HashMap<String, String>();
            HashMap<String, String> hashMap5 = new HashMap<String, String>();
            HashMap<String, String> hashMap6 = new HashMap<String, String>();
            HashMap<String, String> hashMap7 = new HashMap<String, String>();
            HashMap<String, String> hashMap8 = new HashMap<String, String>();
            HashMap<String, String> hashMap9 = new HashMap<String, String>();
            HashMap<String, String> hashMap10 = new HashMap<String, String>();
            HashMap<String, String> hashMap11 = new HashMap<String, String>();

            hashMap1.put("Name","SYMBOL");
            hashMap1.put("Value",jsonObject.getString("Symbol"));

            hashMap2.put("Name","NAME");
            hashMap2.put("Value",jsonObject.getString("Name"));

            hashMap3.put("Name","LASTPRICE");
            hashMap3.put("Value","$"+stockPrice);

            hashMap4.put("Name","CHANGE");
            hashMap4.put("Value",chFinal);
            hashMap4.put("Color",colorChange);

            hashMap5.put("Name","CHANGEPERCENT");
            hashMap5.put("Value",chYTDFinal);
            hashMap5.put("Color",colorChYTD);

            hashMap6.put("Name","MARKETCAP");
            hashMap6.put("Value", mCap);

            hashMap7.put("Name","TIMESTAMP");
            hashMap7.put("Value",timestamp);

            hashMap8.put("Name","HIGH");
            hashMap8.put("Value","$"+high);

            hashMap9.put("Name","LOW");
            hashMap9.put("Value","$"+low);

            hashMap10.put("Name","OPEN");
            hashMap10.put("Value","$"+open);

            hashMap11.put("Name","VOLUME");
            hashMap11.put("Value",jsonObject.getString("Volume"));

            responseList.add(hashMap1);
            responseList.add(hashMap2);
            responseList.add(hashMap3);
            responseList.add(hashMap4);
            responseList.add(hashMap5);
            responseList.add(hashMap6);
            responseList.add(hashMap7);
            responseList.add(hashMap8);
            responseList.add(hashMap9);
            responseList.add(hashMap10);
            responseList.add(hashMap11);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog imageDialog = new Dialog(getContext());
                    imageDialog.getWindow().setLayout(400,300);
                    imageDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    WebView webView = new WebView(getContext());
                    webView.setLayoutParams(new ViewGroup.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT));
                    webView.loadUrl(chartURL);
                    webView.getSettings().setBuiltInZoomControls(true);
                    webView.getSettings().setDisplayZoomControls(false);
                    webView.getSettings().setLoadWithOverviewMode(true);
                    webView.getSettings().setUseWideViewPort(true);
                    webView.getSettings().setSupportZoom(true);
                    imageDialog.setCancelable(true);
                    imageDialog.setContentView(webView);
                    imageDialog.show();
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }



        listView.setAdapter(new CustomStockDetailsAdapter(getActivity().getApplicationContext(), R.layout.fragment_stock_details, responseList));
        return view;
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        InputStream stream = null;
        Bitmap bitmap;
        String yahooURL = "http://chart.finance.yahoo.com/t?s="+symbol+"&lang=en-US&width=1000&height=400";

        @Override
        protected Void doInBackground(Void... params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(yahooURL);
                HttpResponse resp = httpclient.execute(httpget);
                HttpEntity entity = resp.getEntity();
                stream = entity.getContent();
                bitmap = BitmapFactory.decodeStream(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            imageView.setImageBitmap(bitmap);
        }

    }
}

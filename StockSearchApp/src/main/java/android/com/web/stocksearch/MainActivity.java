package android.com.web.stocksearch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.server.converter.StringToIntConverter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    AutoCompleteTextView actv;
    public final static String JSINTENT = "com.webtech.stocksearch.RESULT";
    public final static String SYMBOLNAME = "com.webtech.stocksearch.SYM";
    public final static String NEWS = "com.webtech.stocksearch.GOOGLE";
    public final static String COMPANY = "com.webtech.stocksearch.COMPANY";
    public final static String PRICE = "com.webtech.stocksearch.PRICE";
    TimerTask timerTask;

    List<HashMap<String,String>> responseList = new ArrayList<HashMap<String, String>>();
    List<HashMap<String,String>> respList = new ArrayList<HashMap<String, String>>();
    List<HashMap<String,String>> googleList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> refreshResults = new HashMap<String, String>();
    AsyncTaskRunner asyncTaskRunner;
    boolean async = true;
    String resultQuote = "";
    String getQuoteStatus = "";
    ProgressBar progressBar;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //creating a view for autocomplete
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewInput);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        actv.setThreshold(3);
        actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(actv.isPerformingCompletion()){
                    return;
                }
                else if(actv.getText().toString().length() >= 3 && async == true) {
                    asyncTaskRunner = new AsyncTaskRunner();
                    asyncTaskRunner.execute();
                }
            }
        });

        final SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCES",0);
        //add listener on buttons
        addListenerOnButton();

        //Favorite List Display
        Map<String, ?> map = sharedPreferences.getAll();
        Set<String> keyStrings = map.keySet();
        Iterator iterator = keyStrings.iterator();
        while(iterator.hasNext())
        {
            Object tempKey = iterator.next();
            String tempResults = map.get(tempKey).toString();
            try
            {
                JSONObject json = new JSONObject(tempResults);
                String symbol = json.getString("Symbol");
                String company = json.getString("Name");
                String price = json.getString("LastPrice");
                String mCap = json.getString("MarketCap");
                String changePercent = json.getString("ChangePercent");

                //send to favorite adapter
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("SYMBOL",symbol);
                hashMap.put("NAME",company);
                hashMap.put("PRICE",price);
                hashMap.put("MARKET",mCap);
                hashMap.put("CHANGE",changePercent);
                respList.add(hashMap);

            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        }

        ListView listView = (ListView) findViewById(R.id.favlist);
        final CustomFavoritesAdapter customFavoritesAdapter = new CustomFavoritesAdapter(getApplicationContext(), R.layout.activity_main, respList);
        listView.setAdapter(customFavoritesAdapter);

        //add listener on refresh
        addListenerOnRefreshButton(sharedPreferences, customFavoritesAdapter);

        //add listener on autorefresh
        addListenerOnAutoRefreshButton(sharedPreferences, customFavoritesAdapter);

        final SwipeListener swipeListener = new SwipeListener();
        listView.setOnTouchListener(swipeListener);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(swipeListener.swipeDetected())
                {
                    HashMap<String, String> hashMap = customFavoritesAdapter.getItem(position);
                    final String removeSymbol = hashMap.get("SYMBOL");
                    showAlert(removeSymbol, sharedPreferences, customFavoritesAdapter, hashMap);
                }
                else
                {
                    HashMap<String, String> hashMap = customFavoritesAdapter.getItem(position);
                    final String favSymbol = hashMap.get("SYMBOL");
                    new AsyncTaskFavoriteRequestsRunner().execute(favSymbol);
                }
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void showAlert(final String sym, final SharedPreferences sprefs, final CustomFavoritesAdapter customAdapter, final HashMap<String, String> hashMap)
    {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        // set dialog message
        alertBuilder
                .setMessage("Do you want to delete "+hashMap.get("NAME")+" from Favorites?")
                .setCancelable(true)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        SharedPreferences.Editor editor = sprefs.edit();
                        editor.remove(sym);
                        editor.commit();
                        customAdapter.remove(hashMap);
                        customAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                    }
                });

        try
        {
            // create alert dialog
            AlertDialog alertDialog = alertBuilder.create();
            // show it
            alertDialog.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void addListenerOnButton()
    {
        Button clear = (Button) findViewById(R.id.clearButton);
        if (clear != null) {
            clear.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    actv.setText("");
                }
            });
        }
    }


    public void addListenerOnAutoRefreshButton(final SharedPreferences sharedPref, final CustomFavoritesAdapter cAdapter)
    {
        Switch aRefresh = (Switch)findViewById(R.id.switchButton);
        aRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            String symbol = "";
            String chsymbol = "";
            String company = "";
            String price = "";
            String mCap = "";
            String changePercent = "";
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Handler handler = new Handler();
                Timer timer = new Timer();

                if(isChecked)
                {
                        timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            Map<String, ?> map = sharedPref.getAll();
                                            Set<String> keyStrings = map.keySet();
                                            Iterator iterator = keyStrings.iterator();
                                            while (iterator.hasNext()) {
                                                Object tempKey = iterator.next();
                                                String tempResults = map.get(tempKey).toString();
                                                try {
                                                    JSONObject json = new JSONObject(tempResults);
                                                    symbol = json.getString("Symbol");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                AsyncTaskRefreshRunner asyncTaskRefreshRunner = new AsyncTaskRefreshRunner();
                                                asyncTaskRefreshRunner.execute(symbol);
                                                if (refreshResults.size() > 0) {
                                                    SharedPreferences sp = getSharedPreferences("PREFERENCES", 0);
                                                    String tSymbol = refreshResults.get("SYMBOL");
                                                    String lastprice = refreshResults.get("PRICE");
                                                    String change = refreshResults.get("CHANGE");
                                                    HashMap<String, String> hashMap = new HashMap<String, String>();
                                                    String tString = sp.getString(tSymbol, "");
                                                    try {
                                                        JSONObject json = new JSONObject(tString);
                                                        chsymbol = json.getString("Symbol");
                                                        company = json.getString("Name");
                                                        price = json.getString("LastPrice");
                                                        mCap = json.getString("MarketCap");
                                                        changePercent = json.getString("ChangePercent");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    //send to favorite adapter
                                                    hashMap.put("SYMBOL", chsymbol);
                                                    hashMap.put("NAME", company);
                                                    hashMap.put("PRICE", price);
                                                    hashMap.put("MARKET", mCap);
                                                    hashMap.put("CHANGE", changePercent);

                                                    int pos = cAdapter.resultList.indexOf(hashMap);
                                                    if(pos > 0)
                                                    {
                                                        HashMap<String, String> tMap = cAdapter.getItem(pos);
                                                        tMap.put("PRICE", lastprice);
                                                        tMap.put("CHANGE", change);

                                                        cAdapter.resultList.set(pos, tMap);
                                                        cAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                              }
                                            Log.d("TEST", "testing");
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                });
                            }
                        };
                        timer.schedule(timerTask, 0, 5000);
                        Log.d("AUTO REFRESH CLICKED", "ON");

                }

                else
                {
                    Log.d("AUTO REFRESH CLICKED", "OFF");
                    timerTask.cancel();
                }
            }
        });
    }

    public void addListenerOnRefreshButton(final SharedPreferences sharedPref, final CustomFavoritesAdapter cAdapter)
    {
        ImageButton autoRefresh = (ImageButton) findViewById(R.id.refresh);
        if(autoRefresh != null)
        {
            autoRefresh.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, ?> map = sharedPref.getAll();
                    Set<String> keyStrings = map.keySet();
                    Iterator iterator = keyStrings.iterator();
                    String symbol = "";
                    String company = "";
                    String price = "";
                    String mCap = "";
                    String changePercent = "";

                    while(iterator.hasNext()) {
                        Object tempKey = iterator.next();
                        String tempResults = map.get(tempKey).toString();
                        try
                        {
                            JSONObject json = new JSONObject(tempResults);
                            symbol = json.getString("Symbol");
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new AsyncTaskRefreshRunner().execute(symbol);
                        if(refreshResults.size() > 0)
                        {
                            SharedPreferences sp = getSharedPreferences("PREFERENCES", 0);
                            String tSymbol = refreshResults.get("SYMBOL");
                            String lastprice = refreshResults.get("PRICE");
                            String change = refreshResults.get("CHANGE");
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            String tString = sp.getString(tSymbol, "");
                            try {
                                JSONObject json = new JSONObject(tString);
                                symbol = json.getString("Symbol");
                                company = json.getString("Name");
                                price = json.getString("LastPrice");
                                mCap = json.getString("MarketCap");
                                changePercent = json.getString("ChangePercent");
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //send to favorite adapter
                            hashMap.put("SYMBOL",symbol);
                            hashMap.put("NAME",company);
                            hashMap.put("PRICE",price);
                            hashMap.put("MARKET",mCap);
                            hashMap.put("CHANGE",changePercent);

                            int pos = cAdapter.resultList.indexOf(hashMap);
                            if(pos > 0)
                            {
                                HashMap<String, String> tMap = cAdapter.getItem(pos);
                                tMap.put("PRICE", lastprice);
                                tMap.put("CHANGE", change);

                                cAdapter.resultList.set(pos, tMap);
                                cAdapter.notifyDataSetChanged();
                            }
                        }
                }
            }
        });
      }
    }

    //on click of Get Quote
    public void nextActivity(View v)
    {
        Context context = this;
        if(!actv.getText().toString().isEmpty())
        {
            if(!responseList.isEmpty())
            {
                for(int j=0;j<responseList.size();j++)
                {
                    HashMap<String, String> tMap = responseList.get(j);
                    if(tMap.get("Symbol").contentEquals(actv.getText().toString()))
                    {
                        new AsyncTaskRequestsRunner().execute();
                    }
                }
            }
            else
            {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                // set dialog message
                alertBuilder
                        .setMessage("Invalid Symbol")
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                // current activity
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertBuilder.create();

                // show it
                alertDialog.show();
            }
        }
        else
        {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
            // set dialog message
            alertBuilder
                    .setMessage("Please enter a Stock Name/Symbol")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertBuilder.create();

            // show it
            alertDialog.show();

        }
    }

    private void processResponse(String result)
    {
        try {
            JSONArray jsonarray = new JSONArray(result);
            responseList.clear();
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject json = jsonarray.getJSONObject(i);
                HashMap<String, String> map = new HashMap<String, String>();
                String symbolName = json.getString("Symbol");
                String cName = json.getString("Name");
                String ex = json.getString("Exchange");
                String app = cName+" ("+ex+")";
                map.put("Symbol",symbolName);
                map.put("Exchange",app);
                responseList.add(map);
            }
            final CustomAdapter adapter = new CustomAdapter(this,R.layout.activity_main,R.id.list_item_1, responseList);
            actv.setAdapter(adapter);
            actv.showDropDown();

            //listener to select item from dropdown
            actv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    async = false;
                    String atcInput = actv.getText().toString();
                    String[] split = atcInput.split("\\)");
                    String[] splitElems = split[1].split("=");
                    String inputElem = splitElems[1].substring(0,splitElems[1].length()-1);
                    actv.setText(inputElem);
                    actv.setSelection(actv.getText().length());
                    //dismiss dropdown

                }
            });
        }
        catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    private class AsyncTaskRequestsRunner extends AsyncTask<String, Void, Void>{

        String quoteURL = "http://stocksearch-android.appspot.com/?symbolName="+actv.getText().toString();
        String newsURL = "http://stocksearch-android.appspot.com/?symName="+actv.getText().toString();
        InputStream instream = null;
        String l = null;
        InputStream stream = null;
        String line = null;
        String resp = "";

        @Override
        protected Void doInBackground(String... params) {
            //get quote processing
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(quoteURL);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder sb = new StringBuilder();
                while((l = reader.readLine()) != null)
                {
                    sb.append(l+"\n");
                }
                instream.close();
                resultQuote = sb.toString();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            //news feed call
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(newsURL);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                stream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder sb = new StringBuilder();
                while((line = reader.readLine()) != null)
                {
                    sb.append(line+"\n");
                }
                stream.close();
                resp = sb.toString();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            //process news feed results
            try {
                JSONObject jsonObject = new JSONObject(resp);
                JSONObject json = jsonObject.getJSONObject("responseData");
                JSONArray jsonArray = json.getJSONArray("results");
                googleList.clear();
                for(int j=0; j<jsonArray.length(); j++)
                {
                    JSONObject jsonObj = jsonArray.getJSONObject(j);
                    String publisher = jsonObj.getString("publisher");
                    String pubDate = jsonObj.getString("publishedDate");
                    String url = jsonObj.getString("unescapedUrl");
                    String content = jsonObj.getString("content");
                    String title = jsonObj.getString("title");
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Publisher",publisher);
                    map.put("PublishDate",pubDate);
                    map.put("URL",url);
                    map.put("Content",content);
                    map.put("Title",title);
                    googleList.add(map);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            processAllResponse();
        }
    }

    //method processing all response from background
    public void processAllResponse()
    {
        Context context = this;
        try{
            JSONObject json = new JSONObject(resultQuote);
            getQuoteStatus = json.getString("Status");
            if(getQuoteStatus.contains("SUCCESS"))
            {
                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra(JSINTENT, resultQuote);
                String symbol = json.getString("Symbol");
                String company = json.getString("Name");
                String price = json.getString("LastPrice");

                intent.putExtra(SYMBOLNAME,symbol);
                intent.putExtra(COMPANY,company);
                intent.putExtra(PRICE,price);
                intent.putExtra(NEWS, (Serializable) googleList);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else
            {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                // set dialog message
                alertBuilder
                        .setMessage("No stock data found!")
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                // current activity
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertBuilder.create();

                // show it
                alertDialog.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Get Quote for Favorite selection
    private class AsyncTaskFavoriteRequestsRunner extends AsyncTask<String, Void, Void>{

        String quoteURL = "";
        String newsURL = "";
        InputStream instream = null;
        String l = null;
        InputStream stream = null;
        String line = null;
        String resp = "";

        @Override
        protected Void doInBackground(String... params) {
            //get quote processing
            try {
                quoteURL = "http://stocksearch-android.appspot.com/?symbolName="+params[0];
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(quoteURL);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder sb = new StringBuilder();
                while((l = reader.readLine()) != null)
                {
                    sb.append(l+"\n");
                }
                instream.close();
                resultQuote = sb.toString();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            //news feed call
            try {
                newsURL = "http://stocksearch-android.appspot.com/?symName="+params[0];
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(newsURL);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                stream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder sb = new StringBuilder();
                while((line = reader.readLine()) != null)
                {
                    sb.append(line+"\n");
                }
                stream.close();
                resp = sb.toString();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            //process news feed results
            try {
                JSONObject jsonObject = new JSONObject(resp);
                JSONObject json = jsonObject.getJSONObject("responseData");
                JSONArray jsonArray = json.getJSONArray("results");
                googleList.clear();
                for(int j=0; j<jsonArray.length(); j++)
                {
                    JSONObject jsonObj = jsonArray.getJSONObject(j);
                    String publisher = jsonObj.getString("publisher");
                    String pubDate = jsonObj.getString("publishedDate");
                    String url = jsonObj.getString("unescapedUrl");
                    String content = jsonObj.getString("content");
                    String title = jsonObj.getString("title");
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Publisher",publisher);
                    map.put("PublishDate",pubDate);
                    map.put("URL",url);
                    map.put("Content",content);
                    map.put("Title",title);
                    googleList.add(map);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            processAllFavoritesResponse();
        }
    }


    //method processing all response from background
    public void processAllFavoritesResponse()
    {
        Context context = this;
        try{
            JSONObject json = new JSONObject(resultQuote);
            getQuoteStatus = json.getString("Status");
            if(getQuoteStatus.contains("SUCCESS"))
            {
                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra(JSINTENT, resultQuote);
                String symbol = json.getString("Symbol");
                String company = json.getString("Name");
                String price = json.getString("LastPrice");

                intent.putExtra(SYMBOLNAME,symbol);
                intent.putExtra(COMPANY,company);
                intent.putExtra(PRICE,price);
                intent.putExtra(NEWS, (Serializable) googleList);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else
            {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                // set dialog message
                alertBuilder
                        .setMessage("No stock data found!")
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                // current activity
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertBuilder.create();

                // show it
                alertDialog.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //lookup API call-->AutoComplete
    private class AsyncTaskRunner extends AsyncTask<String, Void, String> {

        String url = "http://stocksearch-android.appspot.com/?input="+actv.getText().toString();
        InputStream stream = null;
        String line = null;
        String result = "";

        @Override
        protected String doInBackground(String... params) {
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet(url);
                    HttpResponse response = httpclient.execute(httpget);
                    HttpEntity entity = response.getEntity();
                    stream = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder sb = new StringBuilder();
                    while((line = reader.readLine()) != null)
                    {
                        sb.append(line+"\n");
                    }
                    stream.close();
                    result = sb.toString();
                } catch (ClientProtocolException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            processResponse(result);
        }
    }

    private class AsyncTaskRefreshRunner extends AsyncTask<String, Void, String>{
        InputStream instream = null;
        String l = "";
        String resultSets;


        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String symbolName = params[0];
            String quoteURL = "http://stocksearch-android.appspot.com/?symbolName="+symbolName;
            //get quote processing
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(quoteURL);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder sb = new StringBuilder();
                while((l = reader.readLine()) != null)
                {
                    sb.append(l+"\n");
                }
                instream.close();
                resultSets = sb.toString();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return resultSets;
        }

        @Override
        protected void onPostExecute(String results) {
            try
            {
                JSONObject json = new JSONObject(results);
                String price = json.getString("LastPrice");
                String change = json.getString("ChangePercent");
                String symbol = json.getString("Symbol");
                refreshResults.put("PRICE",price);
                refreshResults.put("CHANGE",change);
                refreshResults.put("SYMBOL",symbol);
                progressBar.setVisibility(View.INVISIBLE);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://android.com.web.stocksearch/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://android.com.web.stocksearch/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}



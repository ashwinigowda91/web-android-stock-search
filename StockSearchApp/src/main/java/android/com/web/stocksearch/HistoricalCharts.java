package android.com.web.stocksearch;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoricalCharts extends Fragment {


    public HistoricalCharts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String name = getArguments().getString("Symbol");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_historical_charts, container, false);
        final WebView webView = (WebView) view.findViewById(R.id.chart);
        webView.getSettings().setJavaScriptEnabled(true);
        String custom =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "    <head>\n" +
                        "        <meta charset=\"utf-8\">\n" +
                        "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"+"<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js\"></script>\n<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js\"></script>\n" +
                        "\n" +
                        "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1/jquery-ui.min.js\"></script>\n"+"<script src=\"https://code.highcharts.com/stock/highstock.js\"></script>\n" +
                        "                <script src=\"https://code.highcharts.com/stock/modules/exporting.js\"></script> "+"<script src='chart.js'></script></head><body><div id='chartDisplay'></div></body> </html>";


        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String string)
            {
                webView.loadUrl("javascript:display('"+name+"')");
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadDataWithBaseURL("file:///android_asset/", custom, "text/html","UTF-8", null);
        return view;
    }

}

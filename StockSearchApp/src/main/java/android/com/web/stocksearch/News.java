package android.com.web.stocksearch;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class News extends Fragment {


    public News() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        ListView listView = (ListView) view.findViewById(R.id.newslistview);

        //include header
        View header = inflater.inflate(R.layout.news_header, listView, false);
        listView.addHeaderView(header, null, false);

        List<HashMap<String, String>> mapList = (List<HashMap<String, String>>) getArguments().getSerializable("NewsResults");
        listView.setAdapter(new CustomNewsAdapter(getActivity().getApplicationContext(), R.layout.fragment_stock_details, mapList));
        return view;
    }

}

package android.com.web.stocksearch;
import android.net.Uri;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    String getResult;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private String company;
    private String symbolName;
    private String price;

    private void publishPost()
    {
        ShareDialog shareDialog = new ShareDialog(this);
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Current Stock Price of "+company+", "+price)
                    .setContentDescription("Stock Information of "+company)
                    .setContentUrl(Uri.parse("http://finance.yahoo.com/q?s="+symbolName))
                    .setImageUrl(Uri.parse("http://chart.finance.yahoo.com/t?s="+symbolName+"&lang=en-US&width=200&height=200"))
                    .build();
            shareDialog.show(linkContent);
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {

                }

                @Override
                public void onCancel() {
                    Toast.makeText(getApplicationContext(), "Not posted to Facebook!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(FacebookException error) {

                }
            });
        }
    }

    private void onFblogin()
    {
        callbackManager = CallbackManager.Factory.create();
        List<String> permissions = Arrays.asList("publish_actions");
        loginManager = LoginManager.getInstance();
        loginManager.logInWithPublishPermissions(this, permissions);

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                publishPost();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Not posted to Facebook!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Not posted to Facebook!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Facebook
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        toolbar = (Toolbar)findViewById(R.id.toolBarSub);
        //toolbar.setTitle(intent.getStringExtra(MainActivity.COMPANY));
        TextView textView = (TextView) toolbar.findViewById(R.id.titleSub);
        textView.setText(intent.getStringExtra(MainActivity.COMPANY));

        getResult = intent.getStringExtra(MainActivity.JSINTENT);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Button button = (Button) findViewById(R.id.fb);
        if(button != null)
        {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onFblogin();
                }
            });
        }

        symbolName = intent.getStringExtra(MainActivity.SYMBOLNAME);
        company = intent.getStringExtra(MainActivity.COMPANY);
        price = intent.getStringExtra(MainActivity.PRICE);
        final Button star = (Button) findViewById(R.id.emptystar);
            SharedPreferences sharedPrefs = getSharedPreferences("PREFERENCES",0);
            if(sharedPrefs.getString(symbolName,"") != "")
            {
                Drawable draw = star.getBackground();
                star.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellowstar));
            }
            else
            {
                Drawable draw = star.getBackground();
                star.setBackgroundDrawable(getResources().getDrawable(R.drawable.star));
            }

            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        Drawable draw = star.getBackground();
                        if(draw.getConstantState() == getResources().getDrawable(R.drawable.star).getConstantState())
                        {
                            star.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellowstar));
                            SharedPreferences sharedPrefs = getSharedPreferences("PREFERENCES",0);
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString(symbolName,getResult);
                            editor.commit();
                            Toast.makeText(v.getContext(),"Bookmarked "+company+"!",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            star.setBackgroundDrawable(getResources().getDrawable(R.drawable.star));
                            SharedPreferences sharedPrefs = getSharedPreferences("PREFERENCES",0);
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.remove(symbolName);
                            editor.commit();
                        }
                }
            });
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        viewPager = (ViewPager)findViewById(R.id.viewPager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new StockDetails(),"Current");
        viewPagerAdapter.addFragments(new HistoricalCharts(),"Historical");
        viewPagerAdapter.addFragments(new News(),"News");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //get the result of get quote
        android.support.v4.app.Fragment stockFragment = viewPagerAdapter.getItem(0);
        Bundle stockbundle = new Bundle();
        stockbundle.putString("StockResults",getResult);
        stockFragment.setArguments(stockbundle);
        View view = stockFragment.getView();

        //Historical Charts
        android.support.v4.app.Fragment chartFragment = viewPagerAdapter.getItem(1);
        Bundle chartbundle = new Bundle();
        chartbundle.putString("Symbol",symbolName);
        chartFragment.setArguments(chartbundle);
        View chartView = chartFragment.getView();

        //get results of Google News feed
        List<HashMap<String, String>> newsMap = (List<HashMap<String, String>>) intent.getSerializableExtra(MainActivity.NEWS);
        android.support.v4.app.Fragment newsFragment = viewPagerAdapter.getItem(2);
        Bundle newsbundle = new Bundle();
        newsbundle.putSerializable("NewsResults", (Serializable) newsMap);
        newsFragment.setArguments(newsbundle);
        View newsView = newsFragment.getView();

    }


}

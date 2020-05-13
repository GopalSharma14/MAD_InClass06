package com.example.inclass06;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView list_tv;
    private TextView load_tv;
    private TextView curr_num;
    private TextView total_num;
    private TextView title_tv;
    private TextView published_at_tv;
    private TextView description;
    private TextView of_tv;

    private Button go_btn;
    private Button next_btn;
    private Button prev_btn;

    private ImageView pic_iv;
    private ImageView next_iv;
    private ImageView prev_iv;

    private ProgressBar progressBar;

    private int counter = 0;

    ArrayList<News> url_list = null;

    String base_url = "http://newsapi.org/v2/top-headlines";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String category = "sports";

        if (isConnected()) {
            Toast.makeText(MainActivity.this, "Internet is connected!!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Internet is not connected!!", Toast.LENGTH_SHORT).show();
        }

        go_btn = findViewById(R.id.go_btn);

        list_tv = findViewById(R.id.list_tv);
        load_tv = findViewById(R.id.load_tv);
        load_tv.setVisibility(TextView.INVISIBLE);

        title_tv = findViewById(R.id.title_tv);
        title_tv.setVisibility(TextView.INVISIBLE);
        published_at_tv = findViewById(R.id.published_at_tv);
        published_at_tv.setVisibility(TextView.INVISIBLE);
        description = findViewById(R.id.description);
        description.setVisibility(TextView.INVISIBLE);


        curr_num = findViewById(R.id.current_num);
        curr_num.setVisibility(TextView.INVISIBLE);
        of_tv = findViewById(R.id.of_tv);
        of_tv.setVisibility(TextView.INVISIBLE);
        total_num = findViewById(R.id.total_num);
        total_num.setVisibility(TextView.INVISIBLE);

        pic_iv = findViewById(R.id.pic_iv);
        pic_iv.setVisibility(ImageView.INVISIBLE);
        next_iv = findViewById(R.id.next_iv);
        prev_iv = findViewById(R.id.prev_iv);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        final String[] categories = {"business", "entertainment", "general", "health", "science", "sports", "technology"};

        go_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Search")
                        .setItems(categories, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("demo", "Clicked on " + categories[which]);
                                String keyword = categories[which];
                                list_tv.setText(categories[which]);
                                new GetJSONData().execute(categories[which]);
                            }
                        });
                builder.create().show();
            }
        });

        if (url_list == null)
            next_iv.setAlpha(0.5f);

        if (url_list == null)
            prev_iv.setAlpha(0.5f);

        next_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (url_list != null) {
                    News item = url_list.get((++counter) % url_list.size());
                    Picasso.get().load(item.imageURL).into(pic_iv);
                    title_tv.setText(item.title);
                    published_at_tv.setText(item.publishedAt);
                    description.setText(item.description);
                    curr_num.setText(Integer.toString(((counter) % url_list.size()) + 1));
                }
            }
        });

        prev_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (url_list != null) {
                    counter--;
                    if (counter <= 0) {
                        counter = url_list.size() - 1;
                    }
                    News item = url_list.get((counter) % url_list.size());
                    Picasso.get().load(item.imageURL).into(pic_iv);
                    title_tv.setText(item.title);
                    published_at_tv.setText(item.publishedAt);
                    description.setText(item.description);
                    curr_num.setText(Integer.toString(((counter) % url_list.size())+1));
                }
            }
        });

    }


    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }


    class GetJSONData extends AsyncTask<String, Void, ArrayList<News>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            counter = 0;
            pic_iv.setImageDrawable(null);
            load_tv.setVisibility(TextView.VISIBLE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
            curr_num.setVisibility(TextView.INVISIBLE);
            of_tv.setVisibility(TextView.INVISIBLE);
            total_num.setVisibility(TextView.INVISIBLE);
            title_tv.setVisibility(TextView.INVISIBLE);
            published_at_tv.setVisibility(TextView.INVISIBLE);
            description.setVisibility(TextView.INVISIBLE);
            pic_iv.setVisibility(View.INVISIBLE);

        }

        @Override
        protected ArrayList<News> doInBackground(String... strings) {
            HttpURLConnection con = null;
            ArrayList<News> news = new ArrayList<>();
            String category = strings[0];
            try {
                URL url = new URL(base_url
                        + "?apikey=" + getResources().getString(R.string.API_key)
                        + "&country=us"
                        + "&category=" + URLEncoder.encode(category, "UTF-8"));

                con = (HttpURLConnection) url.openConnection();
                con.connect();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String json = IOUtils.toString(con.getInputStream(), "UTF8");
                    JSONObject root = new JSONObject(json);
                    JSONArray articles = root.getJSONArray("articles");
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject articleJSON = articles.getJSONObject(i);

                        News news_item = new News();

                        news_item.title = articleJSON.getString("title");
                        news_item.publishedAt = articleJSON.getString("publishedAt");
                        news_item.imageURL = articleJSON.getString("urlToImage");
                        news_item.description = articleJSON.getString("description");
                        news.add(news_item);

                    }
                }
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JSONException ex) {
                ex.printStackTrace();
            } finally {
                con.disconnect();
            }

            return news;
        }

        @Override
        protected void onPostExecute(ArrayList<News> news) {
            super.onPostExecute(news);
            url_list = news;
            Log.d("randaap", ""+url_list);

            load_tv.setVisibility(TextView.INVISIBLE);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            if (url_list != null && !url_list.isEmpty()) {
                Log.d("demo", "post exec" + url_list.get(counter));
                News item = url_list.get(counter);
                Picasso.get().load(item.imageURL).into(pic_iv);
                pic_iv.setVisibility(ImageView.VISIBLE);
                title_tv.setText(item.title);
                title_tv.setVisibility(TextView.VISIBLE);
                published_at_tv.setText(item.publishedAt);
                published_at_tv.setVisibility(TextView.VISIBLE);
                description.setText(item.description);
                description.setVisibility(TextView.VISIBLE);
                curr_num.setText(Integer.toString(counter + 1));
                curr_num.setVisibility(TextView.VISIBLE);
                of_tv.setVisibility(TextView.VISIBLE);
                total_num.setText("" + url_list.size());
                total_num.setVisibility(TextView.VISIBLE);

                if (url_list.size() > 1) {
                    next_iv.setAlpha(1f);
                    next_iv.setClickable(true);

                    prev_iv.setAlpha(1f);
                    prev_iv.setClickable(true);

                }
                else {
                    next_iv.setAlpha(0.5f);
                    next_iv.setClickable(false);
                    prev_iv.setAlpha(0.5f);
                    prev_iv.setClickable(false);
                }
            }
            else {
                next_iv.setAlpha(0.5f);
                next_iv.setClickable(false);
                prev_iv.setAlpha(0.5f);
                prev_iv.setClickable(false);
                Toast.makeText(MainActivity.this, "No News Found!!", Toast.LENGTH_SHORT).show();
            }

        }
    }
}


package com.example.sleung.impact;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;

import com.example.sleung.impact.models.Legislator;


import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;


public class LegislatorsActivity extends ActionBarActivity {

    private String APIKEY = "UA8MW8CXNKX49HXPZMYCJ0KWXYXOUGW2";

    public static class Bill {
        public String id;
        public String bill_number;
        public String title;
        public String description;

        public Bill(String id, String bill_number, String title, String description) {
            this.id = id;
            this.bill_number = bill_number;
            this.title = title;
            this.description = description;
        }
    }

    public interface BillsService {
        @GET("/bills?q={category}&legislature={state}&apikey={api_key}")
        Call<Bill> getBills(@Query("q") String query, @Query("legislature") String state, @Query("apikey") String apikey);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legislators_list);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.fiscalnote.com")
                .build();

        BillsService service = retrofit.create(BillsService.class);
        Call<Bill> call = service.getBills("gay marriange", "ct", APIKEY);
        call.enqueue(new Callback<List<Bill>>() {
            @Override
            public void onResponse(Response<List<Bill>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    // tasks available
                } else {
                    // error response, no access to resource?
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

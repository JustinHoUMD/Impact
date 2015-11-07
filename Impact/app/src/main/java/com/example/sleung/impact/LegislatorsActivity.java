package com.example.sleung.impact;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;


public class LegislatorsActivity extends ActionBarActivity {

    private static final String APIKEY = "UA8MW8CXNKX49HXPZMYCJ0KWXYXOUGW2";
    private static final String TAG = "LegislatorsActivity";

    public static class Vote {
        public String leg_id;
        public String name;
    }

    public static class BillVote {
        public List<Vote> yes_votes;
        public List<Vote> no_votes;
    }
    public static class Bill {
        public String id;
        public String bill_number;
        public String title;
        public String description;
        public List<BillVote> bill_votes;

        public Bill(String id, String bill_number, String title, String description) {
            this.id = id;
            this.bill_number = bill_number;
            this.title = title;
            this.description = description;
        }
    }

    public interface BillsService {
        @GET("/bills")
        Call<List<Bill>> getBills(@Query("q") String query, @Query("legislature") String state, @Query("apikey") String apikey);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legislators_list);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.fiscalnote.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BillsService service = retrofit.create(BillsService.class);
        Call<List<Bill>> call = service.getBills("rights", "*", APIKEY);
        call.enqueue(new Callback<List<Bill>>() {
            @Override
            public void onResponse(Response<List<Bill>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    Log.d(TAG, "Response successful!");
                    Log.d(TAG, "Response: " + response.body().toString());
                    for (Bill bill: response.body()) {
                        BillVote bv = bill.bill_votes.get(0);
                        for (Vote yes: bv.yes_votes) {
                            Log.d(TAG, yes.name);
                        }
                    }
                } else {
                    Log.d(TAG, "Response failure");
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

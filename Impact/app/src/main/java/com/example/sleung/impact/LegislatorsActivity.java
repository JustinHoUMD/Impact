package com.example.sleung.impact;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private ListView mListView;

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
        public String chamber;
        public String current_status_date;
        public String current_status;
        public String lastActionString = "Proposed";
        public String created_at;
        public String description;
        public List<BillVote> bill_votes;
        public List<String> yesLegislators = new ArrayList<String>();
        public List<String> noLegislators = new ArrayList<String>();
    }

    public interface BillsService {
        @GET("/bills")
        Call<List<Bill>> getBills(@Query("q") String query, @Query("legislature") String state, @Query("apikey") String apikey);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legislators_activity);

        mListView = (ListView) findViewById(R.id.lvLegislators);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.fiscalnote.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BillsService service = retrofit.create(BillsService.class);
        String query = getIntent().getStringExtra("query");
        Call<List<Bill>> call = service.getBills(query, "CT", APIKEY);
        call.enqueue(new Callback<List<Bill>>() {
            @Override
            public void onResponse(Response<List<Bill>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    Log.d(TAG, "Response successful!");
                    Log.d(TAG, "Response: " + response.body().toString());
                    ArrayList<Bill> bills = new ArrayList<Bill>();
                    for (Bill bill: response.body()) {
                        ArrayList<BillVote> bvs = (ArrayList<BillVote>) bill.bill_votes;
                        if (!bvs.isEmpty()) {
                            for (BillVote bv:bvs) {
                                for (Vote yes: bv.yes_votes) {
                                    bill.yesLegislators.add(yes.leg_id);
                                }
                                for (Vote no: bv.no_votes) {
                                    bill.noLegislators.add(no.leg_id);
                                }

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                Date lastUpdatedDate = null;
                                SimpleDateFormat outFormat = new SimpleDateFormat("MM/dd/yyyy");

                                try {
                                    lastUpdatedDate = sdf.parse(bill.current_status_date);
                                    String cap = bill.current_status.substring(0, 1).toUpperCase() + bill.current_status.substring(1);
                                    bill.lastActionString = cap + " (" + outFormat.format(lastUpdatedDate)+ ")";
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    try {
                                        bill.lastActionString = "Proposed" + " (" + outFormat.format(sdf.parse(bill.created_at))+ ")" ;
                                    } catch (ParseException ex) {
                                        ex.printStackTrace();
                                    }
                                }

                            }
                            bills.add(bill);
                        }
                        Log.d(TAG, "Yes: "+bill.yesLegislators.toString());
                        Log.d(TAG, "No: "+bill.noLegislators.toString());
                    }
                    BillAdapter adapter = new BillAdapter(getApplicationContext(),R.layout.legislators_list,bills);
                    mListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        }
                    });
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

    private class BillAdapter extends ArrayAdapter {

        private ArrayList<Bill> bills;
        private Context context;

        public BillAdapter(Context context, int resource, List<Bill> bills) {
            super(context, resource, bills);
            this.context = context;
            this.bills = (ArrayList<Bill>) bills;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Bill bill = bills.get(position);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.legislators_list, parent, false);

            TextView title = (TextView) rowView.findViewById(R.id.title);
            TextView chamber = (TextView) rowView.findViewById(R.id.subtitle);
            title.setText(bill.title);
            chamber.setText(bill.lastActionString);
            return rowView;
        }
    }
}

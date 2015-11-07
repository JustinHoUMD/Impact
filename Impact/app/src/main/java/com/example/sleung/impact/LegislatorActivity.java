package com.example.sleung.impact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.sleung.impact.models.Legislator;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by me on 11/7/15.
 */
public class LegislatorActivity extends ActionBarActivity{
    private static final String TAG = "LegislatorActivity";
    private BillActivity.Bill bill;
    private ListView mLegislatorList;
    private Context context;
    private int separationIndex;
    private ArrayList<String> legislators = new ArrayList<String>();

    public interface BillsService {
        @GET("/legislator/{id}")
        Call<Legislator> getLegislator(@Path("id") String id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BillActivity.Bill b = (BillActivity.Bill) getIntent().getSerializableExtra("bill");
        separationIndex = b.yesLegislators.size();
        if(b.yesLegislators != null) {
            legislators.addAll(b.yesLegislators);
        }
        if (b.noLegislators != null) {
            legislators.addAll(b.noLegislators);
        }
        mLegislatorList = (ListView) findViewById(R.id.legislator_list);
        LegislatorAdapter adapter = new LegislatorAdapter(this, R.id.legislator_list, legislators);
        Log.d(TAG, "Legislators: "+legislators);
        mLegislatorList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private class LegislatorAdapter extends ArrayAdapter {
        private Context context;
        private ArrayList<String> legislators;
        public LegislatorAdapter(Context context, int resource, ArrayList<String> legislators) {
            super(context, resource, legislators);
            this.context = context;
            this.legislators = legislators;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.legislator_list, parent, false);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.github.com")
                    .build();

            BillsService service = retrofit.create(BillsService.class);
            Call<Legislator> call = service.getLegislator(legislators.get(position));
            call.enqueue(new Callback<Legislator>() {
                @Override
                public void onResponse(Response<Legislator> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        Log.d(TAG, "Response successful!");
                        Log.d(TAG, "Response: " + response.body().toString());

                    } else {
                        Log.d(TAG, "Response Failed");
                    }

                }

                @Override
                public void onFailure(Throwable t) {

                }

                });
            return rowView;
        }
    }
}

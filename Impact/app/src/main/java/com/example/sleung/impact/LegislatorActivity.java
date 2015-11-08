package com.example.sleung.impact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sleung.impact.models.Legislator;

import java.util.ArrayList;

import java.util.ArrayList;
import java.util.List;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
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
    private static final String API_KEY = "UA8MW8CXNKX49HXPZMYCJ0KWXYXOUGW2";
    private BillActivity.Bill bill;
    private ListView mLegislatorList;
    private Context context;
    private int separationIndex;
    private ArrayList<String> legislators;
    private ArrayList<Legislator> legislators2;
    private LegislatorAdapter adapter;
    private Activity activity = this;

    public interface BillsService {
        @GET("/legislator/{id}")
        Call<Legislator> getLegislator(@Path("id") String id, @Query("apikey") String apikey);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legislator_activity);
        legislators = new ArrayList<String>();
        legislators2 = new ArrayList<Legislator>();
        BillActivity.Bill b = (BillActivity.Bill) getIntent().getSerializableExtra("bill");
        setTitle(b.title);
        if(b.yesLegislators != null) {
            legislators.addAll(b.yesLegislators);
        }
        if (b.noLegislators != null) {
            legislators.addAll(b.noLegislators);
        }
        separationIndex = b.yesLegislators.size();
        mLegislatorList = (ListView) findViewById(R.id.legislator_list);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.fiscalnote.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BillsService service = retrofit.create(BillsService.class);
        for(int i=0; i<legislators.size(); i++) {
            final int index = i;
            String leg = legislators.get(index);
            Call<Legislator> call = service.getLegislator(leg, API_KEY);
            call.enqueue(new Callback<Legislator>() {
                @Override
                public void onResponse(Response<Legislator> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        Legislator leg = response.body();
                        if (Math.random() < 0.7) {
                            leg.vote = true;
                        } else {
                            leg.vote = false;
                        }
                        legislators2.add(leg);
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        } else {
                            adapter = new LegislatorAdapter(activity, R.id.legislator_list, legislators2);
                            mLegislatorList.setAdapter(adapter);
                        }
                    } else {
                        Log.d(TAG, "Response Failed");
                        Log.d(TAG, "Response: " + response.message().toString());
                    }

                }

                @Override
                public void onFailure(Throwable t) {

                }

            });
        }
        Log.d(TAG, "Adapter: " + mLegislatorList);
        Log.d(TAG, "Legislators: " + legislators);
    }


    private class LegislatorAdapter extends ArrayAdapter {
        private Context context;
        private ArrayList<Legislator> legislators;
        public LegislatorAdapter(Context context, int resource, ArrayList<Legislator> legislators) {
            super(context, resource, legislators);
            this.context = context;
            this.legislators = legislators;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Legislator leg = legislators.get(position);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View rowView = inflater.inflate(R.layout.legislator_list, parent, false);
            TextView name = (TextView) rowView.findViewById(R.id.legislator_name);
            TextView party = (TextView) rowView.findViewById(R.id.party_name);
            name.setText(leg.full_name);
            if (leg.party != null) {
                party.setText(leg.party);
            } else {
                party.setText("Independent");
            }

            if (!leg.vote) {
                ImageView image = (ImageView) rowView.findViewById(R.id.vote);
                image.setBackgroundResource(R.drawable.close);
                image.setColorFilter(Color.argb(170, 170, 170, 255));
            }
            ImageView call = (ImageView) rowView.findViewById(R.id.call);
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri number = Uri.parse("tel:3013256815");
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                    startActivity(callIntent);
                }
            });

            ImageView email = (ImageView) rowView.findViewById(R.id.email);
            email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"shermankleung@gmail.com"});
                    try {
                        startActivity(Intent.createChooser(intent, "Send Email"));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getBaseContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return rowView;
        }
    }
}

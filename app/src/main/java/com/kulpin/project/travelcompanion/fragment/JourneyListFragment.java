package com.kulpin.project.travelcompanion.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kulpin.project.travelcompanion.AppController;
import com.kulpin.project.travelcompanion.Constants;
import com.kulpin.project.travelcompanion.MainActivity;
import com.kulpin.project.travelcompanion.R;
import com.kulpin.project.travelcompanion.adapter.JourneyListAdapter;
import com.kulpin.project.travelcompanion.dto.JourneyDTO;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Andrei on 10.04.2016.
 */
public class JourneyListFragment extends TabFragment {
    private static final int LAYOUT = R.layout.fragment_timeline_main;
    private Context context;
    private View view;
    private RecyclerView recyclerView;
    private JourneyListAdapter journeyListAdapter;
    private List<JourneyDTO> list;

    public static JourneyListFragment getInstance(Context context, String title){
        Bundle args = new Bundle();
        args.putString("title", title);
        JourneyListFragment fragment = new JourneyListFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        Log.d("myLOG", "onCreateView " + this.toString());
        list = new ArrayList<>();
        syncJourneyList();

        recyclerView = (RecyclerView)view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        journeyListAdapter = new JourneyListAdapter(list, getActivity());
        recyclerView.setAdapter(journeyListAdapter);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).build());

        return view;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public JourneyListAdapter getJourneyListAdapter() {
        return journeyListAdapter;
    }

    public long getItemId(int position){
        return list.get(position).getId();
    }

    public void syncJourneyList(){

        String URL = "";
        switch (getArguments().getString("title")){
            case "active": URL = Constants.URL.GET_ACTIVE_JOURNEYS + 1;
                break;
            case "last": URL = Constants.URL.GET_LAST_JOURNEYS + 1;
                break;
        }
        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (list == null) {
                    return;
                }
                list.clear();
                for(int i=0;i<response.length();i++){
                    try{
                        JSONObject obj=response.getJSONObject(i);
                        JourneyDTO item=new JourneyDTO();
                        item.setId(obj.getLong("id"));
                        item.setTitle(obj.getString("title"));
                        item.setStartDate(new Date(obj.getLong("startDate")));
                        item.setEndDate(new Date(obj.getLong("endDate")));
                        list.add(item);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
                journeyListAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    public void addNewJourney(JourneyDTO newJourney){
        String URL = Constants.URL.ADD_JOURNEY;
        JSONObject object = new JSONObject();

        try {
            object.accumulate("userId", 0);
            object.accumulate("title", newJourney.getTitle());
            object.accumulate("startDate", newJourney.getStartDate().getTime());
            object.accumulate("endDate", newJourney.getEndDate().getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("myLOG", "new journey created = " + response);
                ((PagesContainerFragment) getParentFragment()).onRefresh();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("myLOG", "error creating new journey = " + error
                        /*+ ">>" + error.networkResponse.statusCode
                        + ">>" + error.networkResponse.data*/
                        + ">>" + error.getCause()
                        + ">>" + error.getMessage());
            }

        });
        AppController.getInstance().addToRequestQueue(request);
    }

    public void deleteJourney(long journeyId){
        //if (list == null) return;
        //journeyId = list.get(list.size() - 1).getId();
        String URL = Constants.URL.DELETE_JOURNEY + journeyId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("myLOG", "journey deleted: " + response);
                syncJourneyList();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("myLOG", "error deleting journey = " + error);
                syncJourneyList();
            }

        });
        AppController.getInstance().addToRequestQueue(request);

    }


    private List<JourneyDTO> createMockJourneyListData() {
        List<JourneyDTO> data = new ArrayList<>();

        for (int i =1; i <= 10; i ++) {
            data.add(new JourneyDTO("Title " + i, new Date(), new Date()));
        }
        return data;
    }
}

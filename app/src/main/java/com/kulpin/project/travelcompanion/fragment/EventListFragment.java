package com.kulpin.project.travelcompanion.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kulpin.project.travelcompanion.dto.JourneyDTO;
import com.kulpin.project.travelcompanion.controller.AppController;
import com.kulpin.project.travelcompanion.utilities.Constants;
import com.kulpin.project.travelcompanion.R;
import com.kulpin.project.travelcompanion.adapter.EventListAdapter;
import com.kulpin.project.travelcompanion.dto.EventDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventListFragment extends TabFragment{
    private static final int LAYOUT = R.layout.fragment_timeline_main;

    private Context context;
    private View view;
    private EventListAdapter eventListAdapter;
    private List<EventDTO> list;

    /*public static EventListFragment getInstance(Context context){
        Bundle args = new Bundle();
        EventListFragment fragment = new EventListFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        return fragment;
    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        list = new ArrayList<>();
        RecyclerView rv = (RecyclerView)view.findViewById(R.id.recycleView);
        rv.setLayoutManager(new LinearLayoutManager(context));
        eventListAdapter = new EventListAdapter(list, getActivity());
        rv.setAdapter(eventListAdapter);
        syncEventList();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncEventList();
        eventListAdapter.notifyDataSetChanged();
    }

    public void syncEventList(){
        final ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_main);
        progressBar.setVisibility(View.VISIBLE);
        String URL = Constants.URL.GET_ALL_EVENTS + getArguments().getLong("journeyId");
        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                list.clear();
                for(int i=0; i<response.length(); i++){
                    try{
                        JSONObject object = response.getJSONObject(i);
                        EventDTO item = new EventDTO();
                        item.setId(object.getLong("id"));
                        item.setType(object.getInt("type"));
                        item.setTitle(object.getString("title"));
                        item.setPlace(object.getString("place"));
                        item.setDeparturePlace(object.getString("departurePlace"));
                        item.setDestinationPlace(object.getString("destinationPlace"));
                        item.setStartDate(new Date(object.getLong("startDate")));
                        item.setStartTime(new Date(object.getLong("startTime")));
                        item.setEndDate(new Date(object.getLong("endDate")));
                        item.setEndTime(new Date(object.getLong("endTime")));
                        list.add(item);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
                eventListAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tclog", "error syncronization event list = " + error);
            }
        });
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    public void addNewEvent(EventDTO newEvent){
        String URL = Constants.URL.ADD_EVENT;
        JSONObject object = new JSONObject();

        //Log.d("tclog", ""  + (newEvent.getStartTime() == null));

        //if (newEvent.getStartTime() == null) return;

        try {
            if (newEvent.getId() != 0) object.accumulate("id", newEvent.getId());
            object.accumulate("journeyId", getArguments().getLong("journeyId"));
            object.accumulate("userId", 0); //proper value is set on server
            object.accumulate("type", newEvent.getType());
            object.accumulate("title", newEvent.getTitle());
            object.accumulate("place", newEvent.getPlace());
            object.accumulate("departurePlace", newEvent.getDeparturePlace());
            object.accumulate("destinationPlace", newEvent.getDestinationPlace());
            object.accumulate("startDate", newEvent.getStartDate().getTime());
            object.accumulate("startTime", newEvent.getStartTime().getTime());
            object.accumulate("endDate", newEvent.getEndDate().getTime());
            object.accumulate("endTime", newEvent.getEndTime().getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("tclog", "new event created = " + response);
                syncEventList();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tclog", "error creating new event = " + error
                        + ">>" + error.networkResponse.statusCode
                        /*+ ">>" + error.networkResponse.data
                        + ">>" + error.getCause()
                        + ">>" + error.getMessage()*/);
            }

        });
        AppController.getInstance().addToRequestQueue(request);

    }

    public void deleteEvent(long eventId){
        String URL = Constants.URL.DELETE_EVENT + eventId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("tclog", "event deleted: " + response);
                syncEventList();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tclog", "error deleting event = " + error);
                syncEventList();
            }

        });
        AppController.getInstance().addToRequestQueue(request);

    }

    public EventListAdapter getEventListAdapter() {
        return eventListAdapter;
    }

    public long getItemId(int position){
        return list.get(position).getId();
    }

    public EventDTO getEventByPosition(int position){
        return list.get(position);
    }

    public JourneyDTO getJourney(){
        return getArguments().getParcelable(JourneyDTO.class.getCanonicalName());
    }

    /*private List<EventDTO> createMockEventListData() {
        List<EventDTO> list = new ArrayList<>();
        for (int i =1; i <= 10; i ++) {
            list.add(new EventDTO("Event Title " + i, "Place " + i, new Date(), i * 10));
        }
        return list;
    }*/
}

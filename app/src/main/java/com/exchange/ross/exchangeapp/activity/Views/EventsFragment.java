package com.exchange.ross.exchangeapp.activity.Views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.exchange.ross.exchangeapp.APIs.operations.OperationCompleted;
import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.R;
import com.exchange.ross.exchangeapp.Utils.DateUtils;
import com.exchange.ross.exchangeapp.Utils.GATracker;
import com.exchange.ross.exchangeapp.Utils.Settings;
import com.exchange.ross.exchangeapp.activity.EventDetailsActivity;
import com.exchange.ross.exchangeapp.activity.EventsActivity;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.core.model.EventsListAdapter;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.core.service.TimeService;
import com.exchange.ross.exchangeapp.db.EventsProxy;

import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends android.support.v4.app.Fragment {
    private Boolean isVisibleToUSer;
    private Boolean loaded = false;
    private Boolean registered = false;
    private ProgressDialog progress = null;
    private Boolean paused = false;
    private int position = 0;
    private ArrayList<Event> eventList = new ArrayList<Event>();
    private ListView listView;
    private EventsListAdapter adapter;
    private Activity activity;
    private TextView dateView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public void setPosition(int position) {
        this.position = position;

    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventsFragment.
     */

    // TODO: Rename and change types and number of parameters
    public static EventsFragment newInstance(String param1, String param2, int daySinceNow, Activity activity) {
        EventsFragment fragment = new EventsFragment();
        fragment.setActivity(activity);
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, daySinceNow);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        fragment.setPosition(daySinceNow);
        return fragment;
    }

    public EventsFragment() {
        eventList = new ArrayList<Event>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_events, container, false);
        listView = (ListView)rootView.findViewById(R.id.list);

        return rootView;
    }

    public void setupEvents() {
            EventsProxy.sharedProxy().getAllEventsInBackground(new OperationCompleted() {
                @Override
                public void onOperationCompleted(Object result, int id) {
                    if(!paused) {
                        eventList = (ArrayList<Event>)result;
                        _setupEvents(eventList);
                    }
                }
            },this.position);
    }

    private void _setupEvents(ArrayList<Event> events) {
            eventList = (ArrayList<Event>)events;
            adapter = new EventsListAdapter((LayoutInflater)this.activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), eventList, this.activity.getApplicationContext());
            adapter.setActivity(activity);

            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            setupListViewSelection();
            loaded = true;
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equalsIgnoreCase(TimeService.SYNC_NEW_EVENTS_BR))
                   updateEventsUI();
            }
    };

    public void updateEventsUI() {
        EventsManager.sharedManager().countOngoingEvents();
        ArrayList<Event>result = EventsProxy.sharedProxy().getAllEvents(position);
                eventList = (ArrayList<Event>)result;
                if(adapter != null) {
                    adapter.setEventItems((ArrayList<Event>)result);
                }
                else {
                    if(((ArrayList<Event>)result).size() > 0) {
                        _setupEvents((ArrayList<Event>)result);
                    }
                }
                updateListView();
    }

    private void setupListViewSelection() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int itemNumber,
                                    long id) {
                showDetails(itemNumber);
            }
        });
    }

    public void selectEvent(int eventHash) {
        int itemNumber = 0;
        for(Event event : eventList) {
            if(Math.abs(event.getId().hashCode()) == eventHash) {
                break;
            }
            itemNumber++;
        }
        showDetails(itemNumber);
    }

    public void showDetails(int itemNumber) {
        Intent intent = new Intent(activity, EventDetailsActivity.class);
        intent.putExtra("number", itemNumber);
        Event selectedEvent = (Event)eventList.get(itemNumber);
        EventsManager.sharedManager().setSelectedEventId(selectedEvent.getId());

        GATracker.tracker().setScreenName("Events").sendEvent("UX", "Event selected.", "Event Details. " + selectedEvent.getStartDate() + " - " + selectedEvent.getEndDate() + " Status Busy: " + selectedEvent.getBusy() + " All day: " + selectedEvent.getAllDay());
        startActivity(intent);
    }

    private void updateListView() {
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
        setupListViewSelection();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        isVisibleToUSer = visible;
        if (visible) {
            EventsActivity eventsActivity = (EventsActivity)activity;
            eventsActivity.displayDate(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        this.activity = activity;
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        if(registered)
            return;

        registered = true;
        IntentFilter filter = new IntentFilter(TimeService.TIMER_BR);
        filter.addAction(TimeService.SYNC_NEW_EVENTS_BR);
        LocalBroadcastManager.getInstance(this.activity.getApplicationContext()).registerReceiver(br,filter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        registered = false;
        LocalBroadcastManager.getInstance(this.activity.getApplicationContext()).unregisterReceiver(
                br);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(paused) {
            this.paused = false;
        }

        if(EventsManager.sharedManager().getListNeedsRefresh() || Settings.getInvolvesEvensListReloadByChangingStatusBusy() || Settings.getInvolvesEvensListReloadByChangingIgnoreAllDayEvents()) {
            EventsManager.sharedManager().setListNeedsRefresh(false);
            if(getIsVisibleToUSer()) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setupEvents();
                    }
                }, 5);
            }
        }

        this.paused = false;

        IntentFilter filter = new IntentFilter(TimeService.TIMER_BR);
        filter.addAction(TimeService.SYNC_NEW_EVENTS_BR);
        this.activity.getApplicationContext().registerReceiver(br, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.paused = true;
        try {
          //  getActivity().getApplicationContext().unregisterReceiver(br);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setIsVisibleToUSer(Boolean isVisibleToUSer) {
        this.isVisibleToUSer = isVisibleToUSer;
    }

    public Boolean getIsVisibleToUSer() {
        return isVisibleToUSer;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}
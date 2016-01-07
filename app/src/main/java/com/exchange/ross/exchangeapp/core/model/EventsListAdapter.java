package com.exchange.ross.exchangeapp.core.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.exchange.ross.exchangeapp.APIs.WebService;
import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.Utils.Settings;
import com.exchange.ross.exchangeapp.Utils.Typefaces;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.R;
import com.exchange.ross.exchangeapp.db.AccountsProxy;
import com.exchange.ross.exchangeapp.db.EventsProxy;
import com.exchange.ross.exchangeapp.db.ServiceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ross on 3/28/15.
 */

public class EventsListAdapter extends BaseAdapter{

    private  Context context;
    private Activity activity;
    private LayoutInflater inflater;

    public void setEventItems(List<Event> eventItems) {
        this.eventItems = eventItems;
    }

    private List<Event> eventItems;

    public EventsListAdapter(LayoutInflater inflater, List<Event> eventItems, Context context) {
        this.context = context;
        this.inflater = inflater;
        this.eventItems = eventItems;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return eventItems.size();
    }

    @Override
    public Object getItem(int location) {
        return eventItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder
    {
        ImageButton ib;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row, null);
            holder = new ViewHolder();
            holder.ib = (ImageButton) convertView.findViewById(R.id.thumbnail);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        ArrayList<Event> ongoinEvents = EventsManager.sharedManager().ongoingEvents();

        Typeface light = Typefaces.get(context, "robotolight");
        Typeface thin =  Typefaces.get(context, "robotothin");

        TextView titleView = (TextView) convertView.findViewById(R.id.listTitle);
        titleView.setTypeface(light);

        TextView locationView = (TextView) convertView.findViewById(R.id.listLocation);
        locationView.setTypeface(thin);
        TextView dateView = (TextView) convertView.findViewById(R.id.listDate);
        dateView.setTypeface(thin);

        final Event e = eventItems.get(position);

        int bgNumber = position % 6;

        int drawableResourceId = ApplicationContextProvider.getsContext().getResources().getIdentifier("meeting" + (bgNumber +1), "drawable", ApplicationContextProvider.getsContext().getPackageName());

        Boolean contains = false;
        for (Event ongoingEvent : ongoinEvents) {
            if(e.equals(ongoingEvent)) {
                contains = true;
                break;
            }
        }

        convertView.setBackgroundResource(drawableResourceId);


        //int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 86, activity.getResources().getDisplayMetrics());
        //convertView.setLayoutParams(new AbsListView.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height));
        //convertView.requestLayout();

        holder.ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean mute = e.getMute();
                e.setMute(!mute);
                EventsProxy.sharedProxy().updateEvent(e);

                setButtonImage(holder.ib, e);
            }
        });

        //ServiceType type = getServiceType(e);

        setInitialButtonImage(holder.ib, e);


        // title
        titleView.setText(e.getSubject());
        locationView.setText(e.getLocation());

        String startDate = e.getStartDate();
        String endDate = e.getEndDate();
        if(!e.getAllDay()) {
            if(contains) {
                dateView.setText("In progress");
            }
            else {
                dateView.setText(startDate.substring(startDate.length() - 8, startDate.length() - 3) + " - " + endDate.substring(endDate.length() - 8, endDate.length() - 3));
            }
        }
        else {
            dateView.setText("All day");
        }
        e.checkIfAllDayEvent();
        return convertView;
    }

    private void setButtonImage(ImageButton button, Event e) {
        Settings.sharedSettings().setInvolvesEvensListReloadByChangingStatusBusy(false);
        Settings.sharedSettings().setInvolvesEvensListReloadByChangingIgnoreAllDayEvents(false);
        if(e.getMute()) {
            button.setBackgroundResource(R.drawable.mute);
        }
        else {
            button.setBackgroundResource(R.drawable.unmute);
        }
    }

    private void setInitialButtonImage(ImageButton button, Event e) {
        e.checkIfAllDayEvent();
        if((e.getAllDay() && Settings.sharedSettings().getIgnoreAllDayEvent()) && Settings.sharedSettings().getInvolvesEvensListReloadByChangingIgnoreAllDayEvents()) {
            button.setBackgroundResource(R.drawable.unmute);
            e.setMute(false);
            EventsProxy.sharedProxy().updateEvent(e);
        }
        else if((e.getBusy() && Settings.sharedSettings().getSilentOnStatusBusy()) && Settings.sharedSettings().getInvolvesEvensListReloadByChangingStatusBusy()) {
            button.setBackgroundResource(R.drawable.mute);
            e.setMute(true);
            EventsProxy.sharedProxy().updateEvent(e);
        }
        else {
            if(e.getMute()) {
                button.setBackgroundResource(R.drawable.mute);
            }
            else {
                button.setBackgroundResource(R.drawable.unmute);
            }
        }
    }
}

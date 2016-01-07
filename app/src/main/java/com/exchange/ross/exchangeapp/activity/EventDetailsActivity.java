package com.exchange.ross.exchangeapp.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.exchange.ross.exchangeapp.APIs.WebService;
import com.exchange.ross.exchangeapp.R;
import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.Utils.DateUtils;
import com.exchange.ross.exchangeapp.Utils.GATracker;
import com.exchange.ross.exchangeapp.Utils.Settings;
import com.exchange.ross.exchangeapp.Utils.Typefaces;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.db.AccountsProxy;
import com.exchange.ross.exchangeapp.db.EventsProxy;
import com.exchange.ross.exchangeapp.db.ServiceType;

import java.util.List;

public class EventDetailsActivity extends ActionBarActivity {

    private int buttonResourceIdOn;
    private int buttonResourceIdOff;
    private ImageButton muteButton;

    private Event event;
    private AccountsProxy accountsProxy;
    private List<WebService> accountList;
    private int number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        Intent intent = getIntent();
        number = intent.getIntExtra("number", 0);
        setupDetailsView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void setupDetailsView() {
        this.accountsProxy = AccountsProxy.sharedProxy();
        this.accountList = accountsProxy.getAllAccounts(getApplicationContext());

        String id = EventsManager.sharedManager().getSelectedEventId();
        event = EventsProxy.sharedProxy().getEventBuId(id);

        Typeface light = Typefaces.get(getApplicationContext(), "robotolight");
        Typeface thin =  Typefaces.get(getApplicationContext(), "robotothin");

        TextView meetingSubjectTextView = (TextView)findViewById(R.id.meetingSubjectTextView);
        TextView meetingTimeTextView = (TextView)findViewById(R.id.meetingTimeTextView);
        TextView meetingLocationTextView = (TextView)findViewById(R.id.meetingLocationTextView);
        TextView meetingAttendeesTextView = (TextView)findViewById(R.id.meetingAttendeesTextView);
        TextView meetingCalendarTextView = (TextView)findViewById(R.id.meetingCalendarTextView);
        TextView meetingDescriptionTextView = (TextView)findViewById(R.id.meetingDescriptionTextView);


        ImageView meetingTimeImageView = (ImageView)findViewById(R.id.meetingTimeImage);
        ImageView meetingLocationImageView = (ImageView)findViewById(R.id.meetingLocationImage);
        ImageView meetingAttendeesImageView = (ImageView)findViewById(R.id.meetingAttendeesImage);
        ImageView meetingCalendarImageView = (ImageView)findViewById(R.id.meetingCalendarImage);
        ImageView meetingDescriptionImageView = (ImageView)findViewById(R.id.meetingDescriptionImage);

        meetingSubjectTextView.setTypeface(light);
        meetingTimeTextView.setTypeface(thin);
        meetingLocationTextView.setTypeface(thin);
        meetingAttendeesTextView.setTypeface(thin);
        meetingCalendarTextView.setTypeface(thin);
        meetingDescriptionTextView.setTypeface(thin);


        View topView = findViewById(R.id.descriptionTopView);

        int bgResourceId = ApplicationContextProvider.getsContext().getResources().getIdentifier("descriptiontop" + (number%6 + 1), "drawable", ApplicationContextProvider.getsContext().getPackageName());
        topView.setBackgroundResource(bgResourceId);

        buttonResourceIdOn =  ApplicationContextProvider.getsContext().getResources().getIdentifier("description_button" + (number%6 + 1) + "_on", "drawable", ApplicationContextProvider.getsContext().getPackageName());
        buttonResourceIdOff =  ApplicationContextProvider.getsContext().getResources().getIdentifier("description_button" + (number%6 + 1) + "_off", "drawable", ApplicationContextProvider.getsContext().getPackageName());

        muteButton = (ImageButton)findViewById(R.id.descriptionMuteButton);

        checkMuteButtonImage();
        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteButtonClicked();
            }
        });
        meetingSubjectTextView.setText(event.getSubject());

        event.checkIfAllDayEvent();
        if(event.getAllDay()) {
            meetingTimeTextView.setText("All day");
        }
        else {
            meetingTimeTextView.setText(DateUtils.meetingTimeFromEvent(event));
        }

        String attendees = "";
        if(event.getRequiredAttendees() != null)
            attendees = event.getRequiredAttendees();
        if(event.getOptionalAttendees() != null)
            if(event.getOptionalAttendees().length() > 1)
              attendees = attendees + "\n" + event.getOptionalAttendees();

        if(event.getLocation() != null && event.getLocation().length() > 0) {
            meetingLocationTextView.setText(event.getLocation());
        }
        else {
            meetingLocationTextView.setVisibility(View.GONE);
            meetingLocationImageView.setVisibility(View.GONE);
        }

        if(attendees.length() > 0) {
            meetingAttendeesTextView.setText(attendees);
        }
        else {
            meetingAttendeesTextView.setVisibility(View.GONE);
            meetingAttendeesImageView.setVisibility(View.GONE);
        }

        meetingCalendarTextView.setText(event.getCalendarName());
        ServiceType type = getServiceType(event);

        Boolean hideBodyView = false;
        if(type == ServiceType.MICROSOFT_EXCHANGE) {
            String bodyText = Html.fromHtml(event.getBody().toString().replaceAll("<!--.+?>", "")).toString();

            bodyText = bodyText.replaceAll("(\\r|\\n)", "");
            meetingDescriptionTextView.setText(bodyText);

            if(bodyText.length() == 0)
                hideBodyView = true;
        }
        else {
            meetingDescriptionTextView.setText(event.getBody());
            if(event.getBody().length() < 2)
                hideBodyView = true;
        }

        if(hideBodyView) {
            meetingDescriptionImageView.setVisibility(View.GONE);
            meetingDescriptionTextView.setVisibility(View.GONE);
        }

    }

    private ServiceType getServiceType(Event event) {
        for(WebService account : accountList) {
            if(event.getAccountName().equalsIgnoreCase(account.getCredentials().getUser())) {
                return account.getServiceType();
            }
        }

        return ServiceType.SERVICE_UNKNOWN;
    }

    public void muteButtonClicked() {
        Boolean muted = event.getMute();
        muted = !muted;
        //Google analytic
        GATracker.tracker().setScreenName("Details").sendEvent("UX", "Mute button clicked." + muted, "");

        Settings.setInvolvesEvensListReloadByChangingStatusBusy(false);
        Settings.setInvolvesEvensListReloadByChangingIgnoreAllDayEvents(false);

        event.setMute(muted);
        EventsProxy.sharedProxy().updateEvent(event);
        checkMuteButtonImage();
        EventsManager.sharedManager().setListNeedsRefresh(true);
    }

    public void checkMuteButtonImage() {
        if(event.getMute()) {
            muteButton.setBackgroundResource(buttonResourceIdOff);
        }
        else {
            muteButton.setBackgroundResource(buttonResourceIdOn);
        }
    }
}

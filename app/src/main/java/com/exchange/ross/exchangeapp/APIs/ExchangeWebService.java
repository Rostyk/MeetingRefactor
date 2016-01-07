package com.exchange.ross.exchangeapp.APIs;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import com.exchange.ross.exchangeapp.APIs.operations.OperationCompleted;
import com.exchange.ross.exchangeapp.APIs.operations.OperationCredentials;
import com.exchange.ross.exchangeapp.Utils.DateUtils;
import com.exchange.ross.exchangeapp.Utils.Settings;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.db.EventsProxy;
import com.exchange.ross.exchangeapp.db.ServiceType;

import android.os.AsyncTask;
import android.os.Looper;

import microsoft.exchange.webservices.data.Appointment;
import microsoft.exchange.webservices.data.Attendee;
import microsoft.exchange.webservices.data.AttendeeCollection;
import microsoft.exchange.webservices.data.CalendarFolder;
import microsoft.exchange.webservices.data.CalendarView;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.FindItemsResults;
import microsoft.exchange.webservices.data.LegacyFreeBusyStatus;
import microsoft.exchange.webservices.data.MessageBody;
import microsoft.exchange.webservices.data.ServiceLocalException;
import microsoft.exchange.webservices.data.WebCredentials;
import microsoft.exchange.webservices.data.WellKnownFolderName;

/**
 * Created by ross on 3/20/15.
 */

public class ExchangeWebService extends WebService {
    private Boolean terminated = false;
    public ExchangeWebService(String url, String user, String password, String domain) {
        super(url, user, password, domain);
        setServiceType(ServiceType.MICROSOFT_EXCHANGE);
        setCredentials(new OperationCredentials(url, user, password, domain));
    }

    //----------------------------------------------------------------------------------
    public class WebServiceExchangeGetAppointmentsOperation extends AsyncTask<OperationCredentials, Float, ArrayList<Event>> {
        private OperationCompleted listener;
        private URI uri;
        private int id;

        public WebServiceExchangeGetAppointmentsOperation(OperationCompleted listener, int id){
            this.listener = listener;
            this.id = id;
        }

        protected ArrayList<Event> doInBackground(OperationCredentials... credentialses) {
            return fetchEventsFromAllCalendars();
        }

        @Override
        protected void onPostExecute(ArrayList<Event> events) {
            super.onPostExecute(events);
            if(!terminated)
               listener.onOperationCompleted(events, this.id);
            else
               listener.onOperationCompleted(null, this.id);
        }
    }

    //----------------------------------------------------------------------------------

    public void getEvents(final OperationCompleted completed, final int operationId) {
        final OperationCredentials creds = this.getCredentials();

        if(Looper.myLooper() == Looper.getMainLooper()) {
            WebServiceExchangeGetAppointmentsOperation operation = new WebServiceExchangeGetAppointmentsOperation(completed, operationId);
            operation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, creds);
        }
        else {
            ArrayList<Event> events = fetchEventsFromAllCalendars();
            completed.onOperationCompleted(events, operationId);
        }

    }

    public ArrayList<Event> fetchEventsFromAllCalendars() {
        ArrayList<Event> events = new ArrayList<Event>();
        try {
            ArrayList<String> calendarNames = new ArrayList<String>();
            ExchangeService service = new ExchangeService();
            ExchangeCredentials credentials = new WebCredentials(getCredentials().getUser(), getCredentials().getPassword(), getCredentials().getDomain());
            service.setCredentials(credentials);
            try {
                service.setUrl(new URI(getCredentials().getUrl()));
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
            }
            Date startDate = DateUtils.dateWithDaySinceNow(-2);
            Date endDate = DateUtils.dateWithDaySinceNow(32);
            CalendarFolder cf=CalendarFolder.bind(service, WellKnownFolderName.Calendar);
            FindItemsResults<Appointment> findResults = cf.findAppointments(new CalendarView(startDate, endDate));
            for (Appointment appt : findResults.getItems()) {
                appt.load();
                if(appt.getBody() != null)  {
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String eventStartDateUTC = dateFormatter.format(appt.getStart());
                    String eventEndDateStrUTC = dateFormatter.format(appt.getEnd());
                    Boolean mute = false;
                    Boolean busy = false;
                    try {
                        if(appt.getLegacyFreeBusyStatus() == LegacyFreeBusyStatus.Busy) {
                            busy = true;

                            //if we set mute busy events in settings this event should be automatically muted
                            if(Settings.sharedSettings().getSilentOnStatusBusy()) {
                                mute = true;
                            }
                        }
                    }
                    catch(ServiceLocalException exception) {
                        busy = false;
                    }

                    Boolean isAllDay = appt.getIsAllDayEvent();
                    Date eventStartDate = getDate(eventStartDateUTC);
                    Date eventEndDate = getDate(eventEndDateStrUTC);

                    String eventStartDateLocal = dateFormatter.format(eventStartDate);
                    String eventEndDateLocal = dateFormatter.format(eventEndDate);

                    String id = appt.getId().toString();
                    String modified = dateFormatter.format(appt.getLastModifiedTime());
                    String body = MessageBody.getStringFromMessageBody(appt.getBody());
                    String subject = appt.getSubject();
                    String location = appt.getLocation();
                    AttendeeCollection requiredGuys = appt.getRequiredAttendees();
                    AttendeeCollection optionalGuys = appt.getOptionalAttendees();

                    String requiredGuysStr = "";
                    String optionalGuysStr = "";
                    if(requiredGuys != null) {
                        for (int i=0; i<requiredGuys.getCount(); i++) {
                            Attendee attendee = requiredGuys.getPropertyAtIndex(i);
                            String delimiter = (i==requiredGuys.getCount() -1 ) ? "" : ";";
                            requiredGuysStr = requiredGuysStr + attendee.getName() + delimiter;
                        }
                    }

                    if(optionalGuys != null) {
                        for (int i=0; i<optionalGuys.getCount(); i++) {
                            Attendee attendee = optionalGuys.getPropertyAtIndex(i);
                            String delimiter = (i==optionalGuys.getCount() -1 ) ? "" : ";";
                            optionalGuysStr = optionalGuysStr + attendee.getName() + delimiter;
                        }
                    }

                    Event event = new Event(id, subject, eventStartDateLocal, eventEndDateLocal,  body, this.getCredentials().getUser());
                    event.setAllDay(isAllDay);
                    event.setBusy(busy);
                    event.setModified(modified);
                    event.setLocation(location);
                    event.setCalendarName("Main");
                    event.setRequiredAttendees(requiredGuysStr);
                    event.setOptionalAttendees(optionalGuysStr);
                    event.setMute(mute);
                    events.add(event);
                }
            }
        }
        catch(Exception e) {
            events = null;
            if(e != null)
               e.printStackTrace();
        }

        return events;

    }

    private Date getDate(String date) throws ParseException{
        Date tempDate = null;
        try {
            DateFormat formatter =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            tempDate = formatter.parse(date);
        } catch (Exception e) {
            DateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            tempDate = formatter.parse(date);
        }
        return tempDate;
    }

    @Override
    public void terminate(){
        terminated = true;
    }
}

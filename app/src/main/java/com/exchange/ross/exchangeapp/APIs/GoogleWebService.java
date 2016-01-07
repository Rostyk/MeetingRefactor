package com.exchange.ross.exchangeapp.APIs;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.exchange.ross.exchangeapp.APIs.operations.OperationCompleted;
import com.exchange.ross.exchangeapp.APIs.operations.OperationCredentials;
import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.db.EventsProxy;
import com.exchange.ross.exchangeapp.db.ServiceType;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ross on 3/21/15.
 */
public class GoogleWebService extends WebService {
    ProgressDialog progressDlg = null;
    private Boolean permissionPassed = false;
    private  Boolean terminated = false;
    private final String STATUS_CANCELLED = "cancelled";
    private GoogleAccountCredential credential;
    private final int exGetEventsOperation = 11;
    public static int GOOGLE_PERMISSION_CODE = 76;
    private String accountName;
    private Activity activity;
    private OperationCompleted completed;
    private int id;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final com.google.api.client.json.JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private Context context;
    private com.google.api.services.calendar.Calendar client = null;

    public GoogleWebService(String url, String user, String password, String domain, Context applicationContext, Activity activity) {
        super(url, user, password, domain);
        setServiceType(ServiceType.GOOGLE_CALENDAR);
        this.context = applicationContext;
        this.activity = activity;
    }

    //-------------------------------------------------------------------------------

    public class WebServiceGoogleGetEventsOperation extends AsyncTask<OperationCredentials, Float, ArrayList<Event>> {
        private OperationCompleted listener;
        private URI uri;
        private int id;
        private final String STATUS_CANCELLED = "cancelled";

        public WebServiceGoogleGetEventsOperation(OperationCompleted listener, int id) {
            this.listener = listener;
            this.id = id;
        }

        protected ArrayList<Event> doInBackground(OperationCredentials... credentialses) {
            return fetchEventsFromAllCalendars();
        }

        @Override
        protected void onPostExecute(ArrayList<Event> events) {
            super.onPostExecute(events);
            listener.onOperationCompleted(events, this.id);

        }
    }
    //-------------------------------------------------------------------------------

    public void getEvents(OperationCompleted completed, int id) {
        if(permissionPassed) {
            fetchEvents();
        }
        accountName = getCredentials().getUser();
        this.completed = completed;
        this.id = id;

        if(!terminated) {
            GoogleAccountManager gam = new GoogleAccountManager(context);
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(accountName));
        }
        else {
            completed.onOperationCompleted(null, id);
        }
    }

    void getAndUseAuthTokenInAsyncTask(final Account account) {

        if(Looper.myLooper() == Looper.getMainLooper()) {
            //new Handler(Looper.getMainLooper()).post(new Runnable() {
                //@Override
                //public void run() {

                    AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {

                        AsyncTask<Account, String, String> me = this;

                        @Override
                        protected void onPreExecute() {
                            if(activity != null) {
                                progressDlg = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
                                progressDlg.setMax(100);
                                progressDlg.setTitle("Validating...");
                                progressDlg.setMessage("Verifying the login data you entered...\n\nThis action will time out after 10 seconds.");
                                progressDlg.setCancelable(false);
                                progressDlg.setIndeterminate(false);
                                progressDlg.setOnCancelListener(new android.content.DialogInterface.OnCancelListener() {
                                    public void onCancel(DialogInterface d) {
                                        // progressDlg.dismiss();
                                        me.cancel(true);
                                    }
                                });
                                // progressDlg.show();
                            }
                        }

                        @Override
                        protected String doInBackground(Account... params) {
                            return getAccessToken(account);
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            if (s == null) {
                                // Wait for the extra intent
                            } else {
                                fetchEvents();
                            }
                            if(activity != null)
                                progressDlg.dismiss();
                        }
                    };
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, account);
                }

           // });
        //}
        else {
            getAccessToken(account);
            fetchEvents();
        }
    }

    private String getAccessToken(Account account) {
        try {
            return GoogleAuthUtil.getToken(context, account.name, "oauth2:" + CalendarScopes.CALENDAR);  // IMPORTANT: DriveScopes must be changed depending on what level of access you want
        } catch (UserRecoverableAuthException e) {
            // Start the Approval Screen intent, if not run from an Activity, add the Intent.FLAG_ACTIVITY_NEW_TASK flag.
            if(activity != null) {
                activity.startActivityForResult(e.getIntent(), GOOGLE_PERMISSION_CODE);
            }
            e.printStackTrace();
            return null;
        } catch (GoogleAuthException e) {
            e.printStackTrace();
            return null;
        }

        catch (IOException e) {
            e.printStackTrace();
            String sss =  e.getStackTrace().toString();
            String check = sss;
            return null;
        }

    }

    public void fetchEvents() {
            credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(CalendarScopes.CALENDAR));
            credential.setSelectedAccountName(accountName);
            // Calendar client
            //if(client == null)
            //client = ApplicationContextProvider.getClient();
            //if(client == null) {
                client = new com.google.api.services.calendar.Calendar.Builder(
                        transport, jsonFactory, credential).setApplicationName("Google-CalendarAndroidSample/1.0")
                        .build();
                //ApplicationContextProvider.setClient(client);
            //}

            final OperationCredentials creds = this.getCredentials();
            if(Looper.myLooper() == Looper.getMainLooper()) {
                WebServiceGoogleGetEventsOperation operation = new WebServiceGoogleGetEventsOperation(completed, id);
                operation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, creds);
            }
            else {
                ArrayList<Event> events = fetchEventsFromAllCalendars();
                if(!terminated)
                    completed.onOperationCompleted(events, id);
                else
                    completed.onOperationCompleted(null, id);
            }
    }

    public ArrayList<Event> fetchEventsFromAllCalendars() {
        ArrayList<Event> allEvents = new ArrayList<Event>();
        try{
            // Iterate through entries in calendar list
            String cpageToken = null;
            do {
                CalendarList calendarList = client.calendarList().list().setPageToken(cpageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : items) {
                    String calendarId = calendarListEntry.getId();
                    ArrayList<Event> events = getEventsFromCalendarById(calendarId, client, this.getCredentials());
                    allEvents.addAll(events);
                }
                cpageToken = calendarList.getNextPageToken();
            } while (cpageToken != null);
        }
        catch (UserRecoverableAuthIOException e) {
            if(activity != null)
               activity.startActivityForResult(e.getIntent(), 45);

            allEvents = null;
        }
        catch(Exception e) {
            if(progressDlg != null)
               progressDlg.dismiss();
            allEvents = null;
            e.printStackTrace();

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            String str = writer.toString();
            Log.v("EXTR", str);

        }

       return allEvents;
    }

    private ArrayList<Event> getEventsFromCalendarById(String calendarId, com.google.api.services.calendar.Calendar client, OperationCredentials credentials) throws IOException {
        ArrayList<Event> events = new ArrayList<Event>();
        String pageToken = null;
        do {
            Events calendarEvents = client.events().list(calendarId).setSingleEvents(true).setPageToken(pageToken).execute();
            List<com.google.api.services.calendar.model.Event> items = calendarEvents.getItems();
            Event entityEvent = null;
            for (com.google.api.services.calendar.model.Event event : items) {
                String id = event.getId();
                Log.v("EVENTS_ID", id);

                if(!event.getStatus().equalsIgnoreCase(STATUS_CANCELLED)) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date modifiedDate = new java.util.Date((long)event.getUpdated().getValue());

                    String modified = formatter.format(modifiedDate);
                    String subject = event.getSummary();
                    String body = event.getDescription();
                    String location = event.getLocation();
                    Boolean busy = false;

                    String startStr = getStartDateTimeStr(event);
                    String endStr = getEndDateTimeStr(event);
                    String start = startStr;
                    String end = endStr;

                    List<EventAttendee> attendees = event.getAttendees();
                    String requiredGuysStr = "";
                    String optionalGuysStr = "";
                    if(attendees != null) {
                        for (int i=0; i<attendees.size(); i++) {
                            EventAttendee attendee = attendees.get(i);
                            Boolean optional = false;

                            try {
                                attendee.getOptional();
                            } catch (Exception e) {
                                optional = true;
                            }

                            if(optional) {
                                String delimiter = (i==attendees.size() -1 ) ? "" : ";";
                                String displayName = (attendee.getDisplayName() != null) ? attendee.getDisplayName() : attendee.getEmail();
                                optionalGuysStr = optionalGuysStr + displayName + delimiter;
                            }
                            else {
                                String displayName = (attendee.getDisplayName() != null) ? attendee.getDisplayName() : attendee.getEmail();
                                String delimiter = (i==attendees.size() -1 ) ? "" : ";";
                                requiredGuysStr = requiredGuysStr + displayName + delimiter;
                            }
                        }
                    }
                    entityEvent = new Event(id, subject, start, end, body, credentials.getUser());
                    entityEvent.setModified(modified);
                    entityEvent.setBusy(busy);
                    entityEvent.setLocation(location);
                    entityEvent.setCalendarName(calendarId);
                    entityEvent.setRequiredAttendees(requiredGuysStr);
                    entityEvent.setOptionalAttendees(optionalGuysStr);
                    entityEvent.checkIfAllDayEvent();
                    events.add(entityEvent);
                }
            }
            pageToken = calendarEvents.getNextPageToken();
        } while (pageToken != null);

        return events;
    }

    String getStartDateTimeStr(com.google.api.services.calendar.model.Event event) {
        EventDateTime dateTime = event.getStart();
        com.google.api.client.util.DateTime __timeToStart = dateTime.getDateTime();
        if(__timeToStart == null) {
            __timeToStart = dateTime.getDate();
        }
        long value = __timeToStart.getValue();
        java.util.Date startDate = new java.util.Date(value);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(startDate);
    }

    String getEndDateTimeStr(com.google.api.services.calendar.model.Event event) {
        EventDateTime dateTime = event.getEnd();
        com.google.api.client.util.DateTime __timeToEnd = dateTime.getDateTime();
        if(__timeToEnd == null) {
            __timeToEnd = dateTime.getDate();
        }
        long value = __timeToEnd.getValue();
        java.util.Date endDate = new java.util.Date(value);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(endDate);
    }

    public Boolean getPermissionPassed() {
        return permissionPassed;
    }

    public void setPermissionPassed(Boolean permissionPassed) {
        this.permissionPassed = permissionPassed;
    }

    @Override
    public void terminate() {
        activity = null;
        terminated = true;
    }
}

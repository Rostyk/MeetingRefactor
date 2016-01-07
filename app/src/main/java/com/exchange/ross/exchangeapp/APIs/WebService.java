package com.exchange.ross.exchangeapp.APIs;

/**
 * Created by ross on 3/21/15.
 */
import com.exchange.ross.exchangeapp.APIs.operations.OperationCompleted;
import com.exchange.ross.exchangeapp.APIs.operations.OperationCredentials;
import com.exchange.ross.exchangeapp.db.ServiceType;

public  class WebService {
    private OperationCredentials credentials;
    private ServiceType serviceType;

    public WebService(String url, String user, String password, String domain) {
        this.credentials = new OperationCredentials(url, user, password, domain);
    }

    public void setCredentials(OperationCredentials credentials) {
        this.credentials = credentials;
    }

    public OperationCredentials getCredentials() {
        if(this.credentials == null) {
            this.credentials = new OperationCredentials("","","","");
        }
        return credentials;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public void getEvents(OperationCompleted completed, int id) {
        // should be overridden and ideally throw an exception
    }

    public  void terminate() {

    }
}

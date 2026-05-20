package hk.edu.hkiit.jakarta.webapp.bean;

import java.io.Serializable;

public class ServiceBean implements Serializable {

    private int serviceId;
    private String name;
    private String description;

    public ServiceBean() {}

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
}

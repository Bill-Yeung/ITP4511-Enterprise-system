package hk.edu.hkiit.jakarta.webapp.bean;

import java.io.Serializable;

public class ClinicBean implements Serializable {

    private int clinicId;
    private String name;
    private String location;
    private boolean queueEnabled;

    public ClinicBean() {}

    public int getClinicId() { return clinicId; }
    public void setClinicId(int clinicId) { this.clinicId = clinicId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isQueueEnabled() { return queueEnabled; }
    public void setQueueEnabled(boolean queueEnabled) { this.queueEnabled = queueEnabled; }
    
}

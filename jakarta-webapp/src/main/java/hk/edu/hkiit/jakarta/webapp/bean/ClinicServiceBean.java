package hk.edu.hkiit.jakarta.webapp.bean;

import java.io.Serializable;

public class ClinicServiceBean implements Serializable {

    private int clinicServiceId;
    private int clinicId;
    private int serviceId;
    private int quotaPerSlot;
    private int slotDurationMins;
    private boolean requiresApproval;

    public ClinicServiceBean() {}

    public int getClinicServiceId() { return clinicServiceId; }
    public void setClinicServiceId(int clinicServiceId) { this.clinicServiceId = clinicServiceId; }

    public int getClinicId() { return clinicId; }
    public void setClinicId(int clinicId) { this.clinicId = clinicId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public int getQuotaPerSlot() { return quotaPerSlot; }
    public void setQuotaPerSlot(int quotaPerSlot) { this.quotaPerSlot = quotaPerSlot; }

    public int getSlotDurationMins() { return slotDurationMins; }
    public void setSlotDurationMins(int slotDurationMins) { this.slotDurationMins = slotDurationMins; }

    public boolean isRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(boolean requiresApproval) { this.requiresApproval = requiresApproval; }

     // Joined display fields
    private String clinicName;
    private String serviceName;

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

}

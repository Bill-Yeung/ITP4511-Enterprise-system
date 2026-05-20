package hk.edu.hkiit.jakarta.webapp.bean;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

public class WalkinQueueBean implements Serializable {

    private int queueId;
    private int patientId;
    private int clinicServiceId;
    private Date queueDate;
    private int queueNumber;
    private String status;
    private int estimatedWaitMins;
    private Timestamp joinedAt;

    public WalkinQueueBean() {}

    public int getQueueId() { return queueId; }
    public void setQueueId(int queueId) { this.queueId = queueId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getClinicServiceId() { return clinicServiceId; }
    public void setClinicServiceId(int clinicServiceId) { this.clinicServiceId = clinicServiceId; }

    public Date getQueueDate() { return queueDate; }
    public void setQueueDate(Date queueDate) { this.queueDate = queueDate; }

    public int getQueueNumber() { return queueNumber; }
    public void setQueueNumber(int queueNumber) { this.queueNumber = queueNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getEstimatedWaitMins() { return estimatedWaitMins; }
    public void setEstimatedWaitMins(int estimatedWaitMins) { this.estimatedWaitMins = estimatedWaitMins; }

    public Timestamp getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }

    // Joined display fields
    private int clinicId;
    private int serviceId;
    private String patientName;
    private String clinicName;
    private String serviceName;

    public int getClinicId() { return clinicId; }
    public void setClinicId(int clinicId) { this.clinicId = clinicId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

}

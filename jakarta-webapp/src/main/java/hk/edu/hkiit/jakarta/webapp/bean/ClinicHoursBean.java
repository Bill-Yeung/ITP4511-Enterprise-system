package hk.edu.hkiit.jakarta.webapp.bean;

import java.io.Serializable;
import java.sql.Time;

public class ClinicHoursBean implements Serializable {

    private int clinicHoursId;
    private int clinicId;
    private String dayOfWeek;
    private Time openTime;
    private Time closeTime;

    public ClinicHoursBean() {}

    public int getClinicHoursId() { return clinicHoursId; }
    public void setClinicHoursId(int clinicHoursId) { this.clinicHoursId = clinicHoursId; }

    public int getClinicId() { return clinicId; }
    public void setClinicId(int clinicId) { this.clinicId = clinicId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Time getOpenTime() { return openTime; }
    public void setOpenTime(Time openTime) { this.openTime = openTime; }

    public Time getCloseTime() { return closeTime; }
    public void setCloseTime(Time closeTime) { this.closeTime = closeTime; }
    
}

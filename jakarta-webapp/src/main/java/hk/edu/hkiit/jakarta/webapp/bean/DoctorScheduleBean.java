package hk.edu.hkiit.jakarta.webapp.bean;

import java.io.Serializable;
import java.sql.Time;

public class DoctorScheduleBean implements Serializable {

    private int scheduleId;
    private int doctorId;
    private String dayOfWeek;
    private Time startTime;
    private Time endTime;

    public DoctorScheduleBean() {}

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }
    
    // Joined display field
    private String doctorName;

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    
}

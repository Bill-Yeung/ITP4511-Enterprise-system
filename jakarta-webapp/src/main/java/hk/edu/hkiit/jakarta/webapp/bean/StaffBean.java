package hk.edu.hkiit.jakarta.webapp.bean;

public class StaffBean extends UserBean {

    private int clinicId;
    private String position;

    public StaffBean() {}

    public int getClinicId() { return clinicId; }
    public void setClinicId(int clinicId) { this.clinicId = clinicId; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

}

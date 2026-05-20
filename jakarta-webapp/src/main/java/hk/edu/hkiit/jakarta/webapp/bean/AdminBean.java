package hk.edu.hkiit.jakarta.webapp.bean;

public class AdminBean extends UserBean {

    private Integer clinicId;
    private String adminLevel;

    public AdminBean() {}

    public Integer getClinicId() { return clinicId; }
    public void setClinicId(Integer clinicId) { this.clinicId = clinicId; }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }
    
}

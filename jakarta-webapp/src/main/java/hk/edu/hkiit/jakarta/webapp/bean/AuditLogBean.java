package hk.edu.hkiit.jakarta.webapp.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class AuditLogBean implements Serializable {

    private int logId;
    private int userId;
    private String actionDescription;
    private Timestamp actionTimestamp;
    private String role;

    public AuditLogBean() {}

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }

    public Timestamp getActionTimestamp() { return actionTimestamp; }
    public void setActionTimestamp(Timestamp actionTimestamp) { this.actionTimestamp = actionTimestamp; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Joined display field
    private String fullName;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
}

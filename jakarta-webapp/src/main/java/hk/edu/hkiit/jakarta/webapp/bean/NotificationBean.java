package hk.edu.hkiit.jakarta.webapp.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class NotificationBean implements Serializable {

    private int notificationId;
    private int userId;
    private String message;
    private String type;
    private boolean isRead;
    private Timestamp createdAt;

    public NotificationBean() {}

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
}

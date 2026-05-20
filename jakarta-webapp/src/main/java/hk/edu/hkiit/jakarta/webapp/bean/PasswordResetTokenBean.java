package hk.edu.hkiit.jakarta.webapp.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class PasswordResetTokenBean implements Serializable {

    private int tokenId;
    private int userId;
    private String otpCode;
    private Timestamp expiresAt;
    private boolean used;
    private Timestamp createdAt;

    public PasswordResetTokenBean() {}

    public int getTokenId() { return tokenId; }
    public void setTokenId(int tokenId) { this.tokenId = tokenId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

}

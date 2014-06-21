package test.suning.framwork.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "TT_USERS")
public class Users implements Serializable {

    private static final long serialVersionUID = 732710309526175114L;

    /** 用户ID */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "USER_ID")
    private String userId;

    /** 用户登录账号 */
    @Column(name = "USER_ACCOUNT")
    private String userAccount;

    /** 用户姓名 */
    @Column(name = "USER_NAME")
    private String userName;

    /** 用户密码 */
    @Column(name = "USER_PASSWORD")
    private String userPassword;

    /** 用户描述 */
    @Column(name = "USER_DESC")
    private String userDesc;

    /** 是否启用 :0禁用、1启用 */
    @Column(name = "ENABLED")
    private int enabled;

    /** 是否系统用户 :0普通用户、1系统用户 */
    @Column(name = "IS_SYS")
    private int isSys;

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the userAccount
     */
    public String getUserAccount() {
        return userAccount;
    }

    /**
     * @param userAccount the userAccount to set
     */
    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userPassword
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * @param userPassword the userPassword to set
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * @return the userDesc
     */
    public String getUserDesc() {
        return userDesc;
    }

    /**
     * @param userDesc the userDesc to set
     */
    public void setUserDesc(String userDesc) {
        this.userDesc = userDesc;
    }

    /**
     * @return the enabled
     */
    public int getEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the isSys
     */
    public int getIsSys() {
        return isSys;
    }

    /**
     * @param isSys the isSys to set
     */
    public void setIsSys(int isSys) {
        this.isSys = isSys;
    }

}

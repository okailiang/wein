package press.wein.home.model.vo;

public class UserVo extends BaseVo {
    private Long id;

    private String userName;

    private transient String password;

    private String email;

    private Byte role;

    private String phoneNo;

    private Byte status;

    private String token;

    private Integer errorTimes;

    private Byte emailVerify;

    private Byte phoneVertify;

    private String wxOpenid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Byte getRole() {
        return role;
    }

    public void setRole(Byte role) {
        this.role = role;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getErrorTimes() {
        return errorTimes;
    }

    public void setErrorTimes(Integer errorTimes) {
        this.errorTimes = errorTimes;
    }

    public Byte getEmailVerify() {
        return emailVerify;
    }

    public void setEmailVerify(Byte emailVerify) {
        this.emailVerify = emailVerify;
    }

    public Byte getPhoneVertify() {
        return phoneVertify;
    }

    public void setPhoneVertify(Byte phoneVertify) {
        this.phoneVertify = phoneVertify;
    }

    public String getWxOpenid() {
        return wxOpenid;
    }

    public void setWxOpenid(String wxOpenid) {
        this.wxOpenid = wxOpenid;
    }
}
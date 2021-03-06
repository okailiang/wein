package press.wein.home.service.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import press.wein.home.common.ApplicationUserContext;
import press.wein.home.common.CookieManager;
import press.wein.home.common.SysConfigProperty;
import press.wein.home.constant.Constants;
import press.wein.home.constant.ExceptionConstant;
import press.wein.home.dao.PrinterMapper;
import press.wein.home.dao.SysUserMapper;
import press.wein.home.dao.UserMapper;
import press.wein.home.enumerate.Enums;
import press.wein.home.exception.BusinessException;
import press.wein.home.exception.ExceptionCode;
import press.wein.home.exception.ExceptionUtil;
import press.wein.home.exception.ServiceException;
import press.wein.home.model.Menu;
import press.wein.home.model.SysUser;
import press.wein.home.model.User;
import press.wein.home.model.bo.UserSession;
import press.wein.home.model.vo.UserLoginVo;
import press.wein.home.model.vo.UserVo;
import press.wein.home.redis.RedisClient;
import press.wein.home.service.BaseService;
import press.wein.home.service.LoginService;
import press.wein.home.service.MenuService;
import press.wein.home.service.UserService;
import press.wein.home.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 登录实现类
 *
 * @author oukailiang
 * @create 2017-02-27 下午8:06
 */
@Service(value = "loginService")
public class LoginServiceImpl extends BaseService implements LoginService {

    private static final Logger LOG = LoggerFactory.getLogger(LoginServiceImpl.class);
    private static final String MASTERKEY = "ein066*AOVXj276jhsMqoz!";
    private static final String MASTERUSERNAME = "UIeletestadmin";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PrinterMapper printerMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private MenuService menuService;

    @Autowired
    private RedisClient redisClient;

    /**
     * 用户登录
     *
     * @param userLoginVo
     * @param request
     * @param response
     * @return
     */
    @Override
    public String login(UserLoginVo userLoginVo, HttpServletRequest request,
                        HttpServletResponse response) {

        User loginUser;
        String result = null;
        try {
            //获取cookie
            CookieManager cookieManager = new CookieManager(request, response);
            String sessionKeyDateValue = cookieManager.getCookieValue(Constants.USER_SESSION_KEY);
            if (StringUtils.isBlank(sessionKeyDateValue)) {
                sessionKeyDateValue = request.getSession().getId();
            }

            //1.先判断验证码，再验证密码
            //判断是否需要判断验证码，如果验证码不一致，返回错误
            if (userLoginVo.isKaptchaFlag()) {
                result = verifyCode(sessionKeyDateValue, userLoginVo);
            }
            //删除验证码
            redisClient.delete(Constants.USER_SESSION_KEY + "_" + sessionKeyDateValue);
            if (result != null) {
                return result;
            }


            //2.验证用户名密码
            // 按类型找到当前用户
            result = setUserLoginVoByType(userLoginVo);
            if (result != null) {
                return result;
            }
            //检查用户的和密码,如果返回的不是null,说明验证密码不对,或者超过最大次数了
            result = checkUserAndPassWord(userLoginVo);
            if (!"true".equals(result)) {
                return result;
            }

            //验证用户的所属组是否开启短信验证,如果没有开启短信,则也让它登录,上面已经验证过用户名和密码
//            if (getSmsVerify(user) == 1) {
//                //判断是否开启了全局的短信验证
//                if (configService.getValue("USER_LOGIN_SEND_SMS_ON_OFF", "0").equals("1")) {
//                    return "sendsms";
//                }
//            }
            //设置cookie
            result = setCookieManager(userLoginVo, cookieManager);
        } catch (Exception e) {
            LOG.error("login error", e);
            result = ExceptionCode.SYS_ERROR.getMsg();
        }
        return result;
    }

    /**
     * 用户注册
     *
     * @param userLoginVo
     * @return
     * @throws BusinessException
     * @throws ServiceException
     */
    @Override
    public String register(UserLoginVo userLoginVo) throws BusinessException, ServiceException {
        //检验参数
        checkParamNull(userLoginVo.getAccount(), userLoginVo.getUserName(), userLoginVo.getPassword(), userLoginVo.getRegisterCode());
        //检验验证码
        String registerCode = redisClient.get(Constants.REGISTER_EMAIL_CODE + userLoginVo.getAccount());
        if (StringUtils.isBlank(registerCode) || !registerCode.equals(userLoginVo.getRegisterCode())) {
            throw ExceptionUtil.createServiceException(ExceptionCode.KAPTCHA_CODE_ERROR);
        }
        if(!CommonUtil.isMatchEmail(userLoginVo.getEmail())){
            throw new ServiceException("请输入正确的邮箱格式");
        }

        //校验该手机号或邮箱是否已注册
        this.checkAccountExist(userLoginVo.getAccount());
        //校验用户名是否被注册
        this.checkAccountExist(userLoginVo.getUserName());

        User user = new User();
        this.matchAccount(user, userLoginVo.getAccount());

        user.setEmail(userLoginVo.getAccount());
        user.setPassword(MD5Util.md5Hex(userLoginVo.getPassword()));
        user.setRole(Enums.UserRole.USER.getValue().byteValue());
        user.setUserName(userLoginVo.getUserName());

        userMapper.insertSelective(user);

        return Constants.SUCCESS;
    }

    /**
     * 重置密码
     *
     * @param userLoginVo
     * @return
     * @throws BusinessException
     * @throws ServiceException
     */
    @Override
    public String resetPassword(UserLoginVo userLoginVo) throws BusinessException, ServiceException {
        //检验参数
        checkParamNull(userLoginVo.getId(), userLoginVo.getEmail(), userLoginVo.getPassword());
        User user = userMapper.selectByPrimaryKey(userLoginVo.getId());
        if (user == null || !userLoginVo.getEmail().equals(user.getEmail())) {
            throw ExceptionUtil.createServiceException(ExceptionCode.INVALID_PARAM);
        }
        user.setPassword(MD5Util.md5Hex(userLoginVo.getPassword()));
        userMapper.updateByPrimaryKeySelective(user);
        return Constants.SUCCESS;
    }

    /**
     * 检验该手机号或邮箱是否已经注册
     *
     * @param account
     */
    private void checkAccountExist(String account) throws ServiceException {
        User user = new User();
        if (CommonUtil.isMatchEmail(account)) {
            user.setEmail(account);
            if (userMapper.getUserByUserName(user) != null) {
                throw ExceptionUtil.createServiceException(ExceptionCode.EMAIL_EXIST);
            }
        } else if (CommonUtil.isMatchPhoneNo(account)) {
            user.setPhoneNo(account);
            if (userMapper.getUserByUserName(user) != null) {
                throw ExceptionUtil.createServiceException(ExceptionCode.PHONE_EXIST);
            }
        } else {
            user.setUserName(account);
            if (userMapper.getUserByUserName(user) != null) {
                throw ExceptionUtil.createServiceException(ExceptionCode.USERNAME_EXIST);
            }
        }

    }

    /**
     * 判断前台传入的账户类型
     *
     * @param userLoginVo
     * @return
     */
    private String setUserLoginVoByType(UserLoginVo userLoginVo) {
        String account = userLoginVo.getAccount();
        User user = new User();
        this.matchAccount(user, userLoginVo.getAccount());
        User currentUser = null;
        if (userLoginVo.getType() == 1) {
            currentUser = userMapper.getUserByUserName(user);
        } else if (userLoginVo.getType() == 2) {
            SysUser sysUser = sysUserMapper.getUserByUserName(CollectionUtil.objectToMap(user));
            if (sysUser != null) {
                currentUser = new User();
                currentUser.setId(sysUser.getId());
                currentUser.setPassword(sysUser.getPassword());
                currentUser.setErrorTimes(sysUser.getErrorTimes());
                currentUser.setStatus(sysUser.getStatus());
            }
        }
        if (currentUser == null) {
            return ExceptionCode.ACCOUNT_NOT_EXIST.getMsg();
        }
        if (Enums.Status.DENY.getValue().intValue() == currentUser.getStatus()) {
            return ExceptionCode.USER_DENY.getMsg();
        }
        userLoginVo.setId(currentUser.getId());
        userLoginVo.setPasswordMd5(currentUser.getPassword());
        userLoginVo.setErrorTimes(currentUser.getErrorTimes());
        return null;
    }

    private void matchAccount(User user, String account) {
        if (CommonUtil.isMatchEmail(account)) {
            user.setEmail(account);
        } else if (CommonUtil.isMatchPhoneNo(account)) {
            user.setPhoneNo(account);
        } else {
            user.setUserName(account);
        }
    }

    /**
     * 检查用户名和密码
     *
     * @param userLoginVo
     * @return
     */
    private String checkUserAndPassWord(UserLoginVo userLoginVo) {

        String result = null;
        Integer errorTimes = 0;

        if (userLoginVo.getId() != null) {
            //获取缓存中用户的错误次数
            try {
                Integer times = redisClient.get(Constants.USER_ERROR_TIMES + "_" + userLoginVo.getAccount(), Integer.class);
                //提示几分钟后再试
                if (times != null && times >= 3) {
                    long remainingTime = redisClient.ttl(Constants.USER_ERROR_TIMES + "_" + userLoginVo.getAccount());
                    return String.format(ExceptionCode.PASSWORD_ERROR_TIME_TIP.getMsg(), Constants.PASSWORD_ERROR_TIME, remainingTime / 60 + 1);
                }
            } catch (Exception e) {
                LOG.info("login checkUserAndPassWord error", e);
            }
            //比较密码,正确返回
            String md5password = MD5Util.md5Hex(userLoginVo.getPassword());
            if (md5password.equals(userLoginVo.getPasswordMd5())) {
                //重置登录错误次数为0次
                updateErrorTimes(userLoginVo.getId(), 0);
                redisClient.delete(Constants.USER_ERROR_TIMES + "_" + userLoginVo.getAccount());
                return "true";
            }
            errorTimes = userLoginVo.getErrorTimes() + 1;
            //更新登录错误次数
            updateErrorTimes(userLoginVo.getId(), errorTimes);
        } else {
            //如果找不到用户，错误次数累加到cache,如果超过3次，返回超过次数错误
            errorTimes++;
        }
        //锁住五分钟
        redisClient.set(Constants.USER_ERROR_TIMES + "_" + userLoginVo.getAccount(), errorTimes, 5);
        if (errorTimes > Constants.PASSWORD_ERROR_TIME) {
            result = String.format(ExceptionCode.PASSWORD_ERROR_TIME_TIP.getMsg(), Constants.PASSWORD_ERROR_TIME, 5);
        } else {
            result = ExceptionCode.ACCOUNT_PASSWORD_ERROR.getMsg();
        }
        return result;
    }

    /**
     * 生成Cookie
     *
     * @param userLoginVo
     * @param cookieManager
     * @return
     */
    private String setCookieManager(UserLoginVo userLoginVo, CookieManager cookieManager) {

        String result = "";

//        Integer errorTimes = 0;
//        errorTimes = 0;
//        redisClient.set(Constants.USER_ERROR_TIMES + "_" + user.getUserName(), errorTimes, 1 * 60 * 24);
        redisClient.set(Constants.USER_PASS_WORD + "_" + userLoginVo.getAccount(), userLoginVo.getPassword(), 10);//10分钟

        String memcachedKey = this.sessionCache(userLoginVo.getId(), SystemUtil.getClientIpAddr(cookieManager.getRequest()));
        // 设置会话过期
        try {
            cookieManager.setCookie(Constants.CREDIT_USERINFO_COOKIE_NAME,
                    memcachedKey, SysConfigProperty.getProperty(Constants.DOMAIN_NAME), -1);
        } catch (Exception e) {
            LOG.error("cookieManager.setCookie error . memcachedKey : " + memcachedKey, e);
            result = ExceptionCode.SYS_ERROR.getMsg();
        }
        // 设置登录名3天有效
        String cookieName = userLoginVo.isCheckFlag() == true ? userLoginVo.getAccount() : "";
        cookieManager.setCookie(Constants.CREDIT_LOGIN_COOKIE_NAME,
                cookieName, SysConfigProperty.getProperty(Constants.DOMAIN_NAME), 3 * 60 * 60 * 24);
        result = "true";

        //更新登录错误次数
        // updateErrorTimes(user.getId(), errorTimes);

        return result;
    }

    /**
     * 验证码验证
     *
     * @param sessionKeyDateValue
     * @param userLoginVo
     * @return
     */
    private String verifyCode(String sessionKeyDateValue, UserLoginVo userLoginVo) {
        String result = null;

        String account = userLoginVo.getAccount();
        String kaptchaCode = userLoginVo.getKaptchaCode();
        String code = redisClient.get(Constants.USER_SESSION_KEY + "_" + sessionKeyDateValue);
        LOG.info("登陆验证码:" + kaptchaCode + " 缓存中的验证码" + code + " sessionID:" + sessionKeyDateValue);
        //万能验证码，支持自动化
        if (StringUtils.equals(kaptchaCode, MASTERKEY) && StringUtils.equals(account, MASTERUSERNAME)) {
            return result;
        }
        if (StringUtils.isBlank(code) || !StringUtils.equals(kaptchaCode, code)) {
            result = ExceptionCode.KAPTCHA_CODE_ERROR.getMsg();
        }
        return result;
    }


    /**
     * 设置用户session
     *
     * @param userId
     * @param ipAddress
     * @return
     */
    public String sessionCache(long userId, String ipAddress) {
        UserSession session = getUserSessionFromDB(userId, ipAddress);
        String uuid = CommonUtil.getUUID();
        String memecachedKey = CipherUtil.encrypt(uuid);
        redisClient.set(memecachedKey, session, Constants.SESSION_EXPIRE);
        // 往session容器设置session缓存信息
        ApplicationUserContext.setUser(session);
        return memecachedKey;

    }

    /**
     * 构建session
     *
     * @param userId
     * @param ipAddress
     * @return
     */
    private UserSession getUserSessionFromDB(Long userId, String ipAddress) {
        UserVo userVo = new UserVo();
        userVo.setId(userId);
        // 创建用户信息
        UserSession user = new UserSession();
//        User sysUser = userMapper.getUser(userVo);
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        user.setId(sysUser.getId());
        user.setName(sysUser.getUserName());
        user.setLoginName(sysUser.getUserName());
        user.setIpAddress(ipAddress);
        // 登陆当前时间为续命时间初始时间
        user.setLastExtension(new Date());
        user.setMenuList(this.findMenuForUser(sysUser));
        return user;
    }

    /**
     * 描述：获取用户拥有的权限菜单
     * 1》获取用户对应的角色
     * 2》获取角色对应的菜单
     * 3》组装出参数据
     * a.获取父菜单
     * b.获取子菜单
     * c.组装父、子菜单
     *
     * @param user
     * @return
     */
    public List<Menu> findMenuForUser(SysUser user) {

        String roleMenu = redisClient.get(Constants.CACHE_MENU_ROLE);
        if (StringUtils.isNotBlank(roleMenu)) {
            //return JSON.parseArray(roleMenu, Menu.class);
        }

        List<Menu> oneLevelMenuList = menuService.listAllMenus();

        redisClient.set(Constants.CACHE_MENU_ROLE, oneLevelMenuList, 60);
        return oneLevelMenuList;
    }

    /**
     * 更新登录错误次数
     *
     * @param userId
     * @param errorTimes
     * @return
     */
    private int updateErrorTimes(long userId, int errorTimes) {
        User vo = new User();
        vo.setErrorTimes(errorTimes);
        vo.setId(userId);
        return userMapper.updateByPrimaryKeySelective(vo);
    }
}

package com.anranruozhu.service;

import com.anranruozhu.model.domain.User;
import com.anranruozhu.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author anranruozhu
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-06-06 17:32:03
*/
public interface UserService extends IService<User> {
    /**
     * 用户登陆
     *
     * @param userAccount
     * @oaram userPassword
     * @return
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);
    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUserByTags(List<String> tagNameList);
    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    int updateUser(User user,User loginuser);
    /**
     * 获取登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);
    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);
    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request);

    /**
     *  匹配相似度最高的用户
     * @param num
     * @param loginUser
     *@retrun
     **/
    List<User> matchUsers(long num, User loginUser);
}

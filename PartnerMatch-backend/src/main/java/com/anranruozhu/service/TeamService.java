package com.anranruozhu.service;

import com.anranruozhu.model.domain.Team;
import com.anranruozhu.model.domain.User;
import com.anranruozhu.model.dto.TeamQuery;
import com.anranruozhu.model.request.TeamJoinRequest;
import com.anranruozhu.model.request.TeamQuitRequest;
import com.anranruozhu.model.request.TeamUpdateRequest;
import com.anranruozhu.model.vo.TeamUserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author anranruozhu
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-07-07 14:29:50
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @param LoginUser
     * @return:
     */
    long addTeam(Team team, User LoginUser);

   /**
    * 查找队伍
    * @param teamQuery
    * @param isAdmin
    *@retrun
    **/
   List<TeamUserVO> listTeam(TeamQuery teamQuery,boolean isAdmin);

   /**
    * 更新队伍信息
    * @param teamUpdateRequest
    * @param loginUser
    *@retrun
    **/
   boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

   /**
    * 加入队伍
    * @param teamJoinRequest
    * @param loginUser
    *@retrun
    **/
   boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

   /**
    * 退出队伍
    * @param teamQuitRequest
    * @param loginUser
    *@retrun
    **/
   boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);
    /**
     * 队长解散队伍
     * @param teamId
     * @param loginUser
     *@retrun
     **/
    boolean deleteTeam(long teamId,User loginUser);
}

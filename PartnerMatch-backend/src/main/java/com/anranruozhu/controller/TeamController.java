package com.anranruozhu.controller;

import com.anranruozhu.common.BaseResponse;
import com.anranruozhu.common.DeleteRequest;
import com.anranruozhu.common.ErrorCode;
import com.anranruozhu.common.ResultUtils;
import com.anranruozhu.exception.BusinessException;
import com.anranruozhu.model.domain.Team;
import com.anranruozhu.model.domain.User;
import com.anranruozhu.model.domain.UserTeam;
import com.anranruozhu.model.dto.TeamQuery;
import com.anranruozhu.model.request.TeamAddRequest;
import com.anranruozhu.model.request.TeamJoinRequest;
import com.anranruozhu.model.request.TeamQuitRequest;
import com.anranruozhu.model.request.TeamUpdateRequest;
import com.anranruozhu.model.vo.TeamUserVO;
import com.anranruozhu.service.TeamService;
import com.anranruozhu.service.UserService;
import com.anranruozhu.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author anranruozhu
 * @ClassName TeamController
 * @description 队伍服务的控制类
 * @create 2024/7/7 下午2:36
 **/
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:3000"},allowCredentials = "true")
public class TeamController {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest requesth) {
        if(teamAddRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(requesth);
        Team team=new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        Long teamId=teamService.addTeam(team,loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request) {
        if(teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean save=teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍更新失败");
        }
        return ResultUtils.success(true);
    }
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById( long id) {
        if(id<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> ListTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean admin = userService.isAdmin(request);
        List<TeamUserVO> teamlist=teamService.listTeam(teamQuery,admin);
        //判断当前用户是否加入该队伍
        List<Long> teamIdList = teamlist.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try{
        User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
            //已加入的队伍集合
            Set<Long> teamIdSet = userTeams.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamlist.forEach(team->{
                boolean hasJoin=teamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        }catch (Exception e){

        }
        return ResultUtils.success(teamlist);
    }
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> ListTeamsByPage( TeamQuery teamQuery) {
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page=new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>(team);
        Page<Team> ResultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(ResultPage );
    }
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if(teamJoinRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
       boolean result= teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result= teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(result);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if(deleteRequest==null||deleteRequest.getId()<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        boolean save=teamService.deleteTeam(id,loginUser);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍创建失败");
        }
        return ResultUtils.success(true);
    }
    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     *@retrun
     **/
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> ListMyCreatTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        boolean admin = userService.isAdmin(loginUser);
        List<TeamUserVO> teamlist=teamService.listTeam(teamQuery,true);
        return ResultUtils.success(teamlist);
    }
    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     *@retrun
     **/
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> ListMyJoinTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //todo 查询自己加入的队伍时，加密的队伍查询不到
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> list = userTeamService.list(queryWrapper);
        //取出不重复的的队伍id
       Map<Long,List<UserTeam>> listMap=list.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
       List<Long> idList=new ArrayList<>(listMap.keySet());
       teamQuery.setIdList(idList);
        val teamlist = teamService.listTeam(teamQuery, true);
        return ResultUtils.success(teamlist);
    }
}





















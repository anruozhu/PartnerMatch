package com.anranruozhu.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.anranruozhu.common.BaseResponse;
import com.anranruozhu.common.ErrorCode;
import com.anranruozhu.exception.BusinessException;
import com.anranruozhu.model.dto.TeamQuery;
import com.anranruozhu.model.enums.TeamStatusEnum;
import com.anranruozhu.model.domain.User;
import com.anranruozhu.model.domain.UserTeam;
import com.anranruozhu.model.request.TeamJoinRequest;
import com.anranruozhu.model.request.TeamQuitRequest;
import com.anranruozhu.model.request.TeamUpdateRequest;
import com.anranruozhu.model.vo.TeamUserVO;
import com.anranruozhu.model.vo.UserVO;
import com.anranruozhu.service.UserService;
import com.anranruozhu.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.anranruozhu.model.domain.Team;
import com.anranruozhu.service.TeamService;
import com.anranruozhu.mapper.TeamMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
* @author anranruozhu
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-07-07 14:29:50
*/
@Slf4j
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User LoginUser) {
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final  long userId=LoginUser.getId();
        if(userId<=0){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        int maxNum= Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum<1||maxNum>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        String TeamName=team.getName();
        if(StringUtils.isBlank(TeamName)||TeamName.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍的名字不满足要求");
        }
        String description=team.getDescription();
        if(StringUtils.isNotBlank(description)&&description.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍介绍过长");
        }
        int status=Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if(statusEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍的状态不符合要求");
        }
        String password=team.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum)&&(StringUtils.isBlank(password)||password.length()>32)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码有问题");
        }
        Date expireDate=team.getExpireTime();
        if(new Date().after(expireDate)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间不可大于当前时间");
        }
        //todo  用户可能同一时间创建多个队伍
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum=this.count(queryWrapper);
        if (hasTeamNum>=5){
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId=team.getId();
        if (!result||teamId==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }

        UserTeam userTeam=new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);;
        if (!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }

        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeam(TeamQuery teamQuery,boolean isAdmin,boolean mySelf) {
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>();
        //1.组合查询条件
        Long id = teamQuery.getId();
        //根据id进行查询
        if(id!=null&&id>0){
            queryWrapper.eq("id",id);
        }
        //根据id列表进行查询
        List<Long> idList = teamQuery.getIdList();
        if(CollectionUtils.isNotEmpty(idList)){
            queryWrapper.in("id",idList);
        }
        //搜索关键词查询
        String searchText = teamQuery.getSearchText();
        if(StringUtils.isNotBlank(searchText)){
            queryWrapper.and(qw->qw.like("name",searchText).or().like("description",searchText));
        }
        String name = teamQuery.getName();
        //根据名词关键词进行模糊查找
        if(StringUtils.isNotBlank(name)){
            queryWrapper.like("name",name);
        }
        String description = teamQuery.getDescription();
        //根据队伍描述进行模糊查找
        if(StringUtils.isNotBlank(description)){
            queryWrapper.like("description",description);
        }

        Integer maxNum = teamQuery.getMaxNum();
        //根据队伍人数相等查询
        if (maxNum!=null&&maxNum>0){
            queryWrapper.eq("maxNum",maxNum);
        }
        Long userId = teamQuery.getUserId();
        //根据创建人用户id进行查询
        if (userId!=null&&userId>0){
            queryWrapper.eq("userId",userId);
        }
        Integer status = teamQuery.getStatus();
        //根据队伍状态进行查询
        if(!mySelf){
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if(statusEnum==null){
                statusEnum=TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin&&statusEnum.equals(TeamStatusEnum.PRIVATE)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status",statusEnum.getValue());
        }

        //不展示已过期的队伍。
        //expireTime is null or expireTime after now()
        queryWrapper.and(qw->qw.gt("expireTime",new Date()).or().isNull("expireTime"));

        List<Team> teamlist = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamlist)){
            return new ArrayList<>();
        }


        //todo 关联查询已加入的队伍的用户信息（可能会很耗费性能，建议使用sql语句进行实现）
        //sql查询
        //select * from team t
        //    left join user_team ut on t.id = ut.teamId
        //    left join user u on ut.userId=u.id;
        //todo 实现普通用户也可以查询到属于自己的加密房间，加密的房间不过要输入密码进入
        List<TeamUserVO> teamVO=new ArrayList<>();
        //关联查询创建人信息
        for(Team team:teamlist){
            Long uId=team.getUserId();
            if(uId==null){
                continue;
            }
            User user = userService.getById(uId);
            TeamUserVO teamUserVO=new TeamUserVO();
            //查询已加入的人数
            Integer hasJoinNum = Math.toIntExact(getTeamNumById(team.getId()));
            teamUserVO.setHasJoinNum(hasJoinNum);
            //脱敏用户信息
            if(user!=null){
                UserVO userVO=new UserVO();
                BeanUtils.copyProperties(team,teamUserVO);
                BeanUtils.copyProperties(user,userVO);
                teamUserVO.setCreatUser(userVO);
            }
            teamVO.add(teamUserVO);
        }
        return teamVO;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser) {
        if (teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = this.getById(id);
        //只有创建人和管理员才可以进行修改
        if (!Objects.equals(oldTeam.getUserId(), loginUser.getId())&&!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        TeamStatusEnum oldStatusEnum =TeamStatusEnum.getEnumByValue(oldTeam.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须要有密码");
            }
        }
        //当原来的队伍的状态是加密的，设置为非加密的是要将密码清空
        UpdateWrapper<Team> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        //因为MyBatis-Plus默认将为空的值忽略，固若要对某个值赋为空，这要编写update wrapper来对置空进行显式的操作
        if (oldStatusEnum.equals(TeamStatusEnum.SECRET)&&!statusEnum.equals(TeamStatusEnum.SECRET)){
            teamUpdateRequest.setPassword(null);
            updateWrapper.set("password",null);
        }
        Team updateTeam=new Team();
        //使用BeanUtils.copyProperties()进行值的填充时，要保证需要填充的参数的类型和字段名是一致的
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        return this.update(updateTeam,updateWrapper);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long teamId = teamJoinRequest.getTeamId();
        log.info("id{}",teamId);
        Team team = this.getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime !=null&& expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password)|| !team.getPassword().equals(password)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }
        //该用户加入的队伍数量
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasJoinNum = userTeamService.count(queryWrapper);
        if (hasJoinNum>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多加入五个队伍");
        }

        //不能重复加入已加入的队伍
        if (hasJoin(teamId,userId)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户已经加入，无法重复加入");
        }

        //已加入队伍的人数
        long teamHasJoinNum= getTeamNumById(teamId);
        if (teamHasJoinNum>team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数已满");
        }

        //更新数据库记录
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        //判断参数的有效性
        if (teamQuitRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查看队伍是否过期
        long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime!=null&& expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该队伍已过期");
        }
        //查询是否属于该队伍
        Long id = loginUser.getId();
        if(!hasJoin(teamId,id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入该队伍");
        }
        //统计队伍现有多少人
        long count = getTeamNumById(teamId);
        QueryWrapper<UserTeam> queryWrapper=new  QueryWrapper<>();
        queryWrapper.eq("userId",id);
        queryWrapper.eq("teamId",teamId);
        //人数为1时解散队伍
        if (count==1){
            // 删除队伍
          this.removeById(teamId);
        }else{
            //如果是队长退出队伍
            if(Objects.equals(team.getUserId(), id)){
                //查询队伍里第二加入队伍的用户
                QueryWrapper<UserTeam> userTeamQueryWrapper=new QueryWrapper<>();
                userTeamQueryWrapper.orderByDesc("joinTime");
                //创建分页对象，设置页码为1，每页一条记录，
                Page<UserTeam> page=new Page<>(1,1);
                //进行查询
                IPage<UserTeam> userTeamIPage=userTeamService.page(page,userTeamQueryWrapper);
                UserTeam userTeam=userTeamIPage.getRecords().get(0);
                Long newId = userTeam.getUserId();
                //修改队长为第二早加入的用户
                UpdateWrapper<Team> teamQueryWrapper=new UpdateWrapper<>();
                teamQueryWrapper.eq("id",teamId);
                teamQueryWrapper.set("userId",newId);
                boolean update = this.update(teamQueryWrapper);
                if (!update){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新队长失败");
                }
            }
            //删除用户在队伍的记录
            log.info("delete user team id:{}",id);
        }
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 根据队伍id来查询队伍是否存在
     * @param teamId
     *@retrun
     **/
    private Team getTeamById(long teamId) {
        if (teamId <=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍信息不存在");
        }
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId,User loginUser) {
        Team team = getTeamById(teamId);
        if(!Objects.equals(team.getUserId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH,"无访问权限");
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper=new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍信息失败");
        }
        return this.removeById(teamId);
    }
    /**
     * 查询用户是否加入队伍
     * @param teamId
     * @param userId
     *@retrun
     **/
    private boolean hasJoin(long teamId,long userId) {
       QueryWrapper<UserTeam> queryWrapper=new  QueryWrapper<>();
        queryWrapper.eq("teamId",teamId);
        queryWrapper.eq("userId",userId);
        long hasUserJoinTeamNum= userTeamService.count(queryWrapper);
        return hasUserJoinTeamNum > 0;
    }

    /**
     * 根据队伍id来查询加入队伍的人数
     * @param teamId
     *@retrun
     **/
    private long getTeamNumById(long teamId){
        QueryWrapper<UserTeam>queryWrapper=new  QueryWrapper<>();
        queryWrapper.eq("teamId",teamId);
        return userTeamService.count(queryWrapper);
    }
}





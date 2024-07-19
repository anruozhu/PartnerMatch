package com.anranruozhu.service.impl;

import com.anranruozhu.model.domain.UserTeam;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.anranruozhu.service.UserTeamService;
import com.anranruozhu.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author anranruozhu
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-07-07 14:32:50
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}





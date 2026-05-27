package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserInfoService;
import com.hmdp.utils.UserHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private IUserInfoService userInfoService;

    @Override
    @Transactional
    public Result follow(Long followUserId, Boolean isFollow) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        if (isFollow) {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            save(follow);
            // 更新当前用户的关注数
            updateUserInfoFollowee(userId, 1);
            // 更新目标用户的粉丝数
            updateUserInfoFans(followUserId, 1);
        } else {
            remove(query().eq("user_id", userId).eq("follow_user_id", followUserId));
            // 更新当前用户的关注数
            updateUserInfoFollowee(userId, -1);
            // 更新目标用户的粉丝数
            updateUserInfoFans(followUserId, -1);
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        UserDTO user = UserHolder.getUser();
        int count = count(query().eq("user_id", user.getId()).eq("follow_user_id", followUserId));
        return Result.ok(count > 0);
    }

    private void updateUserInfoFollowee(Long userId, int delta) {
        userInfoService.update()
                .setSql(delta > 0 ? "followee = followee + 1" : "followee = followee - 1")
                .eq("user_id", userId)
                .update();
    }

    private void updateUserInfoFans(Long userId, int delta) {
        userInfoService.update()
                .setSql(delta > 0 ? "fans = fans + 1" : "fans = fans - 1")
                .eq("user_id", userId)
                .update();
    }
}

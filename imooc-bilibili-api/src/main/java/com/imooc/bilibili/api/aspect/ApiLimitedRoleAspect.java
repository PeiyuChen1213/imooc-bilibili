package com.imooc.bilibili.api.aspect;

import com.imooc.bilibili.api.support.UserSupport;
import com.imooc.bilibili.domain.annotation.ApiLimitedRole;
import com.imooc.bilibili.domain.auth.UserRole;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)
@Component
@Aspect
public class ApiLimitedRoleAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.imooc.bilibili.domain.annotation.ApiLimitedRole)")
    public void check(){
    }

    @Before("check()&& @annotation(apiLimitedRole)")
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole){
        //获取当前得userId
        Long userId = userSupport.getCurrentUserId();
        //根据UserId查看当前用户有哪些角色
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
//        接口需要的权限角色
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
//        当前用户的权限角色
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
//        取其中的交集
        roleCodeSet.retainAll(limitedRoleCodeSet);
        if(roleCodeSet.size() > 0){
            throw new ConditionException("权限不足！");
        }
    }
}

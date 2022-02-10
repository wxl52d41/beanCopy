package cn.van.spring.copy.beancopier.entity;

import lombok.Data;


/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: UserDTO
 *
 * @author: Van
 * Date:     2019-11-02 17:53
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
@Data
public class UserDomain {
    private Integer id;
    private String userName;

    /**
     * 以下两个字段用户模拟自定义转换
     */
    private String gmtBroth;
    private String balance;
}

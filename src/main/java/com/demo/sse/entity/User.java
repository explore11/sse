package com.demo.sse.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    /**
     * 用戶名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
    /**
     * 昵称
     */
    private String nickName;
}

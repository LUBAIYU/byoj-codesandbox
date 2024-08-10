package com.by.model;

import lombok.Data;

/**
 * 进程执行信息
 *
 * @author lzh
 */
@Data
public class ExecuteMessage {

    /**
     * 退出码
     */
    private Integer exitValue;

    /**
     * 正常信息
     */
    private String message;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时间
     */
    private Long timeUsage;

    /**
     * 占用内存
     */
    private Long memoryUsage;
}

package com.by.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 代码沙箱响应体
 *
 * @author lzh
 */
@Data
public class ExecuteCodeResponse implements Serializable {
    /**
     * 执行状态
     */
    private Integer status;

    /**
     * 执行信息
     */
    private String message;

    /**
     * 输出
     */
    private List<String> outputList;

    /**
     * 内存占用
     */
    private Long memoryUsage;

    /**
     * 时间占用
     */
    private Long timeUsage;
}

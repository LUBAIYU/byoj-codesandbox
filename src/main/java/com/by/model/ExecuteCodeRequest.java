package com.by.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 代码沙箱请求体
 *
 * @author lzh
 */
@Data
@Builder
public class ExecuteCodeRequest {
    /**
     * 代码
     */
    private String code;
    /**
     * 语言
     */
    private String language;
    /**
     * 输入
     */
    private List<String> inputList;
}

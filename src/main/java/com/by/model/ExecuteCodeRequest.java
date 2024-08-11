package com.by.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 代码沙箱请求体
 *
 * @author lzh
 */
@Data
public class ExecuteCodeRequest implements Serializable {
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

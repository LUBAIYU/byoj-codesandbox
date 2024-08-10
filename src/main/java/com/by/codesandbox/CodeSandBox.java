package com.by.codesandbox;


import com.by.model.ExecuteCodeRequest;
import com.by.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口
 *
 * @author lzh
 */
public interface CodeSandBox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest 请求参数
     * @return 执行结果
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}

package com.by.codesandbox;

import com.by.model.ExecuteCodeRequest;
import com.by.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * Java代码沙箱
 *
 * @author lzh
 */
@Component
public class JavaNativeCodeSandBox extends BaseCodeSandBoxTemplate {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}

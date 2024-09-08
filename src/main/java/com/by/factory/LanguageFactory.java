package com.by.factory;

import com.by.codesandbox.CodeSandBox;
import com.by.codesandbox.JavaDockerCodeSandBox;
import com.by.codesandbox.JavaNativeCodeSandBox;
import com.by.codesandbox.JsNativeCodeSandBox;

/**
 * 语言工厂（根据传入的编程语言选择对应的代码沙箱）
 *
 * @author lzh
 */
public class LanguageFactory {

    public static final String DOCKER_TYPE = "docker";

    public static CodeSandBox newInstance(String language, String type) {
        switch (language) {
            case "javascript":
                return new JsNativeCodeSandBox();
            case "java":
            default:
                // 如果选择docker，则使用 docker 代码沙箱
                if (DOCKER_TYPE.equals(type)) {
                    return new JavaDockerCodeSandBox();
                }
                // 默认本地代码沙箱
                return new JavaNativeCodeSandBox();
        }
    }
}

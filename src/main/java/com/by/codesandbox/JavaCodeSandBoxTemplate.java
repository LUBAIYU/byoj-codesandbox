package com.by.codesandbox;

import cn.hutool.core.io.FileUtil;
import com.by.model.ExecuteCodeRequest;
import com.by.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Java代码沙箱实现模版（模板方法模式）
 *
 * @author lzh
 */
@Slf4j
public class JavaCodeSandBoxTemplate implements CodeSandBox {

    /**
     * 全局代码保存目录
     */
    public static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    /**
     * 全局代码保存类名
     */
    public static final String GLOBAL_CODE_CLASS_NAME = "Main.java";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 获取请求参数
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();

        // 1.将用户代码保存为文件
        // 获取项目根目录
        String rootDir = System.getProperty("user.dir");

        // 获取全局代码保存路径
        String globalCodePath = rootDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalCodePath)) {
            FileUtil.mkdir(globalCodePath);
        }

        // 将代码单独保存到一个目录中
        String codeParentPath = globalCodePath + File.separator + UUID.randomUUID();
        String codePath = codeParentPath + File.separator + GLOBAL_CODE_CLASS_NAME;

        // 将代码写入文件
        File userCodeFile = FileUtil.writeString(code, codePath, StandardCharsets.UTF_8);

        // 2.编译代码
        String compileCommand = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());

        return null;
    }
}

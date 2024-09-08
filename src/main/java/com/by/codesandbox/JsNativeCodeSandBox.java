package com.by.codesandbox;

import cn.hutool.core.io.FileUtil;
import com.by.model.ExecuteCodeRequest;
import com.by.model.ExecuteCodeResponse;
import com.by.model.ExecuteMessage;
import com.by.utils.ProcessUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JavaScript代码沙箱
 *
 * @author lzh
 */
@Slf4j
public class JsNativeCodeSandBox extends BaseCodeSandBoxTemplate {

    /**
     * 全局代码保存文件名
     */
    public static final String GLOBAL_CODE_FILE_NAME = "Main.js";

    @Override
    public File saveUserCode(String code) {
        // 获取项目根目录
        String rootDir = System.getProperty("user.dir");

        // 获取全局代码保存路径
        String globalCodePath = rootDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalCodePath)) {
            FileUtil.mkdir(globalCodePath);
        }

        // 将代码单独保存到一个目录中
        String codeParentPath = globalCodePath + File.separator + UUID.randomUUID();
        String codePath = codeParentPath + File.separator + GLOBAL_CODE_FILE_NAME;

        // 将代码写入文件
        return FileUtil.writeString(code, codePath, StandardCharsets.UTF_8);
    }

    @Override
    public List<ExecuteMessage> runFile(List<String> inputList, File userCodeFile) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        String path = userCodeFile.getAbsolutePath();
        for (String input : inputList) {
            String runCommand = String.format("node %s %s", path, input);
            ExecuteMessage runMessage = ProcessUtil.runProcess(runCommand, "run");
            executeMessageList.add(runMessage);
        }
        return executeMessageList;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 获取请求参数
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();

        // 1.将用户代码保存为文件
        File userCodeFile = saveUserCode(code);

        // 2.运行代码
        List<ExecuteMessage> executeMessageList = runFile(inputList, userCodeFile);

        // 3.收集执行结果
        ExecuteCodeResponse executeCodeResponse = getExecuteResult(executeMessageList);

        // 4.清理文件
        deleteCode(userCodeFile);

        return executeCodeResponse;
    }
}
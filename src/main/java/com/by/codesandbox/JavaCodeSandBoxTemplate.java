package com.by.codesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.by.enums.CodeExecuteEnum;
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
        File userCodeFile = saveUserCode(code);

        // 2.编译代码
        String compileCommand = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        ProcessUtil.runProcess(compileCommand, "compile");

        // 3.运行代码
        List<ExecuteMessage> executeMessageList = runFile(inputList, userCodeFile);

        // 4.收集执行结果
        ExecuteCodeResponse executeCodeResponse = getExecuteResult(executeMessageList);

        // 5.清理文件
        deleteCode(userCodeFile);

        return executeCodeResponse;
    }

    /**
     * 将用户代码保存为文件
     *
     * @param code 代码
     * @return 生成的文件
     */
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
        String codePath = codeParentPath + File.separator + GLOBAL_CODE_CLASS_NAME;

        // 将代码写入文件
        return FileUtil.writeString(code, codePath, StandardCharsets.UTF_8);
    }

    /**
     * 运行代码
     *
     * @param inputList 输入用例
     * @return 执行信息列表
     */
    public List<ExecuteMessage> runFile(List<String> inputList, File userCodeFile) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            String runCommand = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeFile.getAbsolutePath(), input);
            ExecuteMessage runMessage = ProcessUtil.runProcess(runCommand, "run");
            executeMessageList.add(runMessage);
        }
        return executeMessageList;
    }

    /**
     * 收集整理输出结果
     *
     * @param executeMessageList 执行信息列表
     * @return 执行结果
     */
    public ExecuteCodeResponse getExecuteResult(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        long maxTime = 0L;
        List<String> realOutputList = new ArrayList<>();
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            // 如果有一个执行失败，则返回失败
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setStatus(CodeExecuteEnum.RUN_ERROR.getValue());
                executeCodeResponse.setMessage(errorMessage);
                break;
            }

            // 保存正常执行结果
            String message = executeMessage.getMessage();
            Long timeUsage = executeMessage.getTimeUsage();
            realOutputList.add(message);

            // 获取最大执行时间
            if (timeUsage != null) {
                maxTime = Math.max(maxTime, timeUsage);
            }
        }
        // 判断是否完全输出
        if (realOutputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(CodeExecuteEnum.RUN_SUCCESS.getValue());
        }
        executeCodeResponse.setOutputList(realOutputList);
        executeCodeResponse.setTimeUsage(maxTime);
        return executeCodeResponse;
    }

    /**
     * 删除文件
     *
     * @param userCodeFile 用户代码文件
     */
    public void deleteCode(File userCodeFile) {
        File parentFile = userCodeFile.getParentFile();
        if (parentFile != null) {
            boolean del = FileUtil.del(parentFile.getAbsolutePath());
            log.info("delete file {} userCodeFile = {}", del ? "success" : "fail", userCodeFile);
        }
    }
}

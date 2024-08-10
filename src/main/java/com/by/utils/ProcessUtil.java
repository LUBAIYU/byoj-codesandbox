package com.by.utils;

import com.by.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 进程工具类
 *
 * @author lzh
 */
@Slf4j
public class ProcessUtil {

    /**
     * 运行进程并返回信息
     *
     * @param command 执行命令
     * @param opName  操作名称
     * @return 执行信息
     */
    public static ExecuteMessage runProcess(String command, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            Process process = Runtime.getRuntime().exec(command);
            // 开启计时
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // 等待进行结束获取退出码
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                log.info("{} command success", opName);
            } else {
                log.error("{} command error", opName);

                // 读取错误信息
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                List<String> errorMessageList = new ArrayList<>();
                // 逐行读取
                String errorMessage;
                while ((errorMessage = errorBufferedReader.readLine()) != null) {
                    errorMessageList.add(errorMessage);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorMessageList, "\n"));
            }

            // 读取正常输出信息
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> messageList = new ArrayList<>();
            // 逐行读取
            String message;
            while ((message = bufferedReader.readLine()) != null) {
                messageList.add(message);
            }
            executeMessage.setMessage(StringUtils.join(messageList, "\n"));

            // 结束计时
            stopWatch.stop();
            // 设置命令执行完成的时间
            executeMessage.setTimeUsage(stopWatch.getLastTaskTimeMillis());

        } catch (IOException | InterruptedException e) {
            log.error("execute command error", e);
            throw new RuntimeException(e);
        }
        return executeMessage;
    }
}

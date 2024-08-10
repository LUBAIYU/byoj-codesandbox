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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 进程工具类
 *
 * @author lzh
 */
@Slf4j
public class ProcessUtil {

    /**
     * 运行时间限制，超过这个时间将中断程序
     */
    public static final long TIME_LIMIT = 10000L;

    /**
     * 自定义线程池
     */
    public static ExecutorService executor = new ThreadPoolExecutor(5, 10, 10 * 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100), new ThreadPoolExecutor.AbortPolicy());

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
            // 创建一个线程监控主线程，当命令执行时间过长时，守护线程会自动结束主线程
            executor.execute(() -> {
                try {
                    Thread.sleep(TIME_LIMIT);
                    log.error("run code over time");
                    process.destroy();
                } catch (InterruptedException e) {
                    log.error("interrupt thread error", e);
                }
            });

            // 开启计时
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // 等待进行结束获取退出码
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                log.info("{} success", opName);
            } else {
                log.error("{} error", opName);

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

            // 关闭线程池
            executor.shutdown();

        } catch (IOException | InterruptedException e) {
            log.error("execute command error", e);
        }
        return executeMessage;
    }
}

package com.by.codesandbox;

import cn.hutool.core.util.ArrayUtil;
import com.by.model.ExecuteMessage;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Docker 代码沙箱
 *
 * @author lzh
 */
@Slf4j
public class JavaDockerCodeSandBox extends BaseCodeSandBoxTemplate {

    /**
     * 超时时间
     */
    public static final long TIME_OUT = 5000L;

    @Override
    public List<ExecuteMessage> runFile(List<String> inputList, File userCodeFile) {
        // 创建Docker客户端
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        // 镜像名称
        String image = "openjdk:8-alpine";

        // 判断镜像是否存在
        if (!hasJavaImage(dockerClient, image)) {
            // 拉取Java环境镜像
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            try {
                pullImageCmd.exec(new PullImageResultCallback() {
                    @Override
                    public void onNext(PullResponseItem item) {
                        log.info("docker pulling image ...");
                        super.onNext(item);
                    }
                }).awaitCompletion();
            } catch (InterruptedException e) {
                log.error("docker pull image error");
            }
        }
        log.info("docker pull image complete ...");

        // 创建容器
        log.info("docker creating container ...");
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);

        // 创建配置
        HostConfig hostConfig = new HostConfig();
        // 将用户代码文件挂载到容器中
        String codeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        hostConfig.withBinds(new Bind(codeParentPath, new Volume("/app")));
        // 设置容器不允许操作根目录
        hostConfig.withReadonlyRootfs(false);
        // 设置容器的内存限制
        hostConfig.withMemory(256 * 1024 * 1024L);
        // 设置容器的CPU数量
        hostConfig.withCpuCount(1L);

        // 添加配置并创建容器
        CreateContainerResponse response = containerCmd
                .withHostConfig(hostConfig)
                // 允许像容器进行输入
                .withAttachStdin(true)
                // 获取容器标准输出
                .withAttachStdout(true)
                // 获取容器错误输出
                .withAttachStderr(true)
                // 以交互模式运行容器
                .withTty(true)
                .exec();

        // 运行容器
        log.info("docker running container ...");
        String containerId = response.getId();
        dockerClient.startContainerCmd(containerId).exec();

        // 进入容器执行命令
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            // 输入参数
            String[] inputs = inputArgs.split(" ");
            String[] cmdArray = new String[]{"java", "-cp", "/app", "Main"};
            ArrayUtil.append(cmdArray, inputs);

            // 创建执行命令
            log.info("docker creating cmd ...");
            ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            // 获取命令ID
            String cmdId = cmdResponse.getId();

            // 执行信息
            ExecuteMessage executeMessage = new ExecuteMessage();

            // 获取内存占用
            dockerClient.statsCmd(containerId)
                    .exec(new ResultCallback<Statistics>() {
                        @Override
                        public void onStart(Closeable closeable) {

                        }

                        @Override
                        public void onNext(Statistics object) {
                            // 设置内存占用
                            Long memoryUsage = object.getMemoryStats().getUsage();
                            executeMessage.setMemoryUsage(memoryUsage);
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void close() {

                        }
                    });

            // 执行命令回调
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onNext(Frame frame) {
                    // 获取流类型
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        // 设置错误信息
                        executeMessage.setErrorMessage(new String(frame.getPayload()));
                    } else {
                        // 设置正常输出
                        executeMessage.setMessage(new String(frame.getPayload()));
                    }
                }
            };

            // 执行命令
            StopWatch stopWatch = new StopWatch();
            try {
                stopWatch.start();
                dockerClient.execStartCmd(cmdId)
                        .exec(execStartResultCallback)
                        // 设置超时时间 5s
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
                stopWatch.stop();
            } catch (InterruptedException e) {
                log.error("docker exec cmd over time");
            }

            // 设置执行时间
            executeMessage.setTimeUsage(stopWatch.getLastTaskTimeMillis());
            executeMessageList.add(executeMessage);
        }

        try {
            // 关闭客户端
            dockerClient.close();
        } catch (IOException e) {
            log.error("docker client close error");
        }
        return executeMessageList;
    }

    private boolean hasJavaImage(DockerClient dockerClient, String imageName) {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        // 获取镜像列表
        List<Image> imageList = listImagesCmd.exec();
        if (CollectionUtils.isEmpty(imageList)) {
            return false;
        }

        // 遍历镜像获取标签
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            if (repoTags != null) {
                if (Arrays.asList(repoTags).contains(imageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}

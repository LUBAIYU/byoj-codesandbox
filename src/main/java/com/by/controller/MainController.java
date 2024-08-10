package com.by.controller;

import com.by.codesandbox.JavaNativeCodeSandBox;
import com.by.model.ExecuteCodeRequest;
import com.by.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 代码沙箱控制器
 *
 * @author lzh
 */
@RestController
@RequestMapping("/codesandbox")
@Slf4j
public class MainController {

    public static final String AUTH_REQUEST_HEADER = "byoj-auth";

    public static final String AUTH_REQUEST_SECRET = "byoj-secret";

    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;

    @PostMapping("/execute")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(header)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }
        if (executeCodeRequest == null) {
            log.error("executeCodeRequest is null");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }
        return javaNativeCodeSandBox.executeCode(executeCodeRequest);
    }
}

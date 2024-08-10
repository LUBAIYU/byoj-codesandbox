package com.by.enums;

import lombok.Getter;

/**
 * 代码执行状态枚举
 *
 * @author lzh
 */
@Getter
public enum CodeExecuteEnum {

    /**
     * 执行成功
     */
    RUN_SUCCESS(0, "运行成功"),

    RUN_ERROR(1, "运行错误");

    private final Integer value;

    private final String text;

    CodeExecuteEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }
}

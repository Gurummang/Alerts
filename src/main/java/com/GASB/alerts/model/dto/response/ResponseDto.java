package com.GASB.alerts.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {

    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";

    private String status;
    private Long fileId;
    private String message;
    private Map<String, Object> error;
    private T data;

    public static <T> ResponseDto<T> ofSuccess() {
        return ResponseDto.<T>builder()
                .status(SUCCESS_STATUS)
                .build();
    }

    public static <T> ResponseDto<T> ofSuccess(Long fileId, T data) {
        return ResponseDto.<T>builder()
                .status(SUCCESS_STATUS)
                .fileId(fileId)
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> ofSuccess(T data) {
        return ResponseDto.<T>builder()
                .status(SUCCESS_STATUS)
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> ofFail(String message) {
        return ResponseDto.<T>builder()
                .status(ERROR_STATUS)
                .message(message)
                .build();
    }

    public static <T> ResponseDto<T> ofFail(Map<String, Object> error) {
        return ResponseDto.<T>builder()
                .status(ERROR_STATUS)
                .error(error)
                .build();
    }
}

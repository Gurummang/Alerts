package com.GASB.alerts.annotation.JWT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class JwtValidationResponse implements Serializable {
    private final String email;
    private final String status;
}

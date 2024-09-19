package com.GASB.alerts.model.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetEmailsResponse {

    private String message;
    private List<String> email;
}

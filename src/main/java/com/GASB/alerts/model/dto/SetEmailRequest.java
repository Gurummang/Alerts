package com.GASB.alerts.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetEmailRequest {

    private String title;
    private List<String> emails;
    private String message;
    private boolean suspicious;
    private boolean sensitive;
    private boolean vt;

}

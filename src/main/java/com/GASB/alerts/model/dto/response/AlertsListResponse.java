package com.GASB.alerts.model.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertsListResponse {
    private long id;
    private List<String> email;
    private String title;
    private String content;
    private boolean suspicious;
    private boolean sensitive;
    private boolean vt;
}











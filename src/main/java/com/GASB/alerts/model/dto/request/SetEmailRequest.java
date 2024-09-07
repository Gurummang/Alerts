package com.GASB.alerts.model.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetEmailRequest {

    private String title;
    private List<String> email;
    private String content;
    private boolean suspicious;
    private boolean sensitive;
    private boolean vt;

}

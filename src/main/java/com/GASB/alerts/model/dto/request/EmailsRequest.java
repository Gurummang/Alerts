package com.GASB.alerts.model.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailsRequest {

    private List<String> email;
}

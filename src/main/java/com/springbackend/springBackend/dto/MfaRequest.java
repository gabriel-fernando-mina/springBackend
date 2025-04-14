package com.springbackend.springBackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MfaRequest {
    private String username;
    private String mfaCode;
}

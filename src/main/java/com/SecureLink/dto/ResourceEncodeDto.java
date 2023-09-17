package com.SecureLink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResourceEncodeDto {
    private String key;
    private int expired;
}

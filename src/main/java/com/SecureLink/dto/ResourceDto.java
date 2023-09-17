package com.SecureLink.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@NoArgsConstructor
@Getter
@Setter
public class ResourceDto {
    private String uri;
    private int expired;
    @JsonIgnore
    private Date createAt = new Date();
    public Date getExpirationDate() {
        long expiredMillis = expired * 1000L;
        return new Date(createAt.getTime() + expiredMillis);
    }

}
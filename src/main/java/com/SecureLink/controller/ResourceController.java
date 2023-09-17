package com.SecureLink.controller;

import com.SecureLink.dto.ResourceDto;
import com.SecureLink.dto.ResourceEncodeDto;
import com.SecureLink.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public Map<String, ResourceDto> getResource(){
        return resourceService.getStorage();
    }

    @PostMapping
    public ResourceEncodeDto uriEncoder(@RequestBody ResourceDto resourceDto){
        return resourceService.uriEncoder(resourceDto);
    }

    @GetMapping(params = {"key"})
    public ResponseEntity<byte[]> getResource(@RequestParam String key){
        return resourceService.getResource(key);
    }

}

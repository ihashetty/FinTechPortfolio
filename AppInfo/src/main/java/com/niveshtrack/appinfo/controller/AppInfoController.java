package com.niveshtrack.appinfo.controller;

import com.niveshtrack.appinfo.config.AppInfoProperties;
import com.niveshtrack.appinfo.dto.AppInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AppInfoController {

    private final AppInfoProperties properties;

    public AppInfoController(AppInfoProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/app-info")
    public ResponseEntity<AppInfoResponse> getAppInfo() {
        AppInfoResponse response = new AppInfoResponse();
        response.setAppName(properties.getAppName());
        response.setTagline(properties.getTagline());
        response.setVersion(properties.getVersion());
        response.setSupportEmail(properties.getSupportEmail());
        response.setSupportPhone(properties.getSupportPhone());
        response.setAddress(properties.getAddress());
        response.setSocialLinks(properties.getSocialLinks());
        response.setCopyright(properties.getCopyright());
        response.setTermsUrl(properties.getTermsUrl());
        response.setPrivacyUrl(properties.getPrivacyUrl());
        return ResponseEntity.ok(response);
    }
}

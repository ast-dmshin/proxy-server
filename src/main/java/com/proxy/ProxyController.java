package com.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RestController
public class ProxyController {

    @Autowired
    private ProxyService proxyService;

    @Value("${proxy.path}")
    private String TARGET_URL = "";

    @GetMapping("/**")
    public ResponseEntity<?> proxyGetRequest(@RequestHeader Map<String, String> headers, @RequestParam Map<String, String> params,
            HttpServletRequest request) {
        try {
            return proxyService.forwardGetRequest(extractUrl(request), params, headers);
        } catch (Exception e) {
            log.error(e.toString());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/**")
    public ResponseEntity<?> proxyPostRequest(@RequestHeader Map<String, String> headers, @RequestBody String body,
            HttpServletRequest request) {
        try {

            return proxyService.forwardPostRequest(extractUrl(request), body, headers);
        } catch (Exception e) {
            log.error(e.toString());
            return ResponseEntity.badRequest().build();
        }
    }

    private String extractUrl(HttpServletRequest request) {
        String path = request.getRequestURI(); // .substring("/proxy".length());
        return TARGET_URL + path; // 외부 서비스 URL을 적절히 변경
    }
}

package com.trackthehill;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    // Throw away endpoint  to confirm server responds
    @GetMapping("/api/ping")
    public String ping() {
        return "pong";
    }
}
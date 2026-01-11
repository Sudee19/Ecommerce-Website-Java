package com.ecommerce.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController implements ErrorController {

    @RequestMapping(value = "/error")
    public Object forward(HttpServletRequest request) {
        Object uriAttr = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String uri = uriAttr != null ? uriAttr.toString() : "";

        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttr instanceof Integer ? (Integer) statusAttr : 500;

        String accept = request.getHeader("Accept");
        boolean wantsHtml = accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);

        if (wantsHtml && !uri.startsWith("/api") && !uri.contains(".")) {
            return "forward:/index.html";
        }

        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Not Found");
    }
}

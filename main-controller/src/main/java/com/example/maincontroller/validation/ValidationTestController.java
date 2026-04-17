package com.example.maincontroller.validation;

import com.example.maincontroller.common.response.NotNeedWrapperResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequestMapping("/validation")
@RestController
public class ValidationTestController {

    @NotNeedWrapperResponse
    @PostMapping("/submit-form")
    public ResponseEntity<String> submitForm(@Valid @RequestBody YourFormClass form, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().toString());
        }
        return ResponseEntity.ok("Form submitted successfully");
    }
}

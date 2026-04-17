package com.example.maincontroller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BusinessRegistrationValidator implements ConstraintValidator<ValidBusinessRegistration, YourFormClass> {
    @Override
    public boolean isValid(YourFormClass form, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (form.isBusinessRegistered()) {
            if (form.getBusinessRegisterNumber() == null || form.getBusinessRegisterNumber().isEmpty()) {
                context.buildConstraintViolationWithTemplate("Business register number is required")
                        .addPropertyNode("businessRegisterNumber").addConstraintViolation();
                return false;
            }
        } else {
            if (form.getBusinessName() == null || form.getBusinessName().isEmpty()) {
                context.buildConstraintViolationWithTemplate("Business name is required")
                        .addPropertyNode("businessName").addConstraintViolation();
                return false;
            }
            if (form.getTin() == null || form.getTin().isEmpty()) {
                context.buildConstraintViolationWithTemplate("TIN is required")
                        .addPropertyNode("tin").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}

package com.example.maincontroller.validation;

import jakarta.validation.GroupSequence;

@GroupSequence({BasicValidation.class, AdvancedValidation.class})
public interface ValidationOrder {
}

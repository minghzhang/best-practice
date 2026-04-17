package com.example.maincontroller.validation;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MyForm {
    @NotNull(message = "Option is required", groups = BasicValidation.class)
    private String option;

    @NotNull(message = "Field1 is required when Option is A", groups = AdvancedValidation.class)
    private String field1;

    @NotNull(message = "Field2 is required when Option is B", groups = AdvancedValidation.class)
    private String field2;

    @AssertTrue(message = "Field1 and Field2 must be provided together when Option is C")
    public boolean isField1AndField2Valid() {
        if ("C".equals(option)) {
            return field1 != null && field2 != null;
        }
        return true;
    }
}

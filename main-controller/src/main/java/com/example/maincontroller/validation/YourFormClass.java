package com.example.maincontroller.validation;

import lombok.Data;
@ValidBusinessRegistration
@Data
public class YourFormClass {

    private boolean businessRegistered;

    private String businessRegisterNumber;

    private String businessName;

    private String tin;

}

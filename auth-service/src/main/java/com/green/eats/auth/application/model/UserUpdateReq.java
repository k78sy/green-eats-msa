package com.green.eats.auth.application.model;

import com.green.eats.common.model.EnumUserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateReq {
    @NotBlank
    private String name;
    @NotBlank
    private String address;
}

package com.vita.user_service.dto.response;

import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDate dob;
    private String address;
}

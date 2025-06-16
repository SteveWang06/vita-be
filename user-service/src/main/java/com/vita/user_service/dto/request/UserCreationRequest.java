package com.vita.user_service.dto.request;

import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDate dob;
    private String address;
}

package com.vita.auth_service.dto.request;



import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationClientRequest {
    String userId;
    String username;
    String firstName;
    String lastName;
    String phone;
    String email;
    LocalDate dob;
    String address;
}

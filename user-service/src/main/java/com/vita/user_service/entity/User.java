package com.vita.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "userId")
    private String userId;

    private String username;
    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String address;

    private LocalDate dob;

}

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

    @Column(nullable = false, unique = true)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String email;

    private String phone;

    private String address;

    private LocalDate dob;

}

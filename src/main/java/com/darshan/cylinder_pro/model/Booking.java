package com.darshan.cylinder_pro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "language")
    private String language = "kn";

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String deviceToken;
    private LocalDate bookingDate;
    private String providerName;

}

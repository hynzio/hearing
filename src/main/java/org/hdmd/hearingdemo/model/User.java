package org.hdmd.hearingdemo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "사용자 엔티티")
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    @Schema(description = "사용자 아이디")
    private Long id;

    @Column(name = "app_token", nullable = false)
    private String fcmToken;

    @Column(name = "password", nullable = false)
    private String pwd;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "rating", nullable = false)
    private String rating;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "range", nullable = false)
    private int range;

}
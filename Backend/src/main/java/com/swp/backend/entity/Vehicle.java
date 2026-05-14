package com.swp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Vehicle")
@Getter
@Setter
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(length = 20)
    private String state = "không hoạt động";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescue_team_id", nullable = false)
    private Staff staff;

    @OneToMany(mappedBy = "vehicle")
    private List<Request> requests;
}
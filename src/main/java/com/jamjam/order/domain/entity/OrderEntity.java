package com.jamjam.order.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    @ElementCollection
    @CollectionTable(name = "order_images")
    private List<String> orderImages;

    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String additionalRequest;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime orderedAt;

    private LocalDateTime finishedAt;

    @NotNull
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private UserEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Builder
    public OrderEntity(String title, LocalDate deadline, List<String> orderImages,
                       String description, String additionalRequest, LocalDateTime finishedAt,
                       OrderStatus orderStatus, UserEntity client, ServiceEntity service) {
        this.title = title;
        this.deadline = deadline;
        this.orderImages = orderImages;
        this.description = description;
        this.additionalRequest = additionalRequest;
        this.finishedAt = finishedAt;
        this.orderStatus = orderStatus;
        this.client = client;
        this.service = service;
    }
}

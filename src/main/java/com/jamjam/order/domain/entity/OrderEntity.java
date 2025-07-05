package com.jamjam.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private LocalDateTime deadline;

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

    @Builder
    public OrderEntity(String title, LocalDateTime deadline, List<String> orderImages, String description, String additionalRequest, LocalDateTime finishedAt, OrderStatus orderStatus) {
        this.title = title;
        this.deadline = deadline;
        this.orderImages = orderImages;
        this.description = description;
        this.additionalRequest = additionalRequest;
        this.finishedAt = finishedAt;
        this.orderStatus = orderStatus;
    }
}

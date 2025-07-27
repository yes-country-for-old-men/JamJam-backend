package com.jamjam.order.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_reference_files")
@Getter
@Setter
@NoArgsConstructor
public class OrderReferenceFileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Builder
    public OrderReferenceFileEntity(String fileUrl, OrderEntity order) {
        this.fileUrl = fileUrl;
        this.order = order;
    }
}

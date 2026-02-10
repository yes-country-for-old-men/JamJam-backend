package com.jamjam.order.domain.entity;

import com.jamjam.global.exception.ApiException;
import com.jamjam.order.dto.OrderStatusRequest;
import com.jamjam.order.exception.OrderError;
import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private LocalDateTime deadline;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderReferenceFileEntity> referenceFiles = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String description;
    private BigDecimal price;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime orderedAt;
    private LocalDateTime serviceCompletedAt;
    private LocalDateTime purchaseConfirmedAt;
    private LocalDateTime canceledAt;

    private boolean purchaseConfirmed = false;

    private String cancelReason;

    @NotNull
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private UserEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Builder
    public OrderEntity(String title, LocalDateTime deadline, List<String> referenceFiles,
                       String description, BigDecimal price,
                       LocalDateTime serviceCompletedAt, LocalDateTime purchaseConfirmedAt,
                       OrderStatus orderStatus, UserEntity client, ServiceEntity service) {
        this.title = title;
        this.deadline = deadline;
        this.description = description;
        this.price = price;
        this.serviceCompletedAt = serviceCompletedAt;
        this.purchaseConfirmedAt = purchaseConfirmedAt;
        this.orderStatus = orderStatus;
        this.client = client;
        this.service = service;
    }

    public void changeStatus(OrderStatusRequest request) {
        switch (request.getOrderStatus()) {
            case CANCELLED:
                this.orderStatus = request.getOrderStatus();
                this.cancelReason = request.getCancelReason();
                this.canceledAt = LocalDateTime.now();
                break;
            case PREPARING:
                this.orderStatus = request.getOrderStatus();
                break;
            case COMPLETED:
                this.orderStatus = request.getOrderStatus();
                this.serviceCompletedAt = LocalDateTime.now();
                break;
            default:
                throw new ApiException(OrderError.UNKNOWN_STATUS);
        }
    }
    public void forceConfirmed() {
        this.purchaseConfirmed = true;
        this.purchaseConfirmedAt = LocalDateTime.now();
    }

    public Long getServiceProviderId() {
        return this.service.getUser().getId();
    }
}

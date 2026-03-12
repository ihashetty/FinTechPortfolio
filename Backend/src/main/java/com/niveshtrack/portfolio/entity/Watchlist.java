package com.niveshtrack.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the {@code watchlist} table.
 * Allows a user to track stocks without necessarily holding them.
 */
@Entity
@Table(name = "watchlist",
        uniqueConstraints = @UniqueConstraint(name = "uk_watchlist_user_symbol", columnNames = {"user_id", "stock_symbol"}))
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "stock_symbol", nullable = false, length = 20)
    private String stockSymbol;

    @Column(name = "stock_name", length = 200)
    private String stockName;

    @Column(name = "added_date")
    @Builder.Default
    private LocalDate addedDate = LocalDate.now();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

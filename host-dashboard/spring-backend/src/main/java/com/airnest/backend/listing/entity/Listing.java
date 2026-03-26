package com.airnest.backend.listing.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String price;

    @Column(nullable = false)
    private String location;

    @Convert(converter = ListingStatusConverter.class)
    @Column(nullable = false)
    private ListingStatus status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Listing() {
    }

    private Listing(String name, String price, String location, ListingStatus status, Instant updatedAt) {
        this.name = name;
        this.price = price;
        this.location = location;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public static Listing create(
        String name,
        String price,
        String location,
        ListingStatus status,
        Instant updatedAt
    ) {
        return new Listing(name, price, location, status, updatedAt);
    }

    public void updateStatus(ListingStatus nextStatus, Instant updatedAt) {
        this.status = nextStatus;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

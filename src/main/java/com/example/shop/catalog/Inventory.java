package com.example.shop.catalog;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name="inventory")
public class Inventory {

    @Id
    @Column(name="product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)
    private Location location = Location.FLOOR;

    @Column(name = "hold_for",length = 255)
    private String holdFor;

    @Column(name = "hold_until")
    private OffsetDateTime holdUntil;

    @Column(name = "updated_at",nullable = false)
    private OffsetDateTime updatedAt;

    protected Inventory(){

    }

    public Inventory(Product product){
        this.product = product;
        this.location= Location.FLOOR;
    }

    @PrePersist
    @PreUpdate
    void touch(){
        this.updatedAt=OffsetDateTime.now();
    }

    public Long getProductId() { return productId; }
    public Product getProduct() { return product; }
    public Location getLocation() { return location; }
    public String getHoldFor() { return holdFor; }
    public OffsetDateTime getHoldUntil() { return holdUntil; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setLocation(Location location) { this.location = location; }
    public void setHoldFor(String holdFor) { this.holdFor = holdFor; }
    public void setHoldUntil(OffsetDateTime holdUntil) { this.holdUntil = holdUntil; }

}

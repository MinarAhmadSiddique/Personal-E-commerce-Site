package com.example.shop.order;

import com.example.shop.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "subtotal_cents",nullable = false)
    private long subtotalCents;

    @Column(name = "shipping_cents",nullable = false)
    private long shippingCents=2500;

    @Column(name = "total_cents",nullable = false)
    private long totalCents;

    @Column(name = "ship_name",nullable = false,length = 160)
    private String shipName;

    @Column(name = "ship_line1",nullable = false,length = 200)
    private String shipLine1;

    @Column(name = "ship_city",nullable = false,length = 100)
    private String shipCity;

    @Column(name = "ship_state",nullable = false,length = 2)
    private String shipState;

    @Column(name = "ship_zip",nullable = false,length = 10)
    private String shipZip;

    @Column(name = "payment_method",nullable = false,length = 40)
    private String paymentMethod;

    @Column(name = "placed_at",nullable = false,updatable = false,insertable = false)
    private OffsetDateTime placedAt;

    protected Order() {
    }

    public Order(User user,String shipName, String shipLine1,String shipCity,String shipState,String shipZip,String paymentMethod){
        this.user = user;
        this.shipName = shipName;
        this.shipLine1 = shipLine1;
        this.shipCity = shipCity;
        this.shipState = shipState;
        this.shipZip = shipZip;
        this.paymentMethod = paymentMethod;
    }

    public void addItem(OrderItem item) {
        items.add(item);        // add to this order's list
        item.setOrder(this);    // tell the item which order it belongs to
    }
    public Long getId() {return id;}
    public User getUser() {return user;}
    public OrderStatus getStatus() {return status;}
    public long getSubtotalCents() {return subtotalCents;}
    public long getShippingCents() {return shippingCents;}
    public long getTotalCents() {return totalCents;}
    public String getShipName() {return shipName;}
    public String getShipLine1() {return shipLine1;}
    public String getShipCity() {return shipCity;}
    public String getShipState() {return shipState;}
    public String getShipZip() {return shipZip;}
    public String getPaymentMethod() {return paymentMethod;}
    public  OffsetDateTime getPlacedAt() {return placedAt;}

    public void setStatus(OrderStatus status) {this.status=status;}
    public void setSubtotalCents(long subtotalCents) {this.subtotalCents = subtotalCents;}
    public void setShippingCents(long shippingCents) {this.shippingCents = shippingCents;}
    public void setTotalCents(long totalCents) {this.totalCents=totalCents;}

    // inside Order.java, if you want the helper:
    public void markPaid() {
        this.status = OrderStatus.PAID;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}

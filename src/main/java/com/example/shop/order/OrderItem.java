package com.example.shop.order;

import com.example.shop.catalog.Product;
import jakarta.persistence.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "product_id",nullable = false)
    private Product product;

    @Column(name = "product_name",nullable = false,length = 160)
    private String productName;

    @Column(name = "product_maker",nullable = false,length = 120)
    private String productMaker;

    @Column(name = "product_serial",nullable = false,length = 80)
    private String productSerial;

    @Column(name = "unit_price_cents",nullable = false)
    private long unitPriceCents;

    protected OrderItem(){

    }

    public OrderItem(Order order,Product product){
        this.order=order;
        this.product=product;
        this.productName=product.getName();
        this.productMaker=product.getMaker();
        this.productSerial=product.getSerialNumber();
        this.unitPriceCents=product.getPriceCents();
    }

    public OrderItem(long id,String name, String maker,String Serial,long price){
        this.id=id;
        this.productName=name;
        this.productMaker=maker;
        this.productSerial=Serial;
        this.unitPriceCents=price;
    }

    public Long getId() {return id;}
    public Order getOrder() {return order;}
    public Product getProduct() {return product;}
    public String getProductName() {return productName;}
    public String getProductMaker() {return productMaker;}
    public String getProductSerial() {return productSerial;}
    public long getUnitPriceCents() {return unitPriceCents;}

    public void setOrder(Order order) {
        this.order = order;
    }
}

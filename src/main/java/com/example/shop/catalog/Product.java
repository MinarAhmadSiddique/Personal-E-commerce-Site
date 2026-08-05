package com.example.shop.catalog;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Locale;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "serial_number", nullable = false, unique = true, length = 80)
    private String serialNumber;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 120)
    private String maker;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private Grade grade;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(name = "category_slug", nullable = false, length = 60)
    private String categorySlug;

    @Column(columnDefinition = "text")
    private String blurb;

    @Column(name = "panel_json", columnDefinition = "jsonb")
    private String panelJson;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    protected Product() {

    }

    public Product(String slug, String serialNumber, String name, String maker, long priceCents, String category, String categorySlug) {
        this.slug = slug;
        this.serialNumber = serialNumber;
        this.name = name;
        this.maker = maker;
        this.priceCents = priceCents;
        this.category = category;
        this.categorySlug = categorySlug;
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getSerialNumber() { return serialNumber; }
    public String getName() { return name; }
    public String getMaker() { return maker; }
    public Integer getModelYear() { return modelYear; }
    public long getPriceCents() { return priceCents; }
    public Grade getGrade() { return grade; }
    public String getCategory() { return category; }
    public String getCategorySlug() { return categorySlug; }
    public String getBlurb() { return blurb; }
    public String getPanelJson() { return panelJson; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setMaker(String maker) { this.maker = maker; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }
    public void setPriceCents(long priceCents) { this.priceCents = priceCents; }
    public void setGrade(Grade grade) { this.grade = grade; }
    public void setCategory(String category) { this.category = category; }
    public void setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }
    public void setBlurb(String blurb) { this.blurb = blurb; }
    public void setPanelJson(String panelJson) { this.panelJson = panelJson; }
    public void setActive(boolean active) { this.active = active; }
}

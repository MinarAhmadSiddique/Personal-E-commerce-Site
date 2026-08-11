package com.example.shop.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService=orderService;
    }

    public static class CheckoutRequest {

        @NotBlank
        private String line1;
        @NotBlank
        private String city;
        @NotBlank
        private String state;
        @NotBlank
        private String zip;

        private List<String> slugs;

        public String getLine1() { return line1; }
        public void setLine1(String v) { this.line1 = v; }
        public String getCity() { return city; }
        public void setCity(String v) { this.city = v; }
        public String getState() { return state; }
        public void setState(String v) { this.state = v; }
        public String getZip() { return zip; }
        public void setZip(String v) { this.zip = v; }
        public List<String> getSlugs() { return slugs; }
        public void setSlugs(List<String> v) { this.slugs = v; }
    }

    public static class OrderResponse{
        private Long id;
        private String status;
        private long totalCents;
        private int itemCount;

        public OrderResponse(Long id, String status, long totalCents, int itemCount){
            this.id = id;
            this.status = status;
            this.totalCents = totalCents;
            this.itemCount = itemCount;
        }

        public static OrderResponse from(Order order){
            long total = order.getItems().stream()
                    .mapToLong(OrderItem::getUnitPriceCents).sum();

            return new OrderResponse(
                    order.getId(),
                    order.getStatus().name(),
                    total,
                    order.getItems().size()
            );
        }

        public Long getId() { return id; }
        public String getStatus() { return status; }
        public long getTotalCents() { return totalCents; }
        public int getItemCount() { return itemCount; }
    }

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest req,Authentication auth)
    {
        ShippingInfo shipping=new ShippingInfo();
        shipping.setLine1(req.getLine1());
        shipping.setCity(req.getCity());
        shipping.setState(req.getState());
        shipping.setZip(req.getZip());

        Order order=orderService.checkout(auth.getName(),req.getSlugs(),shipping);

        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }
}

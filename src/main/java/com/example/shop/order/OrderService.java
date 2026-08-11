package com.example.shop.order;
import com.example.shop.catalog.*;
import com.example.shop.user.User;
import com.example.shop.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        OrderRepository orderRepository,
                        UserRepository userRepository){
        this.productRepository=productRepository;
        this.inventoryRepository=inventoryRepository;
        this.orderRepository=orderRepository;
        this.userRepository=userRepository;
    }

    @Transactional
    public Order checkout(String userEmail, List<String> slugs, ShippingInfo shipping) {
        if (slugs == null || slugs.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty.");
        }

        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("Checkout user not found"));

        // YOUR 7-arg constructor: (user, shipName, shipLine1, shipCity, shipState, shipZip, paymentMethod)
        Order order = new Order(
                user,
                user.getName(),
                shipping.line1(),
                shipping.city(),
                shipping.state(),
                shipping.zip(),
                "SIMULATED"
        );

        long subtotal = 0;

        for (String slug : slugs) {
            Product product = productRepository.findBySlug(slug)
                    .filter(Product::isActive)
                    .orElseThrow(() -> new UnitUnavailableException("This listing is no longer available: " + slug));

            int claimed = inventoryRepository.markSoldIfOnFloor(product.getId());
            if (claimed == 0) {
                throw new UnitUnavailableException("Sold while you were checking out: " + product.getName());
            }

            // YOUR clean 2-arg constructor: (Order, Product) — snapshots automatically
            OrderItem item = new OrderItem(order, product);
            order.addItem(item);
            subtotal += product.getPriceCents();
        }

        boolean paymentApproved = simulatePayment(subtotal);
        if (!paymentApproved) {
            throw new PaymentFailedException("Payment was declined.");
        }

        // your Order has nullable=false money fields — must set them
        order.setSubtotalCents(subtotal);
        order.setShippingCents(2500);
        order.setTotalCents(subtotal + 2500);
        order.markPaid();

        return orderRepository.save(order);
    }

    private boolean simulatePayment(long amountCents) {
        // Always approves for now. Real gateway call would go here.
        return amountCents > 0;
    }

}

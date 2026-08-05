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
    public Order checkout(String userEmail,List<String> slugs,ShippingInfo shipping) {
        if (slugs == null || slugs.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty.");
        }

        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("Checkout user not found"));

        Order order = new Order(user, shipping.line1(), shipping.city(), shipping.state(), shipping.zip());

        long totalCents = 0;

        for (String slug : slugs) {

            Product product = productRepository.findBySlug(slug)
                    .filter(Product::isActive)
                    .orElseThrow(() -> new UnitUnavailableException("This listing is no longer availablr. " + slug));

            int claimed = inventoryRepository.markSoldIfOnFloor(product.getId());

            if (claimed == 0) {
                throw new UnitUnavailableException("Sold while you were checking out: " + product.getName());
            }

            OrderItem item = new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getMaker(),
                    product.getSerialNumber(),
                    product.getPriceCents()
            );

            order.addItem(item);
            totalCents += product.getPriceCents();
        }

        boolean paymentApproved = simulatePayment(totalCents);
        if (!paymentApproved) {
            throw new PaymentFailedException("Payment was declined.");
        }

        order.markPaid();
        return orderRepository.save(order);
    }

    private boolean simulatePayment(long amountCents) {
        // Always approves for now. Real gateway call would go here.
        return amountCents > 0;
    }

}

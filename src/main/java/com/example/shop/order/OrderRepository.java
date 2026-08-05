package com.example.shop.order;

import com.example.shop.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long>{
    List<Order> findByUserOrderByPlacedAtDesc(User user);

    Optional<Order> findByIdAndUser(Long id,User user);

    List<Order> findByStatus(OrderStatus status);
}

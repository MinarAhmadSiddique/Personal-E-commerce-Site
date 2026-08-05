package com.example.shop.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {

    List<Inventory> findByLocation(Location location);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.location = 'SOLD'
            WHERE i.productId =:productId
            AND i.location = 'FLOOR'
            """)
    int markSoldIfOnFloor(@Param("productId") Long productId);

    @Modifying
    @Query("""
      UPDATE Inventory i
      SET i.location = 'FLOOR', i.holdFor = NULL, i.holdUntil = NULL
      WHERE i.location = 'HOLD'
      AND i.holdUntil < :now
""")
    int releaseExpiredHolds(@Param("now") OffsetDateTime now);

}

package com.manilogix.modulith.inventory.internal;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
public interface InventoryRepository extends JpaRepository<InventoryItem,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select i from InventoryItem i where i.id=:id")
 Optional<InventoryItem> lockById(long id);
}


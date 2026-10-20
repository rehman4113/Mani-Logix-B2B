package com.manilogix.modulith.ordering.internal;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select o from PurchaseOrder o where o.id=:id")
 Optional<PurchaseOrder> lockById(long id);
 List<PurchaseOrder> findByCustomerId(long customerId);
 List<PurchaseOrder> findBySupplierId(long supplierId);
 List<PurchaseOrder> findBySupplierIdIn(List<Long> supplierIds);
}


package com.manilogix.modulith.supplier.internal;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SupplierRepository extends JpaRepository<Supplier,Long> {
 boolean existsByIdAndActiveTrue(long id);
 boolean existsByIdAndOwnerUserIdAndActiveTrue(long id,long ownerUserId);
}


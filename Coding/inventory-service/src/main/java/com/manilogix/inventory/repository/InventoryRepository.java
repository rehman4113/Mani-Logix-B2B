package com.manilogix.inventory.repository;

import com.manilogix.inventory.model.InventoryItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends MongoRepository<InventoryItem, String> {
    List<InventoryItem> findByCategoryIgnoreCase(String category);
}
package com.manilogix.inventory.service;

import com.manilogix.inventory.dto.InventoryItemRequest;
import com.manilogix.inventory.dto.InventoryItemResponse;
import com.manilogix.inventory.model.InventoryItem;
import com.manilogix.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository repository;

    public InventoryItemResponse addItem(InventoryItemRequest request) {
        String supplierId = getCurrentUsername();

        InventoryItem item = new InventoryItem();
        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setQuantity(request.getQuantity());
        item.setSupplierId(supplierId);
        item.setReorderLevel(request.getReorderLevel());
        item.setLastUpdated(new Date());

        InventoryItem saved = repository.save(item);
        return mapToResponse(saved);
    }

    public List<InventoryItemResponse> getAllItems() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<InventoryItemResponse> getItemById(String id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    public Optional<InventoryItemResponse> updateItem(String id, InventoryItemRequest request) {
        String supplierId = getCurrentUsername();

        return repository.findById(id)
                .filter(item -> item.getSupplierId().equals(supplierId))
                .map(existing -> {
                    existing.setName(request.getName());
                    existing.setCategory(request.getCategory());
                    existing.setQuantity(request.getQuantity());
                    existing.setReorderLevel(request.getReorderLevel());
                    existing.setLastUpdated(new Date());
                    return mapToResponse(repository.save(existing));
                });
    }

    public Optional<String> deleteItem(String id) {
        String supplierId = getCurrentUsername();

        return repository.findById(id)
                .filter(item -> item.getSupplierId().equals(supplierId))
                .map(item -> {
                    repository.deleteById(id);
                    return "Item deleted successfully: " + id;
                });
    }

    public List<InventoryItemResponse> getItemsByCategory(String category) {
        return repository.findByCategoryIgnoreCase(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryItemResponse> getLowStockItems() {
        return repository.findAll().stream()
                .filter(item -> item.getQuantity() <= item.getReorderLevel())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryItemResponse> getItemsBySupplier(String supplierId) {
        return repository.findById(supplierId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private InventoryItemResponse mapToResponse(InventoryItem item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getQuantity(),
                item.getSupplierId(),
                item.getReorderLevel(),
                item.getLastUpdated()
        );
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
}

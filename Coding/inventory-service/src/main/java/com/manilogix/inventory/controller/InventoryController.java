package com.manilogix.inventory.controller;

import com.manilogix.inventory.dto.InventoryItemRequest;
import com.manilogix.inventory.dto.InventoryItemResponse;
import com.manilogix.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService service;


    // Only SUPPLIER can add items
    @PreAuthorize("hasAnyAuthority('SUPPLIER')")
    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestBody InventoryItemRequest request) {
        return ResponseEntity.ok(service.addItem(request));
    }

    // Accessible by SUPPLIER, MANAGER, ADMIN
    @PreAuthorize("hasAnyAuthority('SUPPLIER', 'MANAGER', 'ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<InventoryItemResponse>> getAll() {
        return ResponseEntity.ok(service.getAllItems());
    }

    // Accessible by SUPPLIER, MANAGER, ADMIN
    @PreAuthorize("hasAnyAuthority('SUPPLIER', 'MANAGER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return service.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only SUPPLIER can update
    @PreAuthorize("hasAuthority('SUPPLIER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestBody InventoryItemRequest request) {
        return service.updateItem(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only SUPPLIER can delete
    @PreAuthorize("hasAuthority('SUPPLIER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return service.deleteItem(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body("Item not found with ID: " + id));
    }

    // Public endpoint for debugging or token check (optional)
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken() {
        // If user is authenticated, their identity is already set by Spring Security
        return ResponseEntity.ok("Token is valid and user is authenticated.");
    }

}


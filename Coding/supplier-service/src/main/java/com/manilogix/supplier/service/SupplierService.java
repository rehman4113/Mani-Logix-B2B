package com.manilogix.supplier.service;

import com.manilogix.supplier.dto.SupplierRequest;
import com.manilogix.supplier.dto.SupplierResponse;
import com.manilogix.supplier.model.Supplier;
import com.manilogix.supplier.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public SupplierResponse addSupplier(SupplierRequest request) {
        Supplier supplier = new Supplier(
                request.getName(),
                request.getEmail(),
                request.getContactNumber(),
                request.getAddress(),
                request.getCompany(),
                LocalDateTime.now()
        );
        supplier = supplierRepository.save(supplier);
        return toResponse(supplier);
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<SupplierResponse> getSupplierById(Long id) {
        return supplierRepository.findById(id).map(this::toResponse);
    }

    public Optional<SupplierResponse> updateSupplier(Long id, SupplierRequest request) {
        return supplierRepository.findById(id).map(existing -> {
            existing.setName(request.getName());
            existing.setEmail(request.getEmail());
            existing.setContactNumber(request.getContactNumber());
            existing.setAddress(request.getAddress());
            existing.setCompany(request.getCompany());
            return toResponse(supplierRepository.save(existing));
        });
    }

    public boolean deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) return false;
        supplierRepository.deleteById(id);
        return true;
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getEmail(),
                supplier.getContactNumber(),
                supplier.getAddress(),
                supplier.getCompany(),
                supplier.getCreatedAt()
        );
    }
}
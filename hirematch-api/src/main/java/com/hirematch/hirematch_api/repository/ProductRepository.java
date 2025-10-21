package com.hirematch.hirematch_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hirematch.hirematch_api.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByType(Product.ProductType type);
    List<Product> findByTypeIn(List<Product.ProductType> types);
}
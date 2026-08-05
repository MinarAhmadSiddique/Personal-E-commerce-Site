package com.example.shop.catalog;

import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final ProductRepository productRepository;

    public CatalogService(ProductRepository productRepository){
        this.productRepository=productRepository;
    }

    public List<Product> listActive(){
        return productRepository.findByActiveTrue();
    }

    public List<Product> listByCategory(String categorySlug){
        return productRepository.findByActiveTrueAndCategorySlug(categorySlug);
    }

    public Product getBySlug(String slug){
        return productRepository.findBySlug(slug)
                .filter(Product::isActive)
                .orElseThrow(()->new ProductNotFoundException(slug));
    }

    public Page<Product> listActive(Pageable pageable){
        return productRepository.findByActiveTrue(pageable);
    }

    public Page<Product> listByCategory(String categorySlug, Pageable pageable) {
        return productRepository.findByActiveTrueAndCategorySlug(categorySlug, pageable);
    }

    public Page<Product> search(String category,Grade grade, String term, Pageable pageable){
        Specification<Product> spec = Specification.where(ProductSpecs.isActive());

        if (category != null && !category.isBlank()) {
            spec = spec.and(ProductSpecs.hasCategory(category));
        }
        if (grade != null) {
            spec = spec.and(ProductSpecs.hasGrade(grade));
        }
        if (term != null && !term.isBlank()) {
            spec = spec.and(ProductSpecs.matchesSearch(term));
        }

        return productRepository.findAll(spec, pageable);
    }

}

package com.example.shop.catalog;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class ProductSpecs {
    private ProductSpecs() {}

    public static Specification<Product> isActive(){
        return (root,query,cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> hasCategory(String categorySlug){
        return (root,query,cb) ->cb.equal(root.get("categorySlug"),categorySlug);
    }

    public static Specification<Product> hasGrade(Grade grade){
        return (root,query,cb) ->cb.equal(root.get("grade"),grade);
    }

    public static Specification<Product> matchesSearch(String term){
        return (root,query,cb)->{
            String like = "%"+term.toLowerCase()+"%";
            return cb.or(
              cb.like(cb.lower(root.get("name")),like),
              cb.like(cb.lower(root.get("maker")),like)
            );
        };
    }
}

package com.example.shop.catalog;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService){
        this.catalogService=catalogService;
    }

    //DTO
    
    public static class ProductResponse{
        private String slug;
        private String serialNumber;
        private String name;
        private String maker;
        private Integer year;
        private long priceCents;
        private String grade;
        private String category;
        private String categorySlug;
        private String blurb;
        private String panelJson;

        public ProductResponse(String slug, String serialNumber, String name, String maker,
                               Integer year, long priceCents, String grade, String category,
                               String categorySlug, String blurb, String panelJson) {
            this.slug = slug;
            this.serialNumber = serialNumber;
            this.name = name;
            this.maker = maker;
            this.year = year;
            this.priceCents = priceCents;
            this.grade = grade;
            this.category = category;
            this.categorySlug = categorySlug;
            this.blurb = blurb;
            this.panelJson = panelJson;
        }

        public static ProductResponse from(Product p) {
            return new ProductResponse(
                    p.getSlug(), p.getSerialNumber(), p.getName(), p.getMaker(),
                    p.getModelYear(), p.getPriceCents(),
                    p.getGrade() == null ? null : p.getGrade().name(),
                    p.getCategory(), p.getCategorySlug(), p.getBlurb(), p.getPanelJson()
            );
        }

        public String getSlug() { return slug; }
        public String getSerialNumber() { return serialNumber; }
        public String getName() { return name; }
        public String getMaker() { return maker; }
        public Integer getYear() { return year; }
        public long getPriceCents() { return priceCents; }
        public String getGrade() { return grade; }
        public String getCategory() { return category; }
        public String getCategorySlug() { return categorySlug; }
        public String getBlurb() { return blurb; }
        public String getPanelJson() { return panelJson; }
    }

    public static class PagedResponse<T>{
        private java.util.List<T> items;
            private int page;
            private int size;
            private long totalItems;
            private int totalPages;

        public PagedResponse(java.util.List<T> items, int page, int size, long totalItems, int totalPages) {
            this.items = items;
            this.page = page;
            this.size = size;
            this.totalItems = totalItems;
            this.totalPages = totalPages;
        }

        public static <T> PagedResponse<T> from(org.springframework.data.domain.Page<T> p){

            return new PagedResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                    p.getTotalElements(), p.getTotalPages());
        }

        public java.util.List<T> getItems() { return items; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalItems() { return totalItems; }
        public int getTotalPages() { return totalPages; }
    }

    @GetMapping("/{slug}")
    public ProductResponse getOne(@PathVariable String slug){
        return ProductResponse.from(catalogService.getBySlug(slug));
    }


    @GetMapping
    public PagedResponse<ProductResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Grade grade,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 12,sort = "name") Pageable pageable
    ){
        Page<Product> page = catalogService.search(category,grade,search,pageable);
        return PagedResponse.from(page.map(ProductResponse::from));
    }
}

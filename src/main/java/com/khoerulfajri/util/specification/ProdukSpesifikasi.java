package com.khoerulfajri.util.specification;

import com.khoerulfajri.entity.Produk;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ProdukSpesifikasi {
    public static Specification<Produk> getSpecification(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }

            String searchTerm = "%" + search.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("nama")), searchTerm);
        };
    }
}

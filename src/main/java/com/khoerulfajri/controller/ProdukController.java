package com.khoerulfajri.controller;

import com.khoerulfajri.entity.Produk;
import com.khoerulfajri.model.CommonResponse;
import com.khoerulfajri.model.PagingResponse;
import com.khoerulfajri.model.ProdukResponse;
import com.khoerulfajri.model.SearchRequest;
import com.khoerulfajri.security.service.UserDetailsImpl;
import com.khoerulfajri.service.ProdukService;
import com.khoerulfajri.util.validation.PagingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProdukController {

    @Autowired
    private ProdukService produkService;

    @GetMapping("/products")
    public List<Produk> findAll(){
        return produkService.findAll();
    }

    @GetMapping("/products-admin")
    @PreAuthorize("hasAuthority('admin')")
    public List<Produk> findAllByAdmin(@AuthenticationPrincipal UserDetailsImpl admin){
        return produkService.findAllByAdmin(admin.getUsername());
    }

    @GetMapping("/products/search")
    public ResponseEntity<CommonResponse<List<Produk>>> findAllSearch(
           @RequestParam (name = "search",required = false) String search,
           @RequestParam(required = false,defaultValue = "1") String page,
           @RequestParam(required = false,defaultValue = "10") String size,
           @RequestParam(required = false,defaultValue = "asc") String direction,
           @RequestParam(required = false,defaultValue = "name") String sortBy
    ){
        Integer safePage = PagingUtil.validatePage(page);
        Integer safeSize = PagingUtil.validateSize(size);
        direction = PagingUtil.validateDirection(direction);

        SearchRequest request = SearchRequest.builder()
                .query(search)
                .page(Math.max(safePage-1,0))
                .size(safeSize)
                .direction(direction)
                .sortBy(sortBy)
                .build();

        Page<Produk> products = produkService.findAllSearch(request);
        PagingResponse paging = PagingResponse.builder()
                .totalPages(products.getTotalPages())
                .totalElement(products.getTotalElements())
                .page(request.getPage()+1)
                .size(request.getSize())
                .hashNext(products.hasNext())
                .hashPrevious(products.hasPrevious())
                .build();

        CommonResponse<List<Produk>> response = CommonResponse.<List<Produk>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All products retrieved")
                .data(products.getContent())
                .paging(paging)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response);
    }

    @GetMapping("/products/{id}")
    public Produk findById (@PathVariable("id") String id){
        return produkService.findByid(id);
    }

    @PostMapping("/products")
    @PreAuthorize("hasAuthority('admin')")
    public ProdukResponse create(@RequestBody Produk produk){
        return produkService.create(produk);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/products")
    public Produk edit(@RequestBody Produk produk){
        return produkService.edit(produk);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/products/{id}")
    public void delete(@PathVariable("id") String id){
        produkService.delete(id);
    }


}

package com.localbuy.controller;

import com.localbuy.dto.ProductDTO;
import com.localbuy.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<?> addProduct(
            @RequestParam("name") String name,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ProductDTO dto = productService.addProduct(name, description, price, image, email);
            return ResponseEntity.ok(dto);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to process image"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProductDTO>> getMyProducts(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(productService.getMyProducts(email));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        try {
            byte[] imageData = productService.getProductImage(id);
            String imageType = productService.getProductImageType(id);
            if (imageData == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, imageType)
                    .body(imageData);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication) {
        try {
            productService.deleteProduct(id, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Product deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

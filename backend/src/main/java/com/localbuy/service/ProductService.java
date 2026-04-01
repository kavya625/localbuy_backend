package com.localbuy.service;

import com.localbuy.dto.ProductDTO;
import com.localbuy.model.Product;
import com.localbuy.model.User;
import com.localbuy.repository.ProductRepository;
import com.localbuy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    public ProductDTO addProduct(String name, String description, BigDecimal price,
                                  MultipartFile image, String sellerEmail) throws IOException {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setSeller(seller);

        if (image != null && !image.isEmpty()) {
            product.setImageData(image.getBytes());
            product.setImageType(image.getContentType());
        }

        product = productRepository.save(product);
        return toDTO(product);
    }

    public List<ProductDTO> getMyProducts(String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        return productRepository.findBySeller(seller)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public byte[] getProductImage(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return product.getImageData();
    }

    public String getProductImageType(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return product.getImageType() != null ? product.getImageType() : "image/jpeg";
    }

    public void deleteProduct(Long productId, String sellerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (!product.getSeller().getEmail().equals(sellerEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        productRepository.delete(product);
    }

    private ProductDTO toDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setSellerId(p.getSeller().getId());
        dto.setSellerName(p.getSeller().getName());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}

package com.ecommerce.ecommerce_backend.config;

import com.ecommerce.ecommerce_backend.entity.Category;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.entity.Role;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.CategoryRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeData();
    }

    private void initializeData() {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@ecommerce.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        // Create demo user if not exists
        if (!userRepository.existsByUsername("demo")) {
            User demo = new User();
            demo.setUsername("demo");
            demo.setEmail("demo@example.com");
            demo.setPassword(passwordEncoder.encode("password"));
            demo.setFirstName("Demo");
            demo.setLastName("User");
            demo.setRole(Role.CUSTOMER);
            userRepository.save(demo);
        }

        // Create categories if not exist
        createCategoryIfNotExists("Electronics", "Electronic devices and gadgets");
        createCategoryIfNotExists("Clothing", "Fashion and apparel");
        createCategoryIfNotExists("Books", "Books and educational materials");
        createCategoryIfNotExists("Home & Garden", "Home improvement and garden supplies");
        createCategoryIfNotExists("Sports", "Sports and outdoor equipment");

        // Create sample products
        createSampleProducts();
    }

    private void createCategoryIfNotExists(String name, String description) {
        if (!categoryRepository.existsByName(name)) {
            Category category = new Category(name, description);
            categoryRepository.save(category);
        }
    }

    private void createSampleProducts() {
        if (productRepository.count() == 0) {
            Category electronics = categoryRepository.findByName("Electronics").orElse(null);
            Category clothing = categoryRepository.findByName("Clothing").orElse(null);
            Category sports = categoryRepository.findByName("Sports").orElse(null);

            if (electronics != null) {
                createProduct("Wireless Headphones", "High-quality wireless headphones with noise cancellation",
                        new BigDecimal("99.99"), 50, electronics,
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400");

                createProduct("Smartphone", "Latest model smartphone with advanced features",
                        new BigDecimal("699.99"), 30, electronics,
                        "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400");

                createProduct("Laptop", "High-performance laptop for work and gaming",
                        new BigDecimal("1299.99"), 20, electronics,
                        "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400");
            }

            if (clothing != null) {
                createProduct("T-Shirt", "Comfortable cotton t-shirt",
                        new BigDecimal("19.99"), 100, clothing,
                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400");

                createProduct("Jeans", "Classic blue denim jeans",
                        new BigDecimal("49.99"), 75, clothing,
                        "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400");
            }

            if (sports != null) {
                createProduct("Running Shoes", "Comfortable running shoes for all terrains",
                        new BigDecimal("129.99"), 60, sports,
                        "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400");

                createProduct("Basketball", "Official size basketball",
                        new BigDecimal("29.99"), 40, sports,
                        "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=400");
            }
        }
    }

    private void createProduct(String name, String description, BigDecimal price,
                               Integer stock, Category category, String imageUrl) {
        Product product = new Product(name, description, price, stock, category);
        product.setImageUrl(imageUrl);
        productRepository.save(product);
    }
}


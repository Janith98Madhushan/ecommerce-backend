package com.ecommerce.ecommerce_backend.service;


import com.ecommerce.ecommerce_backend.dto.CategoryDTO;
import com.ecommerce.ecommerce_backend.entity.Category;
import com.ecommerce.ecommerce_backend.entity.Category;
import com.ecommerce.ecommerce_backend.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<CategoryDTO> findAllActive() {
        List<Category> activeCategories = categoryRepository.findByActiveTrue();
        return activeCategories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            // Soft delete - set active to false instead of hard delete
            Category cat = category.get();
            cat.setActive(false);
            categoryRepository.save(cat);
        }
    }

    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    public long getTotalCategories() {
        return categoryRepository.count();
    }
}

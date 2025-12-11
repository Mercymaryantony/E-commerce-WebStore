package com.webstore.service.whatsapp.business;

import com.webstore.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryBusinessService {

    private final CategoryRepository categoryRepository;

    public CategoryBusinessService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<String> getAllCategoryNames() {
        try {
            return categoryRepository.findAllCategoryNames()
                    .stream()
                    .map(CategoryRepository.CategoryNameProjection::getCategoryName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return categoryRepository.findTop3CategoryNames();
        }
    }

    public List<String> getTop3CategoryNames() {
        return categoryRepository.findTop3CategoryNames();
    }

    public long getTotalCategoryCount() {
        return categoryRepository.count();
    }

    public Integer getCategoryIdByName(String categoryName) {
        CategoryRepository.CategoryIdProjection projection = categoryRepository.findCategoryIdByCategoryName(categoryName);
        return projection != null ? projection.getCategoryId() : null;
    }

    public boolean shouldUseButtonsForCategories() {
        return getTotalCategoryCount() <= 3;
    }
}

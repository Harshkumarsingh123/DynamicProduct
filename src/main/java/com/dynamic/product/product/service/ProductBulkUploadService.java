package com.dynamic.product.product.service;

import com.dynamic.product.category.entity.Category;
import com.dynamic.product.category.repository.CategoryRepository;
import com.dynamic.product.product.dto.ProductExcelRow;
import com.dynamic.product.product.entity.Product;
import com.dynamic.product.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ProductBulkUploadService {

    private final ExcelProductReader excelProductReader;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductBulkUploadService(
            ExcelProductReader excelProductReader,
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.excelProductReader = excelProductReader;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Map<String, Object> uploadProducts(MultipartFile file)
            throws IOException {

        List<ProductExcelRow> rows = excelProductReader.readExl(file);

        int insertedRecords = 0;
        int updatedRecords = 0;
        int failedRecords = 0;

        List<Map<String, Object>> errors = new ArrayList<>();
        Set<Long> categoryIds = new HashSet<>();

        for (ProductExcelRow row : rows) {
            try {
                String categoryIdText = row.getCategoryId();

                if (categoryIdText != null && !categoryIdText.isBlank()) {
                    long categoryId = Long.parseLong(categoryIdText.trim());

                    if (categoryId > 0) {
                        categoryIds.add(categoryId);
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        Map<Long, Category> categoryMap = new HashMap<>();

        for (Category category : categoryRepository.findAllById(categoryIds)) {
            categoryMap.put(category.getId(), category);
        }

        for (ProductExcelRow row : rows) {
            try {
                String name = row.getName();
                String description = row.getDescription();
                String priceText = row.getPrice();
                String visibleText = row.getVisible();
                String imageUrl = row.getImageUrl();
                String categoryIdText = row.getCategoryId();

                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException(
                            "Product name is required.");
                }

                name = name.trim();

                if (description == null || description.isBlank()) {
                    throw new IllegalArgumentException(
                            "Product description is required.");
                }

                description = description.trim();

                if (description.length() > 2000) {
                    throw new IllegalArgumentException(
                            "Description must not exceed 2000 characters.");
                }

                double price;

                try {
                    if (priceText == null || priceText.isBlank()) {
                        throw new NumberFormatException();
                    }

                    price = Double.parseDouble(priceText.trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Price must be a valid positive number.");
                }

                if (!Double.isFinite(price) || price <= 0) {
                    throw new IllegalArgumentException(
                            "Price must be a valid positive number.");
                }

                boolean visible = true;

                if (visibleText != null && !visibleText.isBlank()) {
                    String normalizedVisible = visibleText.trim();

                    if ("true".equalsIgnoreCase(normalizedVisible)) {
                        visible = true;
                    } else if ("false".equalsIgnoreCase(normalizedVisible)) {
                        visible = false;
                    } else {
                        throw new IllegalArgumentException(
                                "Visible must be true or false.");
                    }
                }

                long categoryId;

                try {
                    if (categoryIdText == null
                            || categoryIdText.isBlank()) {
                        throw new NumberFormatException();
                    }

                    categoryId = Long.parseLong(categoryIdText.trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Category ID must be a valid positive integer.");
                }

                if (categoryId <= 0) {
                    throw new IllegalArgumentException(
                            "Category ID must be a valid positive integer.");
                }

                Category category = categoryMap.get(categoryId);

                if (category == null) {
                    throw new IllegalArgumentException(
                            "Category ID " + categoryId + " does not exist."
                    );
                }

                Product product = productRepository
                        .findByNameIgnoreCaseAndCategory_Id(
                                name, categoryId)
                        .orElse(null);

                boolean isNew = product == null;

                if (isNew) {
                    product = new Product();
                }

                product.setName(name);
                product.setDescription(description);
                product.setPrice(price);
                product.setVisible(visible);
                product.setImageUrl(
                        imageUrl == null || imageUrl.isBlank()
                                ? null
                                : imageUrl.trim());
                product.setCategory(category);

                productRepository.save(product);

                if (isNew) {
                    insertedRecords++;
                } else {
                    updatedRecords++;
                }

            } catch (IllegalArgumentException ex) {
                failedRecords++;

                errors.add(Map.of(
                        "rowNumber", row.getRowNumber(),
                        "productName", row.getName() == null
                                ? "" : row.getName(),
                        "errorMessage", ex.getMessage()
                ));

            } catch (RuntimeException ex) {
                failedRecords++;

                errors.add(Map.of(
                        "rowNumber", row.getRowNumber(),
                        "productName", row.getName() == null
                                ? "" : row.getName(),
                        "errorMessage",
                        "Database or processing error. Check the record and try again."
                ));
            }
        }

        return Map.of(
                "message", "Bulk upload completed",
                "totalRecords", rows.size(),
                "insertedRecords", insertedRecords,
                "updatedRecords", updatedRecords,
                "errorRecords", failedRecords,
                "failedRecords", failedRecords,
                "errors", errors
        );
    }
}

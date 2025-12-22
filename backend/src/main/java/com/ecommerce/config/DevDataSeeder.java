package com.ecommerce.config;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        Category electronics = upsertRootCategory(
                "Electronics",
                "Gadgets, devices and accessories",
                "https://images.unsplash.com/photo-1498049794561-7780e7231661?fit=crop&w=1200&q=80&fm=jpg",
                1
        );

        Category fashion = upsertRootCategory(
                "Fashion",
                "Clothing and accessories",
                "https://images.unsplash.com/photo-1445205170230-053b83016050?fit=crop&w=1200&q=80&fm=jpg",
                2
        );

        Category home = upsertRootCategory(
                "Home",
                "Home essentials and decor",
                "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?fit=crop&w=1200&q=80&fm=jpg",
                3
        );

        Category beauty = upsertRootCategory(
                "Beauty",
                "Skincare and personal care",
                "https://images.unsplash.com/photo-1596462502278-27bfdc403348?fit=crop&w=1200&q=80&fm=jpg",
                4
        );

        Category fitness = upsertRootCategory(
                "Fitness",
                "Gym and wellness essentials",
                "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?fit=crop&w=1200&q=80&fm=jpg",
                5
        );

        List<Product> existingProducts = productRepository.findAll();

        List<Product> imageSanitized = new ArrayList<>();
        for (Product p : existingProducts) {
            if (p.getImages() == null || p.getImages().isEmpty()) continue;
            List<String> updated = p.getImages().stream()
                    .map(DevDataSeeder::sanitizeUnsplashUrl)
                    .collect(Collectors.toList());
            if (!updated.equals(p.getImages())) {
                p.setImages(updated);
                imageSanitized.add(p);
            }
        }

        if (!imageSanitized.isEmpty()) {
            productRepository.saveAll(imageSanitized);
        }

        Map<String, Product> existingByNameKey = existingProducts.stream()
                .collect(Collectors.toMap(p -> normalizeName(p.getName()), p -> p, (a, b) -> a));

        List<Product> allProducts = List.of(
                Product.builder()
                        .name("Wireless Headphones")
                        .description("Comfortable over-ear headphones with clear sound.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(2999))
                        .discountPrice(BigDecimal.valueOf(2499))
                        .discountPercentage(17)
                        .categoryId(electronics.getId())
                        .categoryName(electronics.getName())
                        .images(List.of(
                                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?fit=crop&w=1200&q=80&fm=jpg"
                        ))
                        .stockQuantity(25)
                        .active(true)
                        .featured(true)
                        .averageRating(4.6)
                        .reviewCount(128)
                        .tags(List.of("audio", "wireless"))
                        .specs(Product.ProductSpecs.builder()
                                .color("Black")
                                .warranty("1 year")
                                .build())
                        .build(),

                Product.builder()
                        .name("Smart Watch")
                        .description("Track workouts, notifications and more.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(4999))
                        .discountPrice(null)
                        .discountPercentage(0)
                        .categoryId(electronics.getId())
                        .categoryName(electronics.getName())
                        .images(List.of(
                                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?fit=crop&w=1200&q=80&fm=jpg"
                        ))
                        .stockQuantity(15)
                        .active(true)
                        .featured(false)
                        .averageRating(4.2)
                        .reviewCount(74)
                        .tags(List.of("wearable"))
                        .specs(Product.ProductSpecs.builder()
                                .color("Space Gray")
                                .warranty("1 year")
                                .build())
                        .build(),

                Product.builder()
                        .name("Wired Earphones")
                        .description("In-ear wired earphones with clear audio and an in-line mic.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(799))
                        .discountPrice(BigDecimal.valueOf(599))
                        .discountPercentage(25)
                        .categoryId(electronics.getId())
                        .categoryName(electronics.getName())
                        .images(List.of(
                                "https://images.unsplash.com/photo-1484704849700-f032a568e944?fit=crop&w=1200&q=80&fm=jpg"
                        ))
                        .stockQuantity(85)
                        .active(true)
                        .featured(true)
                        .averageRating(4.4)
                        .reviewCount(96)
                        .tags(List.of("audio", "wired"))
                        .specs(Product.ProductSpecs.builder().color("Black").warranty("6 months").build())
                        .build(),

                Product.builder()
                        .name("Classic Sneakers")
                        .description("Everyday sneakers with a comfortable fit.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(1999))
                        .discountPrice(BigDecimal.valueOf(1499))
                        .discountPercentage(25)
                        .categoryId(fashion.getId())
                        .categoryName(fashion.getName())
                        .images(List.of(
                                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?fit=crop&w=1200&q=80&fm=jpg"
                        ))
                        .stockQuantity(40)
                        .active(true)
                        .featured(true)
                        .averageRating(4.8)
                        .reviewCount(210)
                        .tags(List.of("shoes"))
                        .specs(Product.ProductSpecs.builder()
                                .color("White")
                                .build())
                        .build(),

                Product.builder()
                        .name("Minimal Desk Lamp")
                        .description("Warm light desk lamp for your workspace.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(899))
                        .discountPrice(null)
                        .discountPercentage(0)
                        .categoryId(home.getId())
                        .categoryName(home.getName())
                        .images(List.of(
                                "https://images.unsplash.com/photo-1524758631624-e2822e304c36?fit=crop&w=1200&q=80&fm=jpg"
                        ))
                        .stockQuantity(30)
                        .active(true)
                        .featured(false)
                        .averageRating(4.1)
                        .reviewCount(36)
                        .tags(List.of("decor", "lighting"))
                        .specs(Product.ProductSpecs.builder()
                                .color("Beige")
                                .build())
                        .build(),

                Product.builder()
                        .name("Matte Lipstick")
                        .description("Long-lasting matte lipstick with a smooth finish.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(599))
                        .discountPrice(BigDecimal.valueOf(449))
                        .discountPercentage(25)
                        .categoryId(beauty.getId())
                        .categoryName(beauty.getName())
                        .images(List.of("https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?fit=crop&w=1200&q=80&fm=jpg"))
                        .stockQuantity(90)
                        .active(true)
                        .featured(true)
                        .averageRating(4.4)
                        .reviewCount(98)
                        .tags(List.of("beauty"))
                        .specs(Product.ProductSpecs.builder().color("Rose").build())
                        .build(),

                Product.builder()
                        .name("Hydrating Face Serum")
                        .description("Lightweight serum for glowing skin.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(899))
                        .discountPrice(null)
                        .discountPercentage(0)
                        .categoryId(beauty.getId())
                        .categoryName(beauty.getName())
                        .images(List.of("https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?fit=crop&w=1200&q=80&fm=jpg"))
                        .stockQuantity(60)
                        .active(true)
                        .featured(false)
                        .averageRating(4.3)
                        .reviewCount(52)
                        .tags(List.of("skincare"))
                        .specs(Product.ProductSpecs.builder().warranty("N/A").build())
                        .build(),

                Product.builder()
                        .name("Yoga Mat")
                        .description("Non-slip yoga mat for daily workouts.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(1299))
                        .discountPrice(BigDecimal.valueOf(999))
                        .discountPercentage(23)
                        .categoryId(fitness.getId())
                        .categoryName(fitness.getName())
                        .images(List.of("https://images.unsplash.com/photo-1549576490-b0b4831ef60a?fit=crop&w=1200&q=80&fm=jpg"))
                        .stockQuantity(70)
                        .active(true)
                        .featured(true)
                        .averageRating(4.7)
                        .reviewCount(164)
                        .tags(List.of("fitness"))
                        .specs(Product.ProductSpecs.builder().color("Purple").build())
                        .build(),

                Product.builder()
                        .name("Adjustable Dumbbells")
                        .description("Compact adjustable dumbbells set.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(7999))
                        .discountPrice(BigDecimal.valueOf(6999))
                        .discountPercentage(13)
                        .categoryId(fitness.getId())
                        .categoryName(fitness.getName())
                        .images(List.of("https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?fit=crop&w=1200&q=80&fm=jpg"))
                        .stockQuantity(12)
                        .active(true)
                        .featured(false)
                        .averageRating(4.5)
                        .reviewCount(41)
                        .tags(List.of("gym"))
                        .specs(Product.ProductSpecs.builder().warranty("6 months").build())
                        .build(),

                Product.builder()
                        .name("Oversized Hoodie")
                        .description("Soft oversized hoodie for everyday wear.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(1799))
                        .discountPrice(null)
                        .discountPercentage(0)
                        .categoryId(fashion.getId())
                        .categoryName(fashion.getName())
                        .images(List.of("https://images.unsplash.com/photo-1520975958225-8f1f8d6d7c3f?fit=crop&w=1200&q=80&fm=jpg"))
                        .stockQuantity(55)
                        .active(true)
                        .featured(false)
                        .averageRating(4.0)
                        .reviewCount(29)
                        .tags(List.of("fashion"))
                        .specs(Product.ProductSpecs.builder().color("Charcoal").build())
                        .build(),

                Product.builder()
                        .name("Stainless Steel Water Bottle")
                        .description("Keeps your drinks cold or hot for hours.")
                        .brand("ShopEase")
                        .price(BigDecimal.valueOf(799))
                        .discountPrice(BigDecimal.valueOf(649))
                        .discountPercentage(19)
                        .categoryId(home.getId())
                        .categoryName(home.getName())
                        .images(List.of("https://images.unsplash.com/photo-1526401485004-2aa6b204ea7e?fit=crop&w=1200&q=80&fm=jpg"))
                        .stockQuantity(120)
                        .active(true)
                        .featured(true)
                        .averageRating(4.6)
                        .reviewCount(88)
                        .tags(List.of("home"))
                        .specs(Product.ProductSpecs.builder().color("Silver").build())
                        .build()
        );

        List<Product> toUpsert = new ArrayList<>();
        for (Product desired : allProducts) {
            Product existing = existingByNameKey.get(normalizeName(desired.getName()));
            if (existing == null) {
                toUpsert.add(desired);
                continue;
            }

            existing.setDescription(desired.getDescription());
            existing.setBrand(desired.getBrand());
            existing.setPrice(desired.getPrice());
            existing.setDiscountPrice(desired.getDiscountPrice());
            existing.setDiscountPercentage(desired.getDiscountPercentage());
            existing.setCategoryId(desired.getCategoryId());
            existing.setCategoryName(desired.getCategoryName());
            existing.setImages(desired.getImages());
            existing.setStockQuantity(desired.getStockQuantity());
            existing.setActive(desired.isActive());
            existing.setFeatured(desired.isFeatured());
            existing.setAverageRating(desired.getAverageRating());
            existing.setReviewCount(desired.getReviewCount());
            existing.setTags(desired.getTags());
            existing.setSpecs(desired.getSpecs());

            toUpsert.add(existing);
        }

        if (!toUpsert.isEmpty()) {
            productRepository.saveAll(toUpsert);
        }
    }

    private static String normalizeName(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase();
    }

    private static String sanitizeUnsplashUrl(String url) {
        if (url == null) return null;
        if (!url.contains("images.unsplash.com")) return url;

        String sanitized = url;
        sanitized = sanitized.replace("auto=format&", "");
        sanitized = sanitized.replace("auto=format", "");

        if (!sanitized.contains("fm=jpg")) {
            sanitized = sanitized + (sanitized.contains("?") ? "&" : "?") + "fm=jpg";
        }
        return sanitized;
    }

    private Category upsertRootCategory(String name, String description, String image, int displayOrder) {
        String slug = slug(name);
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .image(image)
                        .slug(slug)
                        .parentId(null)
                        .active(true)
                        .displayOrder(displayOrder)
                        .build()));
    }

    private static String slug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}

package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Category;
import com.ecommerce.model.Order;
import com.ecommerce.model.Payment;
import com.ecommerce.model.Product;
import com.ecommerce.model.Review;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemoDataService {

    private static final String DEMO_MARKER_SLUG = "demo-seed-marker";

    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("500");
    private static final BigDecimal SHIPPING_COST = new BigDecimal("50");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;

    public void ensureDemoData(User demoUser) {
        boolean firstSeed = !categoryRepository.existsBySlug(DEMO_MARKER_SLUG);
        if (firstSeed) {
            seedCatalog();
            if (demoUser != null) {
                seedDemoOrdersForVerifiedReviews(demoUser);
            }

            categoryRepository.save(Category.builder()
                    .name("Demo Seed Marker")
                    .description("Internal marker for demo seeding")
                    .image(null)
                    .slug(DEMO_MARKER_SLUG)
                    .parentId(null)
                    .active(false)
                    .displayOrder(0)
                    .build());
        }

        ensureHighVolumeReviews();

        if (demoUser != null) {
            seedDemoUserCartAndWishlist(demoUser);
        }
    }

    private void ensureHighVolumeReviews() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return;
        }

        List<Product> shuffled = new ArrayList<>(products);
        Collections.shuffle(shuffled, new Random(777));

        int reviewedProducts = Math.min(80, shuffled.size());
        seedReviewsAndRatings(shuffled.subList(0, reviewedProducts));
    }

    private void seedCatalog() {
        List<CategorySeed> rootCategories = List.of(
                new CategorySeed("Electronics", "Gadgets, devices and accessories", "https://picsum.photos/seed/cat-electronics/1200/800", 1),
                new CategorySeed("Fashion", "Clothing and accessories", "https://picsum.photos/seed/cat-fashion/1200/800", 2),
                new CategorySeed("Home", "Home essentials and decor", "https://picsum.photos/seed/cat-home/1200/800", 3),
                new CategorySeed("Beauty", "Skincare and personal care", "https://picsum.photos/seed/cat-beauty/1200/800", 4),
                new CategorySeed("Fitness", "Gym and wellness essentials", "https://picsum.photos/seed/cat-fitness/1200/800", 5),
                new CategorySeed("Books", "Bestsellers and new reads", "https://picsum.photos/seed/cat-books/1200/800", 6),
                new CategorySeed("Kitchen", "Cookware and appliances", "https://picsum.photos/seed/cat-kitchen/1200/800", 7),
                new CategorySeed("Travel", "Bags and travel essentials", "https://picsum.photos/seed/cat-travel/1200/800", 8)
        );

        List<Category> categories = new ArrayList<>();
        for (CategorySeed seed : rootCategories) {
            String slug = slug(seed.name);
            Category category = categoryRepository.findBySlug(slug)
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name(seed.name)
                            .description(seed.description)
                            .image(seed.image)
                            .slug(slug)
                            .parentId(null)
                            .active(true)
                            .displayOrder(seed.displayOrder)
                            .build()));
            categories.add(category);
        }

        List<Product> existing = productRepository.findAll();
        Map<String, Product> existingByNameKey = existing.stream()
                .collect(Collectors.toMap(p -> normalizeName(p.getName()), p -> p, (a, b) -> a));

        Random random = new Random(42);

        List<String> electronicsNames = List.of(
                "Wireless Earbuds", "Smart Watch", "Bluetooth Speaker", "Gaming Mouse", "Mechanical Keyboard",
                "4K Action Camera", "USB-C Hub", "Portable SSD", "Noise-Canceling Headphones", "Smart Bulb"
        );
        List<String> fashionNames = List.of(
                "Casual Sneakers", "Denim Jacket", "Hoodie", "Classic T-Shirt", "Running Shorts",
                "Leather Belt", "Sunglasses", "Backpack", "Formal Shirt", "Summer Dress"
        );
        List<String> homeNames = List.of(
                "Premium Bedsheet", "LED Lamp", "Wall Art", "Cushion Set", "Mini Vacuum Cleaner",
                "Aroma Diffuser", "Storage Organizer", "Door Mat", "Curtains", "Study Table"
        );
        List<String> beautyNames = List.of(
                "Vitamin C Serum", "Moisturizer", "Sunscreen SPF 50", "Face Wash", "Lip Balm",
                "Hair Oil", "Body Lotion", "Shampoo", "Conditioner", "Night Cream"
        );
        List<String> fitnessNames = List.of(
                "Resistance Bands", "Yoga Mat", "Dumbbell Set", "Protein Shaker", "Skipping Rope",
                "Foam Roller", "Gym Gloves", "Kettlebell", "Pull-Up Bar", "Fitness Tracker Strap"
        );
        List<String> booksNames = List.of(
                "Productivity Handbook", "Mystery Novel", "Startup Stories", "Cooking Guide", "Mindfulness Journal",
                "Sci-Fi Saga", "History of India", "Design Basics", "Personal Finance", "Travel Diaries"
        );
        List<String> kitchenNames = List.of(
                "Non-stick Pan", "Chef Knife", "Air Fryer", "Coffee Grinder", "Electric Kettle",
                "Mixer Grinder", "Glass Storage Jars", "Cutting Board", "Toaster", "Spice Rack"
        );
        List<String> travelNames = List.of(
                "Hard Shell Suitcase", "Cabin Backpack", "Neck Pillow", "Packing Cubes", "Travel Adapter",
                "Reusable Water Bottle", "Sling Bag", "Duffel Bag", "Passport Wallet", "Trolley Bag"
        );

        Map<String, List<String>> namesByCategorySlug = new HashMap<>();
        namesByCategorySlug.put(slug("Electronics"), electronicsNames);
        namesByCategorySlug.put(slug("Fashion"), fashionNames);
        namesByCategorySlug.put(slug("Home"), homeNames);
        namesByCategorySlug.put(slug("Beauty"), beautyNames);
        namesByCategorySlug.put(slug("Fitness"), fitnessNames);
        namesByCategorySlug.put(slug("Books"), booksNames);
        namesByCategorySlug.put(slug("Kitchen"), kitchenNames);
        namesByCategorySlug.put(slug("Travel"), travelNames);

        List<Product> toSave = new ArrayList<>();

        for (Category category : categories) {
            String catSlug = category.getSlug();
            List<String> baseNames = namesByCategorySlug.getOrDefault(catSlug, List.of("Item"));

            int productsPerCategory = 18; // slightly higher
            for (int i = 1; i <= productsPerCategory; i++) {
                String base = baseNames.get((i - 1) % baseNames.size());
                String name = base + " " + i;
                String key = normalizeName(name);
                if (existingByNameKey.containsKey(key)) {
                    continue;
                }

                BigDecimal price = BigDecimal.valueOf(199 + random.nextInt(9801));

                boolean discounted = random.nextDouble() < 0.35;
                int discountPercentage = discounted ? (10 + random.nextInt(41)) : 0;
                BigDecimal discountPrice = discounted
                        ? price.multiply(BigDecimal.valueOf(100 - discountPercentage))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        : null;

                boolean featured = random.nextDouble() < 0.25;
                List<String> tags = Arrays.asList("demo", category.getSlug(), category.getName().toLowerCase());

                String seed = "demo-" + category.getSlug() + "-" + i;
                String image = "https://picsum.photos/seed/" + seed + "/1200/1200";

                Product product = Product.builder()
                        .name(name)
                        .description("Demo product: " + base + " with great quality and fast delivery.")
                        .brand(pickBrand(random))
                        .price(price)
                        .discountPrice(discountPrice)
                        .discountPercentage(discountPercentage)
                        .categoryId(category.getId())
                        .categoryName(category.getName())
                        .images(List.of(image))
                        .stockQuantity(50 + random.nextInt(250))
                        .active(true)
                        .featured(featured)
                        .averageRating(0.0)
                        .reviewCount(0)
                        .tags(tags)
                        .specs(Product.ProductSpecs.builder()
                                .color(pickColor(random))
                                .material(pickMaterial(random))
                                .warranty("1 year")
                                .build())
                        .build();

                toSave.add(product);
                existingByNameKey.put(key, product);
            }
        }

        if (!toSave.isEmpty()) {
            toSave = productRepository.saveAll(toSave);
        }

        seedReviewsAndRatings(toSave);
    }

    private void seedReviewsAndRatings(List<Product> productsToReview) {
        if (productsToReview == null || productsToReview.isEmpty()) {
            return;
        }

        Random random = new Random(99);

        List<String> names = List.of(
                "Aarav Sharma", "Isha Verma", "Kabir Singh", "Meera Joshi", "Rohan Patil",
                "Ananya Gupta", "Vihaan Rao", "Saanvi Kulkarni", "Arjun Mehta", "Diya Nair",
                "Nikhil Kapoor", "Priya Iyer", "Siddharth Jain", "Neha Bansal", "Aditya Das",
                "Pooja Malhotra", "Rahul Khanna", "Aisha Khan", "Karan Patel", "Sneha Reddy"
        );

        List<String> titles = List.of(
                "Worth it", "Excellent quality", "Good value", "Superb", "Highly recommended", "Loved it"
        );

        List<Review> reviewsToSave = new ArrayList<>();
        List<Product> productsToUpdate = new ArrayList<>();

        for (int i = 0; i < productsToReview.size(); i++) {
            Product product = productsToReview.get(i);

            List<Review> existing = reviewRepository.findByProductId(product.getId());
            int existingCount = existing.size();
            int targetCount = targetReviewCountForProduct(product.getId()); // 10-20
            int toAdd = Math.max(0, targetCount - existingCount);

            int ratingSum = existing.stream().mapToInt(Review::getRating).sum();

            for (int r = 0; r < toAdd; r++) {
                int rating = random.nextDouble() < 0.12
                        ? (1 + random.nextInt(2))
                        : (3 + random.nextInt(3));

                String reviewerName = names.get((i + r) % names.size());
                int reviewerIndex = existingCount + r;

                Review review = Review.builder()
                        .productId(product.getId())
                        .userId("demo-seed-reviewer-" + i + "-" + reviewerIndex)
                        .userName(reviewerName)
                        .rating(rating)
                        .title(titles.get((i + r) % titles.size()))
                        .comment("Demo review: " + reviewerName + " says this product works as expected.")
                        .verified(random.nextDouble() < 0.60)
                        .helpfulCount(random.nextInt(50))
                        .build();

                reviewsToSave.add(review);
                ratingSum += rating;
            }

            int finalCount = existingCount + toAdd;
            if (finalCount > 0) {
                product.setReviewCount(finalCount);
                product.setAverageRating(((double) ratingSum) / finalCount);
                productsToUpdate.add(product);
            }
        }

        if (!reviewsToSave.isEmpty()) {
            reviewRepository.saveAll(reviewsToSave);
        }

        if (!productsToUpdate.isEmpty()) {
            productRepository.saveAll(productsToUpdate);
        }
    }

    private static int targetReviewCountForProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            return 10;
        }
        int h = Math.abs(productId.hashCode());
        return 10 + (h % 11);
    }

    private void seedDemoOrdersForVerifiedReviews(User demoUser) {
        // If demo user already has delivered orders, don't create again
        if (!orderRepository.findByUserIdAndStatus(demoUser.getId(), Order.OrderStatus.DELIVERED).isEmpty()) {
            return;
        }

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return;
        }

        List<Product> shuffled = new ArrayList<>(products);
        Collections.shuffle(shuffled, new Random(7));

        List<Order> ordersToSave = new ArrayList<>();

        for (int o = 1; o <= 3; o++) {
            int start = (o - 1) * 3;
            if (start >= shuffled.size()) {
                break;
            }
            int end = Math.min(shuffled.size(), start + 3);
            List<Product> itemsProducts = shuffled.subList(start, end);
            List<Order.OrderItem> items = new ArrayList<>();

            BigDecimal subtotal = BigDecimal.ZERO;
            for (Product p : itemsProducts) {
                BigDecimal price = p.getDiscountPrice() != null ? p.getDiscountPrice() : p.getPrice();
                int qty = 1 + (o % 2);
                BigDecimal itemSubtotal = price.multiply(BigDecimal.valueOf(qty));
                subtotal = subtotal.add(itemSubtotal);

                items.add(Order.OrderItem.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .productImage((p.getImages() != null && !p.getImages().isEmpty()) ? p.getImages().get(0) : null)
                        .price(price)
                        .quantity(qty)
                        .subtotal(itemSubtotal)
                        .build());
            }

            BigDecimal shippingCost = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? BigDecimal.ZERO : SHIPPING_COST;
            BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalAmount = subtotal.add(shippingCost).add(tax);

            LocalDateTime createdAt = LocalDateTime.now().minusDays(10L * o);
            LocalDateTime deliveredAt = createdAt.plusDays(3);

            Order order = Order.builder()
                    .orderNumber("DEMO" + System.currentTimeMillis() + o)
                    .userId(demoUser.getId())
                    .userName("Demo User")
                    .userEmail(demoUser.getEmail())
                    .items(items)
                    .shippingAddress(Order.ShippingAddress.builder()
                            .fullName("Demo User")
                            .phone("9999999999")
                            .street("123 Demo Street")
                            .city("Demo City")
                            .state("Demo State")
                            .zipCode("000000")
                            .country("India")
                            .build())
                    .subtotal(subtotal)
                    .shippingCost(shippingCost)
                    .tax(tax)
                    .totalAmount(totalAmount)
                    .status(Order.OrderStatus.DELIVERED)
                    .paymentStatus(Order.PaymentStatus.COMPLETED)
                    .paymentMethod("UPI")
                    .trackingNumber("DEMO-TRACK-" + o)
                    .notes("Demo seeded order")
                    .createdAt(createdAt)
                    .deliveredAt(deliveredAt)
                    .build();

            ordersToSave.add(order);
        }

        ordersToSave = orderRepository.saveAll(ordersToSave);

        List<Payment> paymentsToSave = new ArrayList<>();
        for (Order order : ordersToSave) {
            paymentsToSave.add(Payment.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .amount(order.getTotalAmount())
                    .currency("INR")
                    .method(Payment.PaymentMethod.UPI)
                    .status(Payment.PaymentStatus.SUCCESS)
                    .transactionId("DEMO-TXN-" + order.getOrderNumber())
                    .build());
        }

        paymentsToSave = paymentRepository.saveAll(paymentsToSave);

        Map<String, String> paymentIdByOrderId = paymentsToSave.stream()
                .collect(Collectors.toMap(Payment::getOrderId, Payment::getId));

        for (Order order : ordersToSave) {
            String paymentId = paymentIdByOrderId.get(order.getId());
            if (paymentId != null) {
                order.setPaymentId(paymentId);
            }
        }

        orderRepository.saveAll(ordersToSave);
    }

    private void seedDemoUserCartAndWishlist(User demoUser) {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return;
        }

        List<Product> shuffled = new ArrayList<>(products);
        Collections.shuffle(shuffled, new Random(123));

        // Cart
        Cart cart = cartRepository.findByUserId(demoUser.getId()).orElseGet(() -> cartRepository.save(Cart.builder()
                .userId(demoUser.getId())
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .totalItems(0)
                .build()));

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        if (cart.getItems().isEmpty()) {
            for (int i = 0; i < 3; i++) {
                Product p = shuffled.get(i);
                BigDecimal price = p.getDiscountPrice() != null ? p.getDiscountPrice() : p.getPrice();
                int qty = 1 + (i % 2);

                cart.getItems().add(Cart.CartItem.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .productImage((p.getImages() != null && !p.getImages().isEmpty()) ? p.getImages().get(0) : null)
                        .price(price)
                        .quantity(qty)
                        .subtotal(price.multiply(BigDecimal.valueOf(qty)))
                        .build());
            }
            cart.recalculateTotals();
            cartRepository.save(cart);
        }

        // Wishlist
        var wishlist = wishlistRepository.findByUser_Id(demoUser.getId()).orElseGet(() -> {
            var w = new com.ecommerce.model.Wishlist();
            w.setUser(demoUser);
            return wishlistRepository.save(w);
        });

        if (wishlist.getProducts() == null) {
            wishlist.setProducts(new ArrayList<>());
        }

        if (wishlist.getProducts().isEmpty()) {
            for (int i = 3; i < 10; i++) {
                wishlist.getProducts().add(shuffled.get(i));
            }
            wishlistRepository.save(wishlist);
        }
    }

    private static String normalizeName(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static String slug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    private static String pickBrand(Random random) {
        List<String> brands = List.of("ShopEase", "Nova", "Urban", "Pulse", "Aura", "Vertex");
        return brands.get(random.nextInt(brands.size()));
    }

    private static String pickColor(Random random) {
        List<String> colors = List.of("Black", "White", "Blue", "Red", "Green", "Grey");
        return colors.get(random.nextInt(colors.size()));
    }

    private static String pickMaterial(Random random) {
        List<String> materials = List.of("Plastic", "Metal", "Cotton", "Leather", "Glass", "Wood");
        return materials.get(random.nextInt(materials.size()));
    }

    private record CategorySeed(String name, String description, String image, int displayOrder) {}
}

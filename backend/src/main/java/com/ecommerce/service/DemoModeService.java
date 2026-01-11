package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Order;
import com.ecommerce.model.Review;
import com.ecommerce.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DemoModeService {

    private static final String DEMO_EMAIL_DOMAIN = "@ecommerce.local";
    private static final String DEMO_EMAIL_PREFIX = "demo+";
    private static final String LEGACY_DEMO_EMAIL = "demo@ecommerce.local";
    private static final String DEMO_USER_ID_PREFIX = "demo-";

    private final Map<String, DemoSession> sessionsByUserId = new ConcurrentHashMap<>();

    public boolean isDemoEmail(String email) {
        if (email == null) {
            return false;
        }
        if (LEGACY_DEMO_EMAIL.equalsIgnoreCase(email)) {
            return true;
        }
        return email.startsWith(DEMO_EMAIL_PREFIX) && email.endsWith(DEMO_EMAIL_DOMAIN);
    }

    public boolean isDemoUserId(String userId) {
        return userId != null && userId.startsWith(DEMO_USER_ID_PREFIX);
    }

    public String getCurrentEmailOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    public boolean isCurrentUserDemo() {
        return isDemoEmail(getCurrentEmailOrNull());
    }

    public User getOrCreateDemoUserByEmail(String email) {
        String userId = toDemoUserId(email);
        DemoSession session = sessionsByUserId.computeIfAbsent(userId, ignored -> {
            User user = User.builder()
                    .id(userId)
                    .firstName("Demo")
                    .lastName("User")
                    .email(email)
                    .roles(Set.of(User.Role.USER))
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            return new DemoSession(user);
        });
        return session.user;
    }

    public Cart getOrCreateCartByUser(User user) {
        DemoSession session = getSessionByUserId(user.getId());
        if (session.cart == null) {
            session.cart = Cart.builder()
                    .id("demo-cart-" + UUID.randomUUID())
                    .userId(user.getId())
                    .items(new ArrayList<>())
                    .totalItems(0)
                    .totalPrice(BigDecimal.ZERO)
                    .build();
        }
        return session.cart;
    }

    public Cart saveCart(User user, Cart cart) {
        DemoSession session = getSessionByUserId(user.getId());
        session.cart = cart;
        return cart;
    }

    public void clearCart(User user) {
        DemoSession session = getSessionByUserId(user.getId());
        if (session.cart != null) {
            session.cart.getItems().clear();
            session.cart.recalculateTotals();
        }
    }

    public Set<String> getWishlistProductIds(User user) {
        return getSessionByUserId(user.getId()).wishlistProductIds;
    }

    public void addToWishlist(User user, String productId) {
        getSessionByUserId(user.getId()).wishlistProductIds.add(productId);
    }

    public void removeFromWishlist(User user, String productId) {
        getSessionByUserId(user.getId()).wishlistProductIds.remove(productId);
    }

    public void clearWishlist(User user) {
        getSessionByUserId(user.getId()).wishlistProductIds.clear();
    }

    public boolean isInWishlist(User user, String productId) {
        return getSessionByUserId(user.getId()).wishlistProductIds.contains(productId);
    }

    public Order saveOrder(User user, Order order) {
        DemoSession session = getSessionByUserId(user.getId());
        session.ordersById.put(order.getId(), order);
        session.orders.removeIf(o -> order.getId() != null && order.getId().equals(o.getId()));
        session.orders.add(order);
        session.orders.sort(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return order;
    }

    public Order getOrder(User user, String orderId) {
        return getSessionByUserId(user.getId()).ordersById.get(orderId);
    }

    public List<Order> getOrders(User user) {
        return new ArrayList<>(getSessionByUserId(user.getId()).orders);
    }

    public Review saveReview(User user, Review review) {
        DemoSession session = getSessionByUserId(user.getId());
        session.reviewsById.put(review.getId(), review);
        session.reviewsByProductId.computeIfAbsent(review.getProductId(), ignored -> new ArrayList<>());
        session.reviewsByProductId.get(review.getProductId()).removeIf(r -> review.getId() != null && review.getId().equals(r.getId()));
        session.reviewsByProductId.get(review.getProductId()).add(review);
        session.reviewsByProductId.get(review.getProductId()).sort(Comparator.comparing(Review::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return review;
    }

    public Review getReview(User user, String reviewId) {
        return getSessionByUserId(user.getId()).reviewsById.get(reviewId);
    }

    public boolean existsReviewByProductAndUser(User user, String productId) {
        List<Review> reviews = getSessionByUserId(user.getId()).reviewsByProductId.getOrDefault(productId, Collections.emptyList());
        return reviews.stream().anyMatch(r -> user.getId().equals(r.getUserId()));
    }

    public List<Review> getReviewsForProduct(User user, String productId) {
        return getSessionByUserId(user.getId()).reviewsByProductId.getOrDefault(productId, Collections.emptyList())
                .stream()
                .collect(Collectors.toList());
    }

    public void deleteReview(User user, String reviewId) {
        DemoSession session = getSessionByUserId(user.getId());
        Review removed = session.reviewsById.remove(reviewId);
        if (removed != null) {
            List<Review> reviews = session.reviewsByProductId.get(removed.getProductId());
            if (reviews != null) {
                reviews.removeIf(r -> reviewId.equals(r.getId()));
            }
        }
    }

    private DemoSession getSessionByUserId(String userId) {
        if (!isDemoUserId(userId)) {
            throw new IllegalArgumentException("Not a demo userId");
        }
        return sessionsByUserId.computeIfAbsent(userId, ignored -> {
            User user = User.builder()
                    .id(userId)
                    .firstName("Demo")
                    .lastName("User")
                    .email(LEGACY_DEMO_EMAIL)
                    .roles(Set.of(User.Role.USER))
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            return new DemoSession(user);
        });
    }

    private String toDemoUserId(String email) {
        if (LEGACY_DEMO_EMAIL.equalsIgnoreCase(email)) {
            return DEMO_USER_ID_PREFIX + "legacy";
        }
        String raw = email.substring(DEMO_EMAIL_PREFIX.length(), email.length() - DEMO_EMAIL_DOMAIN.length());
        return DEMO_USER_ID_PREFIX + raw;
    }

    private static class DemoSession {
        private final User user;
        private Cart cart;
        private final Set<String> wishlistProductIds = ConcurrentHashMap.newKeySet();
        private final Map<String, Order> ordersById = new ConcurrentHashMap<>();
        private final List<Order> orders = new ArrayList<>();
        private final Map<String, Review> reviewsById = new ConcurrentHashMap<>();
        private final Map<String, List<Review>> reviewsByProductId = new ConcurrentHashMap<>();

        private DemoSession(User user) {
            this.user = user;
        }
    }
}

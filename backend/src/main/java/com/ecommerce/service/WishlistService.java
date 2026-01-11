package com.ecommerce.service;

import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Wishlist;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final DemoModeService demoModeService;

    @Transactional
    public Wishlist getOrCreateWishlist(User user) {
        if (demoModeService.isDemoUserId(user.getId())) {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProducts(demoModeService.getWishlistProductIds(user).stream()
                    .filter(Objects::nonNull)
                    .map(id -> productRepository.findById(Objects.requireNonNull(id)).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new)));
            return wishlist;
        }
        return wishlistRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUser(user);
                    return wishlistRepository.save(wishlist);
                });
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getWishlist(User user) {
        if (demoModeService.isDemoUserId(user.getId())) {
            return demoModeService.getWishlistProductIds(user).stream()
                    .filter(Objects::nonNull)
                    .map(id -> productRepository.findById(Objects.requireNonNull(id)).orElse(null))
                    .filter(Objects::nonNull)
                    .map(ProductResponse::fromProduct)
                    .collect(Collectors.toList());
        }
        Wishlist wishlist = getOrCreateWishlist(user);
        return wishlist.getProducts().stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToWishlist(User user, String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (demoModeService.isDemoUserId(user.getId())) {
            demoModeService.addToWishlist(user, product.getId());
            return;
        }

        Wishlist wishlist = getOrCreateWishlist(user);

        if (!wishlist.containsProduct(product)) {
            wishlist.addProduct(product);
            wishlistRepository.save(wishlist);
        }
    }

    @Transactional
    public void removeFromWishlist(User user, String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (demoModeService.isDemoUserId(user.getId())) {
            demoModeService.removeFromWishlist(user, product.getId());
            return;
        }

        Wishlist wishlist = getOrCreateWishlist(user);
        wishlist.removeProduct(product);
        wishlistRepository.save(wishlist);
    }

    @Transactional
    public void clearWishlist(User user) {
        if (demoModeService.isDemoUserId(user.getId())) {
            demoModeService.clearWishlist(user);
            return;
        }
        Wishlist wishlist = getOrCreateWishlist(user);
        wishlist.clearProducts();
        wishlistRepository.save(wishlist);
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(User user, String productId) {
        if (demoModeService.isDemoUserId(user.getId())) {
            return demoModeService.isInWishlist(user, productId);
        }
        Wishlist wishlist = wishlistRepository.findByUser_Id(user.getId()).orElse(null);
        if (wishlist == null) {
            return false;
        }
        return wishlist.getProducts().stream()
                .anyMatch(p -> p.getId().equals(productId));
    }

    @Transactional
    public void moveToCart(User user, String productId) {
        removeFromWishlist(user, productId);
    }
}

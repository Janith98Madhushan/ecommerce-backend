package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ApiResponse;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getUserProfile(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            User userProfile = userService.findByUsername(user.getUsername());

            // Remove sensitive information
            userProfile.setPassword(null);

            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", userProfile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving profile: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(@Valid @RequestBody User userDetails,
                                                           Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();

            // Check if email is being changed and if new email already exists
            if (!currentUser.getEmail().equals(userDetails.getEmail()) &&
                    userService.existsByEmail(userDetails.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already in use"));
            }

            User updatedUser = userService.updateUser(currentUser.getId(), userDetails);
            updatedUser.setPassword(null); // Remove password from response

            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating profile: " + e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody Map<String, String> passwords,
                                                            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            String currentPassword = passwords.get("currentPassword");
            String newPassword = passwords.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Current password and new password are required"));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("New password must be at least 6 characters long"));
            }

            userService.changePassword(user.getId(), currentPassword, newPassword);
            //return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
            return null;
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error changing password: " + e.getMessage()));
        }
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.findAllUsers();

            // Remove passwords from response
            users.forEach(user -> user.setPassword(null));

            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving users: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User userProfile = user.get();
            userProfile.setPassword(null); // Remove password from response

            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userProfile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving user: " + e.getMessage()));
        }
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id,
                                                        @Valid @RequestBody User userDetails) {
        try {
            Optional<User> existingUser = userService.findById(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if email is being changed and if new email already exists
            if (!existingUser.get().getEmail().equals(userDetails.getEmail()) &&
                    userService.existsByEmail(userDetails.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already in use"));
            }

            User updatedUser = userService.updateUser(id, userDetails);
            updatedUser.setPassword(null); // Remove password from response

            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id,
                                                        Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();

            // Prevent admin from deleting themselves
            if (currentUser.getId().equals(id)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot delete your own account"));
            }

            if (!userService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            userService.deleteUser(id);
            //return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
            return null;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting user: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.getTotalUsers());

            return ResponseEntity.ok(ApiResponse.success("User statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving user statistics: " + e.getMessage()));
        }
    }
}

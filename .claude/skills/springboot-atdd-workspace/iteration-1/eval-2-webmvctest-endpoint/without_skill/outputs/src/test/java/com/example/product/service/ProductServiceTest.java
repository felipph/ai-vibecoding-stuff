package com.example.product.service;

import com.example.product.dto.PriceUpdateRequest;
import com.example.product.exception.InvalidPriceException;
import com.example.product.exception.ProductNotFoundException;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 *
 * Tests focus on business logic:
 * - Price validation (must be positive)
 * - Product existence validation
 * - Correct interaction with repository
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product existingProduct;
    private static final Long EXISTING_PRODUCT_ID = 1L;
    private static final Long NON_EXISTING_PRODUCT_ID = 999L;

    @BeforeEach
    void setUp() {
        existingProduct = new Product(
            EXISTING_PRODUCT_ID,
            "Test Product",
            new BigDecimal("50.00"),
            "Test Description"
        );
    }

    @Nested
    @DisplayName("updatePrice() Tests")
    class UpdatePriceTests {

        @Test
        @DisplayName("Should update price successfully when product exists and price is valid")
        void shouldUpdatePriceSuccessfullyWhenProductExistsAndPriceIsValid() {
            // Arrange
            BigDecimal newPrice = new BigDecimal("99.99");
            PriceUpdateRequest request = new PriceUpdateRequest(newPrice);

            when(productRepository.findById(EXISTING_PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Product result = productService.updatePrice(EXISTING_PRODUCT_ID, request);

            // Assert
            assertNotNull(result);
            assertEquals(newPrice, result.getPrice());
            assertEquals(EXISTING_PRODUCT_ID, result.getId());
            verify(productRepository).findById(EXISTING_PRODUCT_ID);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when product does not exist")
        void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() {
            // Arrange
            PriceUpdateRequest request = new PriceUpdateRequest(new BigDecimal("99.99"));

            when(productRepository.findById(NON_EXISTING_PRODUCT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.updatePrice(NON_EXISTING_PRODUCT_ID, request)
            );

            assertEquals("Product not found with id: " + NON_EXISTING_PRODUCT_ID, exception.getMessage());
            verify(productRepository).findById(NON_EXISTING_PRODUCT_ID);
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidPriceException when price is null")
        void shouldThrowInvalidPriceExceptionWhenPriceIsNull() {
            // Arrange
            PriceUpdateRequest request = new PriceUpdateRequest(null);

            // Act & Assert
            InvalidPriceException exception = assertThrows(
                InvalidPriceException.class,
                () -> productService.updatePrice(EXISTING_PRODUCT_ID, request)
            );

            assertEquals("Price must be positive", exception.getMessage());
            verify(productRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidPriceException when price is zero")
        void shouldThrowInvalidPriceExceptionWhenPriceIsZero() {
            // Arrange
            PriceUpdateRequest request = new PriceUpdateRequest(BigDecimal.ZERO);

            // Act & Assert
            InvalidPriceException exception = assertThrows(
                InvalidPriceException.class,
                () -> productService.updatePrice(EXISTING_PRODUCT_ID, request)
            );

            assertEquals("Price must be positive", exception.getMessage());
            verify(productRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidPriceException when price is negative")
        void shouldThrowInvalidPriceExceptionWhenPriceIsNegative() {
            // Arrange
            PriceUpdateRequest request = new PriceUpdateRequest(new BigDecimal("-10.00"));

            // Act & Assert
            InvalidPriceException exception = assertThrows(
                InvalidPriceException.class,
                () -> productService.updatePrice(EXISTING_PRODUCT_ID, request)
            );

            assertEquals("Price must be positive", exception.getMessage());
            verify(productRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should accept minimum positive price (0.01)")
        void shouldAcceptMinimumPositivePrice() {
            // Arrange
            BigDecimal minPrice = new BigDecimal("0.01");
            PriceUpdateRequest request = new PriceUpdateRequest(minPrice);

            when(productRepository.findById(EXISTING_PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Product result = productService.updatePrice(EXISTING_PRODUCT_ID, request);

            // Assert
            assertEquals(minPrice, result.getPrice());
        }

        @Test
        @DisplayName("Should accept large positive price")
        void shouldAcceptLargePositivePrice() {
            // Arrange
            BigDecimal largePrice = new BigDecimal("999999.99");
            PriceUpdateRequest request = new PriceUpdateRequest(largePrice);

            when(productRepository.findById(EXISTING_PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Product result = productService.updatePrice(EXISTING_PRODUCT_ID, request);

            // Assert
            assertEquals(largePrice, result.getPrice());
        }

        @Test
        @DisplayName("Should preserve other product fields when updating price")
        void shouldPreserveOtherProductFieldsWhenUpdatingPrice() {
            // Arrange
            BigDecimal newPrice = new BigDecimal("75.00");
            PriceUpdateRequest request = new PriceUpdateRequest(newPrice);

            when(productRepository.findById(EXISTING_PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Product result = productService.updatePrice(EXISTING_PRODUCT_ID, request);

            // Assert
            assertEquals(newPrice, result.getPrice());
            assertEquals("Test Product", result.getName());
            assertEquals("Test Description", result.getDescription());
            assertEquals(EXISTING_PRODUCT_ID, result.getId());
        }
    }

    @Nested
    @DisplayName("findById() Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return product when it exists")
        void shouldReturnProductWhenItExists() {
            // Arrange
            when(productRepository.findById(EXISTING_PRODUCT_ID)).thenReturn(Optional.of(existingProduct));

            // Act
            Product result = productService.findById(EXISTING_PRODUCT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(EXISTING_PRODUCT_ID, result.getId());
            assertEquals("Test Product", result.getName());
            verify(productRepository).findById(EXISTING_PRODUCT_ID);
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when product does not exist")
        void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() {
            // Arrange
            when(productRepository.findById(NON_EXISTING_PRODUCT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.findById(NON_EXISTING_PRODUCT_ID)
            );

            assertEquals("Product not found with id: " + NON_EXISTING_PRODUCT_ID, exception.getMessage());
            verify(productRepository).findById(NON_EXISTING_PRODUCT_ID);
        }
    }
}

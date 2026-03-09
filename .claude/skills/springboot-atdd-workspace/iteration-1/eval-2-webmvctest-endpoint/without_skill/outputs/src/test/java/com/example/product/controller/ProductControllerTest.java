package com.example.product.controller;

import com.example.product.dto.PriceUpdateRequest;
import com.example.product.exception.InvalidPriceException;
import com.example.product.exception.ProductNotFoundException;
import com.example.product.model.Product;
import com.example.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ProductController using @WebMvcTest.
 *
 * Tests focus on:
 * - HTTP layer behavior (status codes, response format)
 * - Validation of input (price must be positive)
 * - Error handling (product not found, invalid price)
 * - Integration between controller and service layer
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private static final String BASE_URL = "/api/products";
    private static final Long EXISTING_PRODUCT_ID = 1L;
    private static final Long NON_EXISTING_PRODUCT_ID = 999L;

    @Nested
    @DisplayName("PATCH /api/products/{id}/price - Update Price Tests")
    class UpdatePriceTests {

        @Test
        @DisplayName("Should return 200 OK with updated product when price is valid")
        void shouldReturn200OkWithUpdatedProductWhenPriceIsValid() throws Exception {
            // Arrange
            BigDecimal newPrice = new BigDecimal("99.99");
            PriceUpdateRequest request = new PriceUpdateRequest(newPrice);

            Product updatedProduct = new Product(
                EXISTING_PRODUCT_ID,
                "Test Product",
                newPrice,
                "Test Description"
            );

            when(productService.updatePrice(eq(EXISTING_PRODUCT_ID), any(PriceUpdateRequest.class)))
                .thenReturn(updatedProduct);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(EXISTING_PRODUCT_ID))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.name").value("Test Product"));

            // Verify service was called
            verify(productService).updatePrice(eq(EXISTING_PRODUCT_ID), any(PriceUpdateRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is null")
        void shouldReturn400BadRequestWhenPriceIsNull() throws Exception {
            // Arrange
            String requestBody = "{}"; // Missing price field

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());

            // Verify service was never called
            verify(productService, never()).updatePrice(any(), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is zero")
        void shouldReturn400BadRequestWhenPriceIsZero() throws Exception {
            // Arrange
            PriceUpdateRequest request = new PriceUpdateRequest(BigDecimal.ZERO);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

            // Verify service was never called
            verify(productService, never()).updatePrice(any(), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is negative")
        void shouldReturn400BadRequestWhenPriceIsNegative() throws Exception {
            // Arrange
            PriceUpdateRequest request = new PriceUpdateRequest(new BigDecimal("-10.00"));

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

            // Verify service was never called
            verify(productService, never()).updatePrice(any(), any());
        }

        @Test
        @DisplayName("Should return 404 Not Found when product does not exist")
        void shouldReturn404NotFoundWhenProductDoesNotExist() throws Exception {
            // Arrange
            PriceUpdateRequest request = new PriceUpdateRequest(new BigDecimal("50.00"));

            when(productService.updatePrice(eq(NON_EXISTING_PRODUCT_ID), any(PriceUpdateRequest.class)))
                .thenThrow(new ProductNotFoundException(NON_EXISTING_PRODUCT_ID));

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + NON_EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: " + NON_EXISTING_PRODUCT_ID));

            // Verify service was called
            verify(productService).updatePrice(eq(NON_EXISTING_PRODUCT_ID), any(PriceUpdateRequest.class));
        }

        @Test
        @DisplayName("Should accept minimum valid price (0.01)")
        void shouldAcceptMinimumValidPrice() throws Exception {
            // Arrange
            BigDecimal minPrice = new BigDecimal("0.01");
            PriceUpdateRequest request = new PriceUpdateRequest(minPrice);

            Product updatedProduct = new Product(
                EXISTING_PRODUCT_ID,
                "Test Product",
                minPrice,
                "Test Description"
            );

            when(productService.updatePrice(eq(EXISTING_PRODUCT_ID), any(PriceUpdateRequest.class)))
                .thenReturn(updatedProduct);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(0.01));
        }

        @Test
        @DisplayName("Should accept large valid price")
        void shouldAcceptLargeValidPrice() throws Exception {
            // Arrange
            BigDecimal largePrice = new BigDecimal("999999.99");
            PriceUpdateRequest request = new PriceUpdateRequest(largePrice);

            Product updatedProduct = new Product(
                EXISTING_PRODUCT_ID,
                "Test Product",
                largePrice,
                "Test Description"
            );

            when(productService.updatePrice(eq(EXISTING_PRODUCT_ID), any(PriceUpdateRequest.class)))
                .thenReturn(updatedProduct);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(999999.99));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request body is missing")
        void shouldReturn400BadRequestWhenRequestBodyIsMissing() throws Exception {
            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

            // Verify service was never called
            verify(productService, never()).updatePrice(any(), any());
        }

        @Test
        @DisplayName("Should return 415 Unsupported Media Type when content type is not JSON")
        void shouldReturn415UnsupportedMediaTypeWhenContentTypeIsNotJson() throws Exception {
            // Arrange
            PriceUpdateRequest request = new PriceUpdateRequest(new BigDecimal("50.00"));

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

            // Verify service was never called
            verify(productService, never()).updatePrice(any(), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is invalid JSON")
        void shouldReturn400BadRequestWhenPriceIsInvalidJson() throws Exception {
            // Arrange
            String invalidJson = "{ \"price\": \"not-a-number\" }";

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                .andExpect(status().isBadRequest());

            // Verify service was never called
            verify(productService, never()).updatePrice(any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id} - Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should return 200 OK with product when product exists")
        void shouldReturn200OkWithProductWhenProductExists() throws Exception {
            // Arrange
            Product product = new Product(
                EXISTING_PRODUCT_ID,
                "Test Product",
                new BigDecimal("29.99"),
                "Test Description"
            );

            when(productService.findById(EXISTING_PRODUCT_ID)).thenReturn(product);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + EXISTING_PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(EXISTING_PRODUCT_ID))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(29.99));

            // Verify service was called
            verify(productService).findById(EXISTING_PRODUCT_ID);
        }

        @Test
        @DisplayName("Should return 404 Not Found when product does not exist")
        void shouldReturn404NotFoundWhenProductDoesNotExist() throws Exception {
            // Arrange
            when(productService.findById(NON_EXISTING_PRODUCT_ID))
                .thenThrow(new ProductNotFoundException(NON_EXISTING_PRODUCT_ID));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + NON_EXISTING_PRODUCT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: " + NON_EXISTING_PRODUCT_ID));

            // Verify service was called
            verify(productService).findById(NON_EXISTING_PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle price with multiple decimal places")
        void shouldHandlePriceWithMultipleDecimalPlaces() throws Exception {
            // Arrange
            BigDecimal price = new BigDecimal("99.999");
            PriceUpdateRequest request = new PriceUpdateRequest(price);

            Product updatedProduct = new Product(
                EXISTING_PRODUCT_ID,
                "Test Product",
                price,
                "Test Description"
            );

            when(productService.updatePrice(eq(EXISTING_PRODUCT_ID), any(PriceUpdateRequest.class)))
                .thenReturn(updatedProduct);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle very small positive price")
        void shouldHandleVerySmallPositivePrice() throws Exception {
            // Arrange
            BigDecimal price = new BigDecimal("0.001");
            PriceUpdateRequest request = new PriceUpdateRequest(price);

            Product updatedProduct = new Product(
                EXISTING_PRODUCT_ID,
                "Test Product",
                price,
                "Test Description"
            );

            when(productService.updatePrice(eq(EXISTING_PRODUCT_ID), any(PriceUpdateRequest.class)))
                .thenReturn(updatedProduct);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return error response with correct structure on validation failure")
        void shouldReturnErrorResponseWithCorrectStructureOnValidationFailure() throws Exception {
            // Arrange
            PriceUpdateRequest request = new PriceUpdateRequest(null);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/" + EXISTING_PRODUCT_ID + "/price")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
        }
    }
}

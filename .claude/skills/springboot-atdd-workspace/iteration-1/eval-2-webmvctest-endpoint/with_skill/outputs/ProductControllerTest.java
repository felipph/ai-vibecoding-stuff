package com.example.product;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ATDD tests for Product price update endpoint.
 *
 * Acceptance Criteria:
 * [+] User can update product price with valid positive price for existing product
 * [-] System rejects update with negative price
 * [-] System rejects update with zero price
 * [-] System rejects update for non-existent product
 * [+] Updated product returns the new price in response
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private static final Long EXISTING_PRODUCT_ID = 1L;
    private static final Long NON_EXISTENT_PRODUCT_ID = 999L;
    private static final String UPDATE_PRICE_URL = "/api/products/{id}/price";

    @Nested
    @DisplayName("Update product price with valid positive price")
    class UpdatePriceWithValidPrice {

        @Test
        void updatePrice_withValidPositivePrice_returnsUpdatedProduct() throws Exception {
            // Arrange - [+] User can update product price with valid positive price for existing product
            BigDecimal newPrice = new BigDecimal("99.99");
            UpdatePriceRequest request = new UpdatePriceRequest(newPrice);

            Product updatedProduct = new Product(EXISTING_PRODUCT_ID, "Test Product", newPrice);

            when(productService.updatePrice(eq(EXISTING_PRODUCT_ID), any(BigDecimal.class)))
                    .thenReturn(updatedProduct);

            // Act & Assert
            mockMvc.perform(patch(UPDATE_PRICE_URL, EXISTING_PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(EXISTING_PRODUCT_ID))
                    .andExpect(jsonPath("$.price").value(99.99));

            verify(productService).updatePrice(EXISTING_PRODUCT_ID, newPrice);
        }

        @Test
        void updatePrice_withMinimumPositivePrice_returnsUpdatedProduct() throws Exception {
            // Arrange - [+] User can update product price with valid positive price (minimum edge case)
            BigDecimal newPrice = new BigDecimal("0.01");
            UpdatePriceRequest request = new UpdatePriceRequest(newPrice);

            Product updatedProduct = new Product(EXISTING_PRODUCT_ID, "Test Product", newPrice);

            when(productService.updatePrice(eq(EXISTING_PRODUCT_ID), any(BigDecimal.class)))
                    .thenReturn(updatedProduct);

            // Act & Assert
            mockMvc.perform(patch(UPDATE_PRICE_URL, EXISTING_PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(0.01));
        }
    }

    @Nested
    @DisplayName("Update product price with invalid price")
    class UpdatePriceWithInvalidPrice {

        @Test
        void updatePrice_withNegativePrice_returnsBadRequest() throws Exception {
            // Arrange - [-] System rejects update with negative price
            BigDecimal negativePrice = new BigDecimal("-10.00");
            UpdatePriceRequest request = new UpdatePriceRequest(negativePrice);

            // Act & Assert
            mockMvc.perform(patch(UPDATE_PRICE_URL, EXISTING_PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updatePrice_withZeroPrice_returnsBadRequest() throws Exception {
            // Arrange - [-] System rejects update with zero price
            BigDecimal zeroPrice = BigDecimal.ZERO;
            UpdatePriceRequest request = new UpdatePriceRequest(zeroPrice);

            // Act & Assert
            mockMvc.perform(patch(UPDATE_PRICE_URL, EXISTING_PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updatePrice_withNullPrice_returnsBadRequest() throws Exception {
            // Arrange - [-] System rejects update with null price (validation)
            String requestBody = "{}";

            // Act & Assert
            mockMvc.perform(patch(UPDATE_PRICE_URL, EXISTING_PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update product price for non-existent product")
    class UpdatePriceForNonExistentProduct {

        @Test
        void updatePrice_withNonExistentProductId_returnsNotFound() throws Exception {
            // Arrange - [-] System rejects update for non-existent product
            BigDecimal newPrice = new BigDecimal("50.00");
            UpdatePriceRequest request = new UpdatePriceRequest(newPrice);

            when(productService.updatePrice(eq(NON_EXISTENT_PRODUCT_ID), any(BigDecimal.class)))
                    .thenThrow(new ProductNotFoundException(NON_EXISTENT_PRODUCT_ID));

            // Act & Assert
            mockMvc.perform(patch(UPDATE_PRICE_URL, NON_EXISTENT_PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Product not found"))
                    .andExpect(jsonPath("$.productId").value(NON_EXISTENT_PRODUCT_ID));
        }
    }
}

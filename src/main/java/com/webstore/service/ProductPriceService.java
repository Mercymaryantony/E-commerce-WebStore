package com.webstore.service;

import com.webstore.dto.request.ProductPriceRequestDto;
import com.webstore.dto.response.ProductPriceResponseDto;

import java.math.BigInteger;
import java.util.List;

public interface ProductPriceService {

    ProductPriceResponseDto createProductPrice(ProductPriceRequestDto request);

    List<ProductPriceResponseDto> getAllProductPrices();  // Add this line

    ProductPriceResponseDto getProductPriceById(Integer id);

    ProductPriceResponseDto updateProductPrice(Integer id, BigInteger priceAmount);

    void deleteProductPrice(Integer id);
}
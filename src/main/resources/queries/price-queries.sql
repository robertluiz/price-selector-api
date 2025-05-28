SELECT * FROM PRICES
WHERE brand_id = :brandId 
  AND product_id = :productId 
  AND start_date <= :applicationDate 
  AND end_date >= :applicationDate 
ORDER BY priority DESC; 
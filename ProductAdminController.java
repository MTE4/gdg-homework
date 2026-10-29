package gdg.hongik.mission.Controller;

import gdg.hongik.mission.domain.Product;
import gdg.hongik.mission.store.ProductStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class ProductAdminController {

    // 1. 상품 등록: POST /api/admin/products
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createProduct(@RequestBody Map<String, Object> body) {
        Object nameValue = body.get("name");
        if (!(nameValue instanceof String) || ((String) nameValue).isBlank()) {
            throw new RuntimeException("상품명은 비어 있을 수 없습니다.");
        }
        String name = (String) nameValue;
        long price = toLong(body.get("price"));
        long stock = toLong(body.get("stock"));

        if (price < 0 || stock < 0) {
            throw new RuntimeException("가격과 재고는 0 이상이어야 합니다.");
        }
        for (Product product : ProductStore.products) {
            if (product.getName().equals(name)) {
                throw new RuntimeException("이미 존재하는 상품명입니다. (상품명: " + name + ")");
            }
        }

        Product product = new Product(ProductStore.sequence++, name, price, stock);
        ProductStore.products.add(product);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", product.getId());
        result.put("name", product.getName());
        result.put("price", product.getPrice());
        result.put("stock", product.getStock());
        return result;
    }

    // 2. 재고 추가: PATCH /api/admin/products/{productId}/stock
    @PatchMapping("/products/{productId}/stock")
    public Map<String, Object> addStock(@PathVariable Long productId,
                                        @RequestBody Map<String, Object> body) {
        long quantity = toLong(body.get("quantity"));
        if (quantity < 1) {
            throw new RuntimeException("추가할 수량은 1 이상이어야 합니다.");
        }

        Product product = findById(productId);
        product.setStock(product.getStock() + quantity);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", product.getName());
        result.put("stock", product.getStock());
        return result;
    }

    // 3. 상품 삭제: DELETE /api/admin/products
    @SuppressWarnings("unchecked")
    @DeleteMapping("/products")
    public Map<String, Object> deleteProducts(@RequestBody Map<String, Object> body) {
        List<Object> productIds = (List<Object>) body.get("productIds");
        if (productIds == null || productIds.isEmpty()) {
            throw new RuntimeException("삭제할 상품 ID가 없습니다.");
        }

        // 1단계: 없는 ID가 있는지 먼저 검사 (하나라도 없으면 아무것도 삭제하지 않음)
        List<Long> ids = new ArrayList<>();
        for (Object value : productIds) {
            Long id = toLong(value);
            findById(id);
            ids.add(id);
        }

        // 2단계: 삭제
        ProductStore.products.removeIf(product -> ids.contains(product.getId()));

        // 남은 상품 목록 응답
        List<Map<String, Object>> remaining = new ArrayList<>();
        for (Product product : ProductStore.products) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", product.getName());
            item.put("stock", product.getStock());
            remaining.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("products", remaining);
        return result;
    }

    private Product findById(Long productId) {
        for (Product product : ProductStore.products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        throw new RuntimeException("존재하지 않는 상품입니다. (상품 ID: " + productId + ")");
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new RuntimeException("숫자 값이 필요합니다.");
    }
}

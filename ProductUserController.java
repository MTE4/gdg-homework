package gdg.hongik.mission.Controller;

import gdg.hongik.mission.domain.Product;
import gdg.hongik.mission.store.ProductStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ProductUserController {

    // 1. 상품 조회: GET /api/products?name=키보드
    @GetMapping("/products")
    public Map<String, Object> getProduct(@RequestParam String name) {
        for (Product product : ProductStore.products) {
            if (product.getName().equals(name)) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("productId", product.getId());
                result.put("name", product.getName());
                result.put("price", product.getPrice());
                result.put("stock", product.getStock());
                return result;
            }
        }
        throw new RuntimeException("존재하지 않는 상품입니다. (상품명: " + name + ")");
    }

    // 2. 상품 구매: POST /api/orders
    @SuppressWarnings("unchecked")
    @PostMapping("/orders")
    public Map<String, Object> purchase(@RequestBody Map<String, Object> body) {
        List<Map<String, Object>> requestItems = (List<Map<String, Object>>) body.get("items");
        if (requestItems == null || requestItems.isEmpty()) {
            throw new RuntimeException("구매할 상품이 없습니다.");
        }

        // 1단계: 전부 구매 가능한지 먼저 검사 (하나라도 실패하면 아무것도 차감하지 않음)
        Set<Long> seenIds = new HashSet<>();
        for (Map<String, Object> item : requestItems) {
            Long productId = toLong(item.get("productId"));
            long quantity = toLong(item.get("quantity"));

            if (quantity < 1) {
                throw new RuntimeException("구매 수량은 1 이상이어야 합니다.");
            }
            if (!seenIds.add(productId)) {
                throw new RuntimeException("같은 상품을 중복해서 요청할 수 없습니다. (상품 ID: " + productId + ")");
            }

            Product product = findById(productId);
            if (product.getStock() < quantity) {
                throw new RuntimeException("재고가 부족합니다. (상품: " + product.getName()
                        + ", 요청: " + quantity + ", 재고: " + product.getStock() + ")");
            }
        }

        // 2단계: 재고 차감 + 응답 만들기
        long totalPrice = 0;
        List<Map<String, Object>> boughtItems = new ArrayList<>();
        for (Map<String, Object> item : requestItems) {
            Product product = findById(toLong(item.get("productId")));
            long quantity = toLong(item.get("quantity"));

            product.setStock(product.getStock() - quantity);

            long price = product.getPrice() * quantity;
            totalPrice += price;

            Map<String, Object> bought = new LinkedHashMap<>();
            bought.put("name", product.getName());
            bought.put("quantity", quantity);
            bought.put("price", price);
            boughtItems.add(bought);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalPrice", totalPrice);
        result.put("items", boughtItems);
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

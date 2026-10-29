package gdg.hongik.mission.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {

    private Long id;       // 상품 번호
    private String name;   // 상품 이름
    private long price;    // 1개 가격
    private long stock;    // 남은 재고

    public Product(Long id, String name, long price, long stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}

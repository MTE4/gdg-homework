package gdg.hongik.mission.store;

import gdg.hongik.mission.domain.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductStore {
    public static List<Product> products = new ArrayList<>();
    public static Long sequence = 1L;
}


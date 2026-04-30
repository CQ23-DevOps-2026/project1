package com.yas.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.inventory.model.Stock;
import com.yas.inventory.model.StockHistory;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockHistoryRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryListVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StockHistoryServiceTest {

    private StockHistoryRepository stockHistoryRepository;
    private ProductService productService;

    private StockHistoryService stockHistoryService;

    @BeforeEach
    void setUp() {
        stockHistoryRepository = mock(StockHistoryRepository.class);
        productService = mock(ProductService.class);
        stockHistoryService = new StockHistoryService(stockHistoryRepository, productService);
    }

    @Test
    void createStockHistories_shouldCreateOnlyForMatchedStockIds() {
        Warehouse wh = Warehouse.builder().id(1L).name("WH1").addressId(100L).build();

        Stock s1 = Stock.builder().id(11L).productId(101L).quantity(5L).reservedQuantity(0L).warehouse(wh).build();
        Stock s2 = Stock.builder().id(22L).productId(202L).quantity(10L).reservedQuantity(0L).warehouse(wh).build();

        List<StockQuantityVm> quantities = List.of(
                new StockQuantityVm(11L, 3L, "note1"),
                new StockQuantityVm(999L, 1L, "ignored"));

        assertDoesNotThrow(() -> stockHistoryService.createStockHistories(List.of(s1, s2), quantities));

        ArgumentCaptor<List<StockHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockHistoryRepository).saveAll(captor.capture());

        List<StockHistory> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getProductId()).isEqualTo(101L);
        assertThat(saved.getFirst().getAdjustedQuantity()).isEqualTo(3L);
        assertThat(saved.getFirst().getNote()).isEqualTo("note1");
        assertThat(saved.getFirst().getWarehouse().getId()).isEqualTo(1L);
    }

    @Test
    void getStockHistories_shouldMapToVmWithProductName() {
        Long productId = 101L;
        Long warehouseId = 1L;

        StockHistory history = StockHistory.builder()
                .id(1L)
                .productId(productId)
                .adjustedQuantity(5L)
                .note("note")
                .warehouse(Warehouse.builder().id(warehouseId).name("WH1").addressId(100L).build())
                .build();

        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(productId, warehouseId))
                .thenReturn(List.of(history));
        when(productService.getProduct(eq(productId))).thenReturn(new ProductInfoVm(productId, "P1", "SKU1", true));

        StockHistoryListVm vm = stockHistoryService.getStockHistories(productId, warehouseId);

        assertThat(vm.data()).hasSize(1);
        assertThat(vm.data().getFirst().productName()).isEqualTo("P1");
        assertThat(vm.data().getFirst().adjustedQuantity()).isEqualTo(5L);

        verify(productService).getProduct(productId);
        verify(stockHistoryRepository).findByProductIdAndWarehouseIdOrderByCreatedOnDesc(productId, warehouseId);
    }
}

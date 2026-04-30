package com.yas.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.StockExistingException;
import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockPostVm;
import com.yas.inventory.viewmodel.stock.StockQuantityUpdateVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stock.StockVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StockServiceTest {

    private WarehouseRepository warehouseRepository;
    private StockRepository stockRepository;
    private ProductService productService;
    private WarehouseService warehouseService;
    private StockHistoryService stockHistoryService;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        warehouseRepository = mock(WarehouseRepository.class);
        stockRepository = mock(StockRepository.class);
        productService = mock(ProductService.class);
        warehouseService = mock(WarehouseService.class);
        stockHistoryService = mock(StockHistoryService.class);
        stockService = new StockService(
                warehouseRepository,
                stockRepository,
                productService,
                warehouseService,
                stockHistoryService);
    }

    @Test
    void addProductIntoWarehouse_whenStockAlreadyExists_shouldThrow() {
        StockPostVm postVm = new StockPostVm(10L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 10L)).thenReturn(true);

        assertThrows(StockExistingException.class, () -> stockService.addProductIntoWarehouse(List.of(postVm)));
        verify(stockRepository, never()).saveAll(any());
    }

    @Test
    void addProductIntoWarehouse_whenProductNotFound_shouldThrow() {
        StockPostVm postVm = new StockPostVm(10L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 10L)).thenReturn(false);
        when(productService.getProduct(10L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> stockService.addProductIntoWarehouse(List.of(postVm)));
        verify(stockRepository, never()).saveAll(any());
    }

    @Test
    void addProductIntoWarehouse_whenWarehouseNotFound_shouldThrow() {
        StockPostVm postVm = new StockPostVm(10L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 10L)).thenReturn(false);
        when(productService.getProduct(10L)).thenReturn(new ProductInfoVm(10L, "P1", "SKU1", true));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> stockService.addProductIntoWarehouse(List.of(postVm)));
        verify(stockRepository, never()).saveAll(any());
    }

    @Test
    void addProductIntoWarehouse_whenNormalCase_shouldSaveStocksWithZeroQuantities() {
        StockPostVm postVm1 = new StockPostVm(10L, 1L);
        StockPostVm postVm2 = new StockPostVm(20L, 1L);

        when(stockRepository.existsByWarehouseIdAndProductId(1L, 10L)).thenReturn(false);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 20L)).thenReturn(false);

        when(productService.getProduct(10L)).thenReturn(new ProductInfoVm(10L, "P1", "SKU1", true));
        when(productService.getProduct(20L)).thenReturn(new ProductInfoVm(20L, "P2", "SKU2", true));

        Warehouse wh = Warehouse.builder().id(1L).name("WH1").addressId(100L).build();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(wh));

        assertDoesNotThrow(() -> stockService.addProductIntoWarehouse(List.of(postVm1, postVm2)));

        ArgumentCaptor<List<Stock>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockRepository).saveAll(captor.capture());
        List<Stock> saved = captor.getValue();

        assertThat(saved).hasSize(2);
        assertThat(saved).allSatisfy(s -> {
            assertThat(s.getQuantity()).isEqualTo(0L);
            assertThat(s.getReservedQuantity()).isEqualTo(0L);
            assertThat(s.getWarehouse().getId()).isEqualTo(1L);
        });
        assertThat(saved).extracting(Stock::getProductId).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void getStocksByWarehouseIdAndProductNameAndSku_shouldReturnStockVmsMappedFromProductInfo() {
        Long warehouseId = 1L;
        List<ProductInfoVm> products = List.of(
                new ProductInfoVm(10L, "P1", "SKU1", true),
                new ProductInfoVm(20L, "P2", "SKU2", true));
        when(warehouseService.getProductWarehouse(warehouseId, "name", "sku", FilterExistInWhSelection.YES))
                .thenReturn(products);

        Warehouse wh = Warehouse.builder().id(warehouseId).name("WH").addressId(100L).build();
        Stock s1 = Stock.builder().id(1L).productId(10L).quantity(5L).reservedQuantity(1L).warehouse(wh).build();
        Stock s2 = Stock.builder().id(2L).productId(20L).quantity(7L).reservedQuantity(0L).warehouse(wh).build();

        when(stockRepository.findByWarehouseIdAndProductIdIn(eq(warehouseId), any(List.class)))
                .thenReturn(List.of(s1, s2));

        List<StockVm> result = stockService.getStocksByWarehouseIdAndProductNameAndSku(warehouseId, "name", "sku");

        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(vm -> {
            if (vm.productId().equals(10L)) {
                assertEquals("P1", vm.productName());
                assertEquals("SKU1", vm.productSku());
            }
        });
    }

    @Test
    void updateProductQuantityInStock_whenStocksEmpty_shouldNotUpdateProductQuantity() {
        List<StockQuantityVm> quantities = List.of(new StockQuantityVm(1L, 1L, "n"));
        when(stockRepository.findAllById(any())).thenReturn(List.of());

        assertDoesNotThrow(() -> stockService.updateProductQuantityInStock(new StockQuantityUpdateVm(quantities)));

        verify(stockRepository).saveAll(eq(List.of()));
        verify(stockHistoryService).createStockHistories(eq(List.of()), eq(quantities));
        verify(productService, never()).updateProductQuantity(any());
    }

    @Test
    void updateProductQuantityInStock_whenQuantityNull_shouldTreatAsZero() {
        Warehouse wh = Warehouse.builder().id(1L).name("WH").addressId(100L).build();
        Stock stock = Stock.builder().id(1L).productId(10L).quantity(5L).reservedQuantity(0L).warehouse(wh).build();

        List<StockQuantityVm> quantities = List.of(new StockQuantityVm(1L, null, "n"));
        when(stockRepository.findAllById(any())).thenReturn(List.of(stock));

        stockService.updateProductQuantityInStock(new StockQuantityUpdateVm(quantities));

        assertThat(stock.getQuantity()).isEqualTo(5L);
        verify(stockRepository).saveAll(any());
        verify(stockHistoryService).createStockHistories(any(), eq(quantities));
        verify(productService).updateProductQuantity(any());
    }

    @Test
    void updateProductQuantityInStock_whenInvalidAdjustedQuantity_shouldThrowBadRequest() {
        Warehouse wh = Warehouse.builder().id(1L).name("WH").addressId(100L).build();
        // Use a negative stock quantity to hit the current validation branch.
        Stock stock = Stock.builder().id(1L).productId(10L).quantity(-10L).reservedQuantity(0L).warehouse(wh).build();

        List<StockQuantityVm> quantities = List.of(new StockQuantityVm(1L, -1L, "n"));
        when(stockRepository.findAllById(any())).thenReturn(List.of(stock));

        assertThrows(BadRequestException.class,
                () -> stockService.updateProductQuantityInStock(new StockQuantityUpdateVm(quantities)));

        verify(stockRepository, never()).saveAll(any());
        verify(stockHistoryService, never()).createStockHistories(any(), any());
        verify(productService, never()).updateProductQuantity(any());
    }
}

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

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.address.AddressDetailVm;
import com.yas.inventory.viewmodel.address.AddressPostVm;
import com.yas.inventory.viewmodel.address.AddressVm;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseDetailVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseListGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehousePostVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

class WarehouseServiceTest {

    private WarehouseRepository warehouseRepository;
    private StockRepository stockRepository;
    private ProductService productService;
    private LocationService locationService;

    private WarehouseService warehouseService;

    @BeforeEach
    void setUp() {
        warehouseRepository = mock(WarehouseRepository.class);
        stockRepository = mock(StockRepository.class);
        productService = mock(ProductService.class);
        locationService = mock(LocationService.class);
        warehouseService = new WarehouseService(warehouseRepository, stockRepository, productService, locationService);
    }

    @Test
    void getProductWarehouse_whenProductIdsExist_shouldMarkExistFlag() {
        Long warehouseId = 1L;
        List<Long> productIdsInWarehouse = List.of(10L, 20L);
        when(stockRepository.getProductIdsInWarehouse(warehouseId)).thenReturn(productIdsInWarehouse);

        List<ProductInfoVm> filtered = List.of(
                new ProductInfoVm(10L, "P1", "SKU1", false),
                new ProductInfoVm(30L, "P3", "SKU3", false));
        when(productService.filterProducts(eq("name"), eq("sku"), eq(productIdsInWarehouse),
                eq(FilterExistInWhSelection.YES)))
                .thenReturn(filtered);

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(
                warehouseId,
                "name",
                "sku",
                FilterExistInWhSelection.YES);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().id()).isEqualTo(10L);
        assertThat(result.getFirst().existInWh()).isTrue();
        assertThat(result.get(1).id()).isEqualTo(30L);
        assertThat(result.get(1).existInWh()).isFalse();
    }

    @Test
    void getProductWarehouse_whenNoProductIds_shouldReturnFilteredListAsIs() {
        Long warehouseId = 1L;
        when(stockRepository.getProductIdsInWarehouse(warehouseId)).thenReturn(List.of());

        List<ProductInfoVm> filtered = List.of(new ProductInfoVm(10L, "P1", "SKU1", true));
        when(productService.filterProducts(eq("name"), eq("sku"), eq(List.of()), eq(FilterExistInWhSelection.NO)))
                .thenReturn(filtered);

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(
                warehouseId,
                "name",
                "sku",
                FilterExistInWhSelection.NO);

        assertThat(result).isEqualTo(filtered);
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> warehouseService.findById(99L));
    }

    @Test
    void findById_whenFound_shouldReturnWarehouseDetailVm() {
        Warehouse wh = Warehouse.builder().id(1L).name("WH1").addressId(100L).build();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(wh));

        AddressDetailVm address = AddressDetailVm.builder()
                .id(100L)
                .contactName("John")
                .phone("123")
                .addressLine1("L1")
                .addressLine2("L2")
                .city("City")
                .zipCode("12345")
                .districtId(10L)
                .stateOrProvinceId(20L)
                .countryId(30L)
                .build();
        when(locationService.getAddressById(100L)).thenReturn(address);

        WarehouseDetailVm vm = warehouseService.findById(1L);

        assertEquals(1L, vm.id());
        assertEquals("WH1", vm.name());
        assertEquals("John", vm.contactName());
        assertEquals(30L, vm.countryId());
    }

    @Test
    void create_whenNameExists_shouldThrowDuplicated() {
        WarehousePostVm postVm = new WarehousePostVm(
                "id-1",
                "WH1",
                "John",
                "123",
                "L1",
                "L2",
                "City",
                "12345",
                10L,
                20L,
                30L);
        when(warehouseRepository.existsByName("WH1")).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> warehouseService.create(postVm));
        verify(locationService, never()).createAddress(any(AddressPostVm.class));
    }

    @Test
    void create_whenNormalCase_shouldCreateAddressAndSaveWarehouse() {
        WarehousePostVm postVm = new WarehousePostVm(
                "id-1",
                "WH1",
                "John",
                "123",
                "L1",
                "L2",
                "City",
                "12345",
                10L,
                20L,
                30L);
        when(warehouseRepository.existsByName("WH1")).thenReturn(false);

        AddressVm created = AddressVm.builder().id(100L).build();
        when(locationService.createAddress(any(AddressPostVm.class))).thenReturn(created);

        Warehouse saved = Warehouse.builder().id(1L).name("WH1").addressId(100L).build();
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);

        Warehouse result = warehouseService.create(postVm);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAddressId()).isEqualTo(100L);

        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("WH1");
        assertThat(captor.getValue().getAddressId()).isEqualTo(100L);
    }

    @Test
    void update_whenWarehouseNotFound_shouldThrow() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());
        WarehousePostVm postVm = new WarehousePostVm(
                "id-1",
                "WH1",
                "John",
                "123",
                "L1",
                "L2",
                "City",
                "12345",
                10L,
                20L,
                30L);

        assertThrows(NotFoundException.class, () -> warehouseService.update(postVm, 1L));
    }

    @Test
    void update_whenDuplicatedName_shouldThrowDuplicated() {
        Warehouse wh = Warehouse.builder().id(1L).name("OLD").addressId(100L).build();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(wh));
        when(warehouseRepository.existsByNameWithDifferentId("WH1", 1L)).thenReturn(true);

        WarehousePostVm postVm = new WarehousePostVm(
                "id-1",
                "WH1",
                "John",
                "123",
                "L1",
                "L2",
                "City",
                "12345",
                10L,
                20L,
                30L);

        assertThrows(DuplicatedException.class, () -> warehouseService.update(postVm, 1L));
        verify(locationService, never()).updateAddress(eq(100L), any(AddressPostVm.class));
    }

    @Test
    void update_whenNormalCase_shouldUpdateAddressAndSaveWarehouse() {
        Warehouse wh = Warehouse.builder().id(1L).name("OLD").addressId(100L).build();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(wh));
        when(warehouseRepository.existsByNameWithDifferentId("WH1", 1L)).thenReturn(false);

        WarehousePostVm postVm = new WarehousePostVm(
                "id-1",
                "WH1",
                "John",
                "123",
                "L1",
                "L2",
                "City",
                "12345",
                10L,
                20L,
                30L);

        assertDoesNotThrow(() -> warehouseService.update(postVm, 1L));
        verify(locationService).updateAddress(eq(100L), any(AddressPostVm.class));
        verify(warehouseRepository).save(any(Warehouse.class));
        assertThat(wh.getName()).isEqualTo("WH1");
    }

    @Test
    void delete_whenWarehouseNotFound_shouldThrow() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> warehouseService.delete(1L));
    }

    @Test
    void delete_whenNormalCase_shouldDeleteWarehouseAndAddress() {
        Warehouse wh = Warehouse.builder().id(1L).name("WH1").addressId(100L).build();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(wh));

        assertDoesNotThrow(() -> warehouseService.delete(1L));

        verify(warehouseRepository).deleteById(1L);
        verify(locationService).deleteAddress(100L);
    }

    @Test
    void getPageableWarehouses_shouldMapPageCorrectly() {
        @SuppressWarnings("unchecked")
        Page<Warehouse> page = mock(Page.class);
        List<Warehouse> content = List.of(
                Warehouse.builder().id(1L).name("WH1").addressId(100L).build(),
                Warehouse.builder().id(2L).name("WH2").addressId(200L).build());
        when(page.getContent()).thenReturn(content);
        when(page.getNumber()).thenReturn(0);
        when(page.getSize()).thenReturn(10);
        when(page.getTotalElements()).thenReturn(2L);
        when(page.getTotalPages()).thenReturn(1);
        when(page.isLast()).thenReturn(true);
        when(warehouseRepository.findAll(any(Pageable.class))).thenReturn(page);

        WarehouseListGetVm vm = warehouseService.getPageableWarehouses(0, 10);

        assertThat(vm.warehouseContent()).hasSize(2);
        assertThat(vm.pageNo()).isEqualTo(0);
        assertThat(vm.isLast()).isTrue();
    }
}

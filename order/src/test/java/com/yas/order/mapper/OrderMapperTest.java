package com.yas.order.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.order.model.csv.OrderItemCsv;
import com.yas.order.viewmodel.order.OrderBriefVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = Mappers.getMapper(OrderMapper.class);
    }

    @Test
    void toCsv_ValidOrderBriefVm_ReturnsOrderItemCsv() {
        // Arrange
        OrderAddressVm billingAddress = OrderAddressVm.builder()
                .phone("123456789")
                .build();
        
        OrderBriefVm orderBriefVm = new OrderBriefVm(
                1L,
                "customer@test.com",
                billingAddress,
                new BigDecimal("100.00"),
                null,
                null,
                null,
                null,
                ZonedDateTime.now()
        );

        // Act
        OrderItemCsv result = orderMapper.toCsv(orderBriefVm);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPhone()).isEqualTo("123456789");
    }

    @Test
    void toCsv_NullOrderBriefVm_ReturnsNull() {
        // Act
        OrderItemCsv result = orderMapper.toCsv(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toCsv_NullBillingAddressVm_ReturnsOrderItemCsvWithNullPhone() {
        // Arrange
        OrderBriefVm orderBriefVm = new OrderBriefVm(
                1L,
                "customer@test.com",
                null,
                new BigDecimal("100.00"),
                null,
                null,
                null,
                null,
                ZonedDateTime.now()
        );

        // Act
        OrderItemCsv result = orderMapper.toCsv(orderBriefVm);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPhone()).isNull();
    }
}


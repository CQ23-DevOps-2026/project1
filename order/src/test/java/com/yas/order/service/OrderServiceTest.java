package com.yas.order.service;

import static com.yas.order.utils.SecurityContextUtils.setSubjectUpSecurityContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.mapper.OrderMapper;
import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.model.request.OrderRequest;
import com.yas.order.repository.OrderItemRepository;
import com.yas.order.repository.OrderRepository;
import com.yas.order.viewmodel.order.*;
import com.yas.order.viewmodel.orderaddress.OrderAddressPostVm;
import com.yas.order.viewmodel.product.ProductVariationVm;
import org.instancio.Instancio;
import static org.instancio.Select.field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;

import java.time.ZonedDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        order = Instancio.of(Order.class)
                .set(field(Order::getId), 1L)
                .create();
        orderItem = Instancio.of(OrderItem.class)
                .set(field(OrderItem::getOrderId), 1L)
                .create();
    }

    @Test
    void createOrder_Success_ReturnsOrderVm() {
        // Arrange
        OrderPostVm orderPostVm = Instancio.create(OrderPostVm.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });
        
        // Act
        OrderVm result = orderService.createOrder(orderPostVm);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anySet());
        verify(productService).subtractProductStockQuantity(any(OrderVm.class));
        verify(cartService).deleteCartItems(any(OrderVm.class));
        verify(promotionService).updateUsagePromotion(anyList());
    }

    @Test
    void getOrderWithItemsById_Exists_ReturnsOrderVm() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of(orderItem));

        // Act
        OrderVm result = orderService.getOrderWithItemsById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getOrderWithItemsById_NotExists_ThrowsNotFoundException() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.getOrderWithItemsById(1L));
    }

    @Test
    void getAllOrder_ReturnsOrderListVm() {
        // Arrange
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);

        // Act
        OrderListVm result = orderService.getAllOrder(
                Pair.of(ZonedDateTime.now(), ZonedDateTime.now()),
                "product",
                List.of(OrderStatus.PENDING),
                Pair.of("country", "phone"),
                "email@test.com",
                Pair.of(0, 10)
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void getLatestOrders_ValidCount_ReturnsOrderBriefVms() {
        // Arrange
        when(orderRepository.getLatestOrders(any(Pageable.class))).thenReturn(List.of(order));

        // Act
        List<OrderBriefVm> result = orderService.getLatestOrders(5);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getLatestOrders_InvalidCount_ReturnsEmptyList() {
        // Act
        List<OrderBriefVm> result = orderService.getLatestOrders(0);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void updateOrderPaymentStatus_Exists_ReturnsUpdatedPaymentOrderStatusVm() {
        // Arrange
        PaymentOrderStatusVm request = PaymentOrderStatusVm.builder()
                .orderId(1L)
                .paymentId(100L)
                .paymentStatus("COMPLETED")
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        PaymentOrderStatusVm result = orderService.updateOrderPaymentStatus(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(order);
    }

    @Test
    void rejectOrder_Exists_UpdatesStatus() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.rejectOrder(1L, "reason");

        // Assert
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REJECT);
        verify(orderRepository).save(order);
    }

    @Test
    void acceptOrder_Exists_UpdatesStatus() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.acceptOrder(1L);

        // Assert
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        verify(orderRepository).save(order);
    }

    @Test
    void isOrderCompletedWithUserIdAndProductId_ReturnsVm() {
        // Arrange
        setSubjectUpSecurityContext("user-id");
        when(productService.getProductVariations(10L)).thenReturn(List.of());
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));

        // Act
        OrderExistsByProductAndUserGetVm result = orderService.isOrderCompletedWithUserIdAndProductId(10L);

        // Assert
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    void getMyOrders_ReturnsOrderGetVms() {
        // Arrange
        setSubjectUpSecurityContext("user-id");
        when(orderRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(order));

        // Act
        List<OrderGetVm> result = orderService.getMyOrders("product", OrderStatus.PENDING);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void findOrderVmByCheckoutId_Exists_ReturnsOrderGetVm() {
        // Arrange
        when(orderRepository.findByCheckoutId("checkout-id")).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(anyLong())).thenReturn(List.of(orderItem));

        // Act
        OrderGetVm result = orderService.findOrderVmByCheckoutId("checkout-id");

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void findOrderByCheckoutId_NotExists_ThrowsNotFoundException() {
        // Arrange
        when(orderRepository.findByCheckoutId("invalid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.findOrderByCheckoutId("invalid"));
    }

    @Test
    void exportCsv_EmptyList_ReturnsEmptyCsv() throws Exception {
        // Arrange
        OrderRequest request = new OrderRequest();
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        byte[] result = orderService.exportCsv(request);

        // Assert
        assertThat(result).isNotEmpty(); // Should contain header at least
    }

    @Test
    void exportCsv_WithData_ReturnsCsvBytes() throws Exception {
        // Arrange
        OrderRequest request = new OrderRequest();
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toCsv(any(OrderBriefVm.class))).thenReturn(Instancio.create(com.yas.order.model.csv.OrderItemCsv.class));

        // Act
        byte[] result = orderService.exportCsv(request);

        // Assert
        assertThat(result).isNotEmpty();
    }
}

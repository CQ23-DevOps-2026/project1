package com.yas.customer.service;

import static com.yas.customer.util.SecurityContextUtils.setUpSecurityContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.customer.model.UserAddress;
import com.yas.customer.repository.UserAddressRepository;
import com.yas.customer.viewmodel.address.ActiveAddressVm;
import com.yas.customer.viewmodel.address.AddressDetailVm;
import com.yas.customer.viewmodel.address.AddressPostVm;
import com.yas.customer.viewmodel.address.AddressVm;
import com.yas.customer.viewmodel.useraddress.UserAddressVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

class UserAddressServiceTest {

    private static final String USER_ID = "user-1";

    private UserAddressRepository userAddressRepository;

    private LocationService locationService;

    private UserAddressService userAddressService;

    @BeforeEach
    void setUp() {
        userAddressRepository = mock(UserAddressRepository.class);
        locationService = mock(LocationService.class);
        userAddressService = new UserAddressService(userAddressRepository, locationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetUserAddressList_whenAnonymousUser_thenThrowAccessDeniedException() {
        setUpSecurityContext("anonymousUser");

        assertThrows(AccessDeniedException.class, () -> userAddressService.getUserAddressList());
    }

    @Test
    void testGetUserAddressList_whenAddressesExist_thenReturnSortedActiveAddresses() {
        setUpSecurityContext(USER_ID);
        List<UserAddress> userAddresses = List.of(
            UserAddress.builder().id(1L).userId(USER_ID).addressId(11L).isActive(false).build(),
            UserAddress.builder().id(2L).userId(USER_ID).addressId(22L).isActive(true).build()
        );
        when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(userAddresses);
        when(locationService.getAddressesByIdList(List.of(11L, 22L))).thenReturn(List.of(
            addressDetailVm(11L, "First Address"),
            addressDetailVm(22L, "Default Address")
        ));

        List<ActiveAddressVm> result = userAddressService.getUserAddressList();

        assertThat(result).hasSize(2);
        assertTrue(result.get(0).isActive());
        assertThat(result.get(0).id()).isEqualTo(22L);
        assertFalse(result.get(1).isActive());
        assertThat(result.get(1).id()).isEqualTo(11L);
    }

    @Test
    void testGetAddressDefault_whenAnonymousUser_thenThrowAccessDeniedException() {
        setUpSecurityContext("anonymousUser");

        assertThrows(AccessDeniedException.class, () -> userAddressService.getAddressDefault());
    }

    @Test
    void testGetAddressDefault_whenNoDefaultAddress_thenThrowNotFoundException() {
        setUpSecurityContext(USER_ID);
        when(userAddressRepository.findByUserIdAndIsActiveTrue(USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userAddressService.getAddressDefault());
    }

    @Test
    void testGetAddressDefault_whenDefaultAddressExists_thenReturnAddressDetailVm() {
        setUpSecurityContext(USER_ID);
        UserAddress userAddress = UserAddress.builder().id(1L).userId(USER_ID).addressId(33L).isActive(true).build();
        AddressDetailVm addressDetailVm = addressDetailVm(33L, "Default Address");
        when(userAddressRepository.findByUserIdAndIsActiveTrue(USER_ID)).thenReturn(Optional.of(userAddress));
        when(locationService.getAddressById(33L)).thenReturn(addressDetailVm);

        AddressDetailVm result = userAddressService.getAddressDefault();

        assertThat(result).isEqualTo(addressDetailVm);
    }

    @Test
    void testCreateAddress_whenFirstAddress_thenSaveAsActive() {
        setUpSecurityContext(USER_ID);
        AddressPostVm request = addressPostVm();
        AddressVm createdAddress = addressVm(44L);
        UserAddress savedAddress = UserAddress.builder().id(10L).userId(USER_ID).addressId(44L).isActive(true).build();
        when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
        when(locationService.createAddress(request)).thenReturn(createdAddress);
        when(userAddressRepository.save(org.mockito.ArgumentMatchers.any(UserAddress.class))).thenReturn(savedAddress);

        UserAddressVm result = userAddressService.createAddress(request);

        assertThat(result.userId()).isEqualTo(USER_ID);
        assertTrue(result.isActive());
        assertThat(result.addressGetVm().id()).isEqualTo(44L);
    }

    @Test
    void testCreateAddress_whenUserAlreadyHasAddress_thenSaveAsInactive() {
        setUpSecurityContext(USER_ID);
        AddressPostVm request = addressPostVm();
        AddressVm createdAddress = addressVm(55L);
        UserAddress existingAddress = UserAddress.builder().id(1L).userId(USER_ID).addressId(44L).isActive(true).build();
        UserAddress savedAddress = UserAddress.builder().id(11L).userId(USER_ID).addressId(55L).isActive(false).build();
        when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(existingAddress));
        when(locationService.createAddress(request)).thenReturn(createdAddress);
        when(userAddressRepository.save(org.mockito.ArgumentMatchers.any(UserAddress.class))).thenReturn(savedAddress);

        UserAddressVm result = userAddressService.createAddress(request);

        assertFalse(result.isActive());
        assertThat(result.addressGetVm().id()).isEqualTo(55L);
    }

    @Test
    void testDeleteAddress_whenAddressDoesNotExist_thenThrowNotFoundException() {
        setUpSecurityContext(USER_ID);
        when(userAddressRepository.findOneByUserIdAndAddressId(USER_ID, 66L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userAddressService.deleteAddress(66L));
    }

    @Test
    void testDeleteAddress_whenAddressExists_thenDeleteAddress() {
        setUpSecurityContext(USER_ID);
        UserAddress userAddress = UserAddress.builder().id(12L).userId(USER_ID).addressId(66L).isActive(false).build();
        when(userAddressRepository.findOneByUserIdAndAddressId(USER_ID, 66L)).thenReturn(userAddress);

        userAddressService.deleteAddress(66L);

        verify(userAddressRepository).delete(userAddress);
    }

    @Test
    void testChooseDefaultAddress_whenAddressesExist_thenToggleActiveFlags() {
        setUpSecurityContext(USER_ID);
        UserAddress first = UserAddress.builder().id(1L).userId(USER_ID).addressId(11L).isActive(true).build();
        UserAddress second = UserAddress.builder().id(2L).userId(USER_ID).addressId(22L).isActive(false).build();
        when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(first, second));

        userAddressService.chooseDefaultAddress(22L);

        assertFalse(first.getIsActive());
        assertTrue(second.getIsActive());
        verify(userAddressRepository).saveAll(List.of(first, second));
    }

    private AddressDetailVm addressDetailVm(Long id, String contactName) {
        return new AddressDetailVm(
            id,
            contactName,
            "0123456789",
            "123 Street",
            "Ho Chi Minh",
            "70000",
            1L,
            "District 1",
            2L,
            "HCM",
            3L,
            "Vietnam"
        );
    }

    private AddressPostVm addressPostVm() {
        return new AddressPostVm(
            "John Doe",
            "0123456789",
            "123 Street",
            "Ho Chi Minh",
            "70000",
            1L,
            2L,
            3L
        );
    }

    private AddressVm addressVm(Long id) {
        return new AddressVm(
            id,
            "John Doe",
            "0123456789",
            "123 Street",
            "Ho Chi Minh",
            "70000",
            1L,
            2L,
            3L
        );
    }
}

package com.yas.rating.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class RatingTest {

    @Test
    void testRatingBuilderAndGetters() {
        Rating rating = Rating.builder()
                .id(1L)
                .content("Good")
                .ratingStar(5)
                .productId(100L)
                .productName("Product A")
                .firstName("John")
                .lastName("Doe")
                .build();

        assertThat(rating.getId()).isEqualTo(1L);
        assertThat(rating.getContent()).isEqualTo("Good");
        assertThat(rating.getRatingStar()).isEqualTo(5);
        assertThat(rating.getProductId()).isEqualTo(100L);
        assertThat(rating.getProductName()).isEqualTo("Product A");
        assertThat(rating.getFirstName()).isEqualTo("John");
        assertThat(rating.getLastName()).isEqualTo("Doe");
    }

    @Test
    void testRatingSetters() {
        Rating rating = new Rating();
        rating.setId(2L);
        rating.setContent("Bad");
        rating.setRatingStar(1);
        rating.setProductId(200L);
        rating.setProductName("Product B");
        rating.setFirstName("Jane");
        rating.setLastName("Smith");

        assertThat(rating.getId()).isEqualTo(2L);
        assertThat(rating.getContent()).isEqualTo("Bad");
        assertThat(rating.getRatingStar()).isEqualTo(1);
        assertThat(rating.getProductId()).isEqualTo(200L);
        assertThat(rating.getProductName()).isEqualTo("Product B");
        assertThat(rating.getFirstName()).isEqualTo("Jane");
        assertThat(rating.getLastName()).isEqualTo("Smith");
    }

    @Test
    void testEqualsAndHashCode() {
        Rating rating1 = Rating.builder().id(1L).build();
        Rating rating2 = Rating.builder().id(1L).build();
        Rating rating3 = Rating.builder().id(2L).build();
        Rating ratingNullId = new Rating();

        // Equals
        assertThat(rating1).isEqualTo(rating1); // Same object
        assertThat(rating1).isEqualTo(rating2); // Same ID
        assertThat(rating1).isNotEqualTo(rating3); // Different ID
        assertThat(rating1).isNotEqualTo(null); // Null
        assertThat(rating1).isNotEqualTo("String"); // Different class
        assertThat(ratingNullId).isNotEqualTo(rating1); // One ID null

        // HashCode
        assertThat(rating1.hashCode()).isEqualTo(rating2.hashCode());
        assertThat(rating1.hashCode()).isEqualTo(Rating.class.hashCode());
    }

    @Test
    void testNoArgsConstructor() {
        Rating rating = new Rating();
        assertThat(rating).isNotNull();
    }

    @Test
    void testAllArgsConstructor() {
        Rating rating = new Rating(1L, "Content", 5, 10L, "Product", "Last", "First");
        assertThat(rating.getId()).isEqualTo(1L);
        assertThat(rating.getRatingStar()).isEqualTo(5);
    }
}

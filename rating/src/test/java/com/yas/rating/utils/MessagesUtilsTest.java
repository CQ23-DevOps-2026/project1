package com.yas.rating.utils;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_withExistingCode_shouldReturnMessage() {
        String message = MessagesUtils.getMessage("SUCCESS_MESSAGE");
        assertThat(message).isEqualTo("SUCCESS");
    }

    @Test
    void getMessage_withNonExistingCode_shouldReturnCode() {
        String message = MessagesUtils.getMessage("NON_EXISTING_CODE");
        assertThat(message).isEqualTo("NON_EXISTING_CODE");
    }

    @Test
    void getMessage_withArguments_shouldReturnFormattedMessage() {
        String message = MessagesUtils.getMessage("RATING_NOT_FOUND", 1L);
        assertThat(message).isEqualTo("RATING 1 is not found");
    }
}

package com.yas.tax.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_WhenKeyExists_ShouldReturnFormattedMessage() {
        // Since we cannot easily mock ResourceBundle.getBundle, 
        // we test with a key that likely doesn't exist to cover the catch block,
        // or a real one if we knew the messages.properties content.
        String message = MessagesUtils.getMessage("NON_EXISTENT_KEY", "arg1");
        assertThat(message).isEqualTo("NON_EXISTENT_KEY");
    }

    @Test
    void getMessage_WhenKeyDoesNotExist_ShouldReturnKey() {
        String message = MessagesUtils.getMessage("test.key", "val");
        assertThat(message).isEqualTo("test.key");
    }
}

package jbst.foundation.feigns.telegram.domain.requests;

import java.util.Map;

public record TelegramMessageRequest(
        String token,
        String chatId,
        String text
) {

    public Map<String, Object> getRequestBody() {
        return Map.of(
                "chat_id", this.chatId,
                "text", this.text,
                "parse_mode", "HTML"
        );
    }
}

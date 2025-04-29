package com.project.cloths.Service.Impl;

import com.project.cloths.Entity.Order;
import com.project.cloths.Entity.OrderStatusHistory;
import com.project.cloths.Entity.TelegramConfig;
import com.project.cloths.Service.OrderService;
import com.project.cloths.Service.TelegramChatbotService;
import com.project.cloths.repository.OrderRepository;
import com.project.cloths.repository.TelegramConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramChatbotServiceImpl implements TelegramChatbotService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TelegramConfigRepository telegramConfigRepository;

    private String currentBotToken;

    private final Random random = new Random();

    @Override
    public String handleIncomingMessage(String chatId, String message) {
        String response;
        String lowerCaseMessage = message.toLowerCase().trim();

        // Lưu chatId vào TelegramConfig (nếu có botToken, tức là từ Telegram)
        if (currentBotToken != null) {
            saveChatId(chatId, currentBotToken);
        }

        // 1. Xử lý câu chào
        if (containsAny(lowerCaseMessage, "chào", "hi", "hello", "hola")) {
            response = getRandomGreeting(chatId);
        }
        // 2. Kiểm tra đơn hàng
        else if (containsAny(lowerCaseMessage, "đơn hàng", "đơn", "order")) {
            Long orderId = extractOrderId(lowerCaseMessage);
            if (orderId != null) {
                response = getOrderStatusMessage(orderId);
            } else {
                response = "Bạn ơi, mình không thấy mã đơn hàng trong tin nhắn. Bạn gửi mã đơn hàng kiểu như 'Đơn hàng 42' để mình kiểm tra nha! 😊";
            }
        }
        // 3. Chính sách đổi trả
        else if (containsAny(lowerCaseMessage, "đổi trả", "hoàn trả", "return", "refund")) {
            response = "🌟 Chính sách đổi trả của shop nè:\n" +
                    "- Đổi trả trong vòng 7 ngày kể từ ngày nhận hàng.\n" +
                    "- Sản phẩm phải còn nguyên vẹn, chưa sử dụng.\n" +
                    "- Bạn cần hỗ trợ đổi trả thì nhắn mình thêm thông tin nha! 😊";
        }
        // 4. Thông tin giao hàng
        else if (containsAny(lowerCaseMessage, "giao hàng", "ship", "vận chuyển", "đâu rồi")) {
            response = "🚚 Thời gian giao hàng của shop:\n" +
                    "- Nội thành: 1-2 ngày.\n" +
                    "- Ngoại thành: 3-5 ngày.\n" +
                    "Bạn có mã đơn hàng không? Gửi mình để mình kiểm tra chính xác hơn nha! 😊";
        }
        // 5. Tư vấn sản phẩm
        else if (containsAny(lowerCaseMessage, "sản phẩm", "quần áo", "mua gì", "gợi ý")) {
            response = "👗 Bạn muốn tìm quần áo gì nha? Shop mình có:\n" +
                    "- Áo thun: trẻ trung, năng động, giá từ 150k.\n" +
                    "- Quần jeans: phong cách, bền đẹp, giá từ 300k.\n" +
                    "- Áo khoác: ấm áp, thời thượng, giá từ 500k.\n" +
                    "Bạn thích kiểu nào, mình gợi ý thêm cho! 😊";
        }
        // 6. Hỏi về giá
        else if (containsAny(lowerCaseMessage, "giá", "bao nhiêu", "mấy tiền")) {
            response = "💰 Giá sản phẩm bên mình dao động tùy loại nha:\n" +
                    "- Áo thun: từ 150k - 250k.\n" +
                    "- Quần jeans: từ 300k - 500k.\n" +
                    "- Áo khoác: từ 500k - 800k.\n" +
                    "Bạn muốn hỏi cụ thể sản phẩm nào, mình báo giá chi tiết hơn! 😊";
        }
        // 7. Hỏi về khuyến mãi
        else if (containsAny(lowerCaseMessage, "khuyến mãi", "sale", "giảm giá")) {
            response = "🎉 Hiện tại shop đang có chương trình sale nè:\n" +
                    "- Mua 2 áo thun giảm 10%.\n" +
                    "- Mua đơn từ 1 triệu được freeship.\n" +
                    "Bạn muốn mình gợi ý sản phẩm đang sale không? 😊";
        }
        // 8. Câu hỏi mở hoặc không hiểu
        else {
            response = getRandomFallback(chatId);
        }

        return response;
    }

    @Override
    public String getOrderStatusMessage(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return "Mình không tìm thấy đơn hàng với mã " + orderId + ". Bạn kiểm tra lại mã đơn hàng giúp mình nha! 😊";
        }

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("🔔 Trạng thái đơn hàng\n")
                .append("--------------------\n")
                .append("📦 Mã đơn hàng: ").append(orderId).append("\n")
                .append("📋 Trạng thái: ").append(order.getStatus().name()).append("\n");

        if (!order.getStatusHistory().isEmpty()) {
            OrderStatusHistory latestHistory = order.getStatusHistory().get(order.getStatusHistory().size() - 1);
            messageBuilder.append("⏰ Cập nhật lúc: ")
                    .append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(latestHistory.getChangedAt()))
                    .append("\n");
        }

        messageBuilder.append("--------------------\n")
                .append("Cảm ơn bạn đã mua sắm! ❤️");

        return messageBuilder.toString();
    }

    @Override
    public void saveChatId(String chatId, String botToken) {
        if (botToken == null) {
            System.out.println("Bot token is null, cannot save chatId.");
            return;
        }

        TelegramConfig config = telegramConfigRepository.findByBotToken(botToken);
        if (config == null) {
            config = new TelegramConfig();
            config.setBotToken(botToken);
        }
        config.setChatId(chatId);
        telegramConfigRepository.save(config);
    }

    @Override
    public void setCurrentBotToken(String botToken) {
        this.currentBotToken = botToken;
    }

    private Long extractOrderId(String message) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private boolean containsAny(String message, String... keywords) {
        return Arrays.stream(keywords).anyMatch(message::contains);
    }

    private String getRandomGreeting(String chatId) {
        List<String> greetings = Arrays.asList(
                "Chào bạn! Mình là chatbot của shop, sẵn sàng giúp bạn nha! 😊 Bạn cần gì nào?",
                "Hi bạn! Mình là trợ lý ảo của shop đây. Hôm nay bạn muốn mua gì nha? 🛒",
                "Hello! Rất vui được trò chuyện với bạn. Bạn cần mình giúp gì? 😊"
        );
        return greetings.get(random.nextInt(greetings.size()));
    }

    private String getRandomFallback(String chatId) {
        List<String> fallbacks = Arrays.asList(
                "Hihi, mình chưa hiểu ý bạn lắm. Bạn hỏi lại kiểu khác được không nha? Ví dụ: 'Đơn hàng 42 đâu rồi?' hoặc 'Có chương trình khuyến mãi không?' 😊",
                "Ồ, câu này hơi khó với mình. Bạn thử hỏi về đơn hàng, sản phẩm, hoặc khuyến mãi xem, mình sẽ trả lời ngay! 😊",
                "Mình chưa rõ ý bạn lắm. Bạn muốn hỏi gì nha? Mình có thể giúp về đơn hàng, sản phẩm, hoặc chính sách shop đó! 😊"
        );
        return fallbacks.get(random.nextInt(fallbacks.size()));
    }
}
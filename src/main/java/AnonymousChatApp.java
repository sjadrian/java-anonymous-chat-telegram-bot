import com.sjadrian.bot.JavaBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class AnonymousChatApp {
    public static void main(String[] args) {

        try {
            // Instantiate Telegram Bots API
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Register bot
            botsApi.registerBot(new JavaBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

package house.greysap;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class ResponseHandler {
    private final MessageSender sender;
    private final List<String> tasks;
    private final List<String> doneTasks;

    public ResponseHandler(MessageSender sender, DBContext db) {
        this.sender = sender;
        tasks = db.getList("TASKS");
        doneTasks = db.getList("DONE");
    }

    public void replyToDo(long chatId) {
        try {
            sender.execute(new SendMessage()
                    .setText("Here are your tasks. Select task that you have already done.")
                    .setChatId(chatId)
                    .setReplyMarkup(KeyboardFactory.withDynamicButtons(tasks)));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void replyToDoButtons(long chatId, String buttonId) {
        doneTasks.add(buttonId);
        tasks.remove(buttonId);

        try {
            sender.execute(new SendMessage()
                    .setText("Done! Send /todo to add new task. Send /did to look your past tasks.")
                    .setChatId(chatId));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
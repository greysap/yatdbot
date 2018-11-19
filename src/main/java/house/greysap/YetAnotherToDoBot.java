package house.greysap;

import org.apache.commons.lang3.StringUtils;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Flag.*;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.ADMIN;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

/**
 * Available commands:
 * 1. "/todo" adds new task to list "TASKS". After adding task with "!" list is being re-sorted, so the most important tasks are at the top of the list.
 * 2. "/tasks" shows elements of list "TASKS".
 * 3. "/done" evokes inline keyboard with task as button's label. After pushing the button chosen task is moved to list "DONE".
 * 4. "/did" shows elements in list "DONE"
 */
public class YetAnotherToDoBot extends AbilityBot {
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");
    private static final String BOT_USERNAME = System.getenv("BOT_USERNAME");
    private static final int CREATOR_ID = Integer.valueOf(System.getenv("CREATOR_ID"));

    private final ResponseHandler responseHandler;

    public YetAnotherToDoBot() {
        super(BOT_TOKEN, BOT_USERNAME);
        responseHandler = new ResponseHandler(sender, db);
    }

    @Override
    public int creatorId() {
        return CREATOR_ID;
    }

    public Ability addTask() {
        String todoMessage = "Send me task you want to do. Add some '!' according to importance of the task.";

        return Ability.builder()
                .name("todo")
                .info("Add new task.")
                .privacy(ADMIN)
                .locality(ALL)
                .input(0)
                .action(ctx -> silent.forceReply(todoMessage, ctx.chatId()))
                // The signature of a reply is -> (Consumer<Update> action, Predicate<Update>... conditions)
                // So, we  first declare the action that takes an update (NOT A MESSAGECONTEXT) like the action above
                // The reason of that is that a reply can be so versatile depending on the message, context becomes an inefficient wrapping
                .reply(upd -> {

                            List<String> tasks = db.getList("TASKS");
                            if (upd.getMessage().hasText()) {
                                String message = upd.getMessage().getText();
                                tasks.add(message);
                                if (message.contains("!")) {
                                    // Sort tasks by descending of "!" in a text.
                                    tasks.sort((s1, s2) -> StringUtils.countMatches(s2, "!") - StringUtils.countMatches(s1, "!"));
                                }
                                silent.send("Got it! Send /tasks to look your current tasks.",
                                        upd.getMessage().getChatId());
                            } else
                                silent.send("Interesting. Now please send me task with some text.",
                                    upd.getMessage().getChatId());

                        },
                        // Now we start declaring conditions, MESSAGE is a member of the enum Flag class
                        // That class contains out-of-the-box predicates for your replies!
                        // MESSAGE means that the update must have a message
                        // This is imported statically, Flag.MESSAGE
                        MESSAGE,
                        // REPLY means that the update must be a reply, Flag.REPLY
                        REPLY,
                        // A new predicate user-defined
                        // The reply must be to the bot
                        isReplyToBot(),
                        // If we process similar logic in other abilities, then we have to make this reply specific to this message
                        // The reply is to the todoMessage
                        isReplyToMessage(todoMessage)
                )
                // You can add more replies by calling .reply(...)
                .build();
    }

    public Ability viewCurrentTasks() {
        return Ability
                .builder()
                .name("tasks")
                .info("Print current tasks.")
                .locality(ALL)
                .privacy(ADMIN)
                .action(ctx -> {
                    List<String> tasks = db.getList("TASKS");

                    if (tasks.isEmpty()) {
                        silent.send("You have no tasks!", ctx.chatId());
                    } else {
                        silent.send("Here are your tasks! Send /done if you already have done something or /todo to add new task.", ctx.chatId());
                        silent.send(StringUtils.join(tasks, "\n"), ctx.chatId());
                    }

                })
                .build();
    }

    public Ability viewDoneTasks() {
        return Ability
                .builder()
                .name("did")
                .info("Print your past tasks.")
                .locality(ALL)
                .privacy(ADMIN)
                .action(ctx -> {
                    List<String> tasks = db.getList("DONE");

                    if (tasks.isEmpty()) {
                        silent.send("Did you do something? I dunno.", ctx.chatId());
                    } else {
                        silent.send("Here are your deeds! Send /done if you already have done something or /todo to add new task.", ctx.chatId());
                        silent.send(StringUtils.join(tasks, "\n"), ctx.chatId());
                    }

                })
                .build();
    }

    public Ability doTask() {
        return Ability
                .builder()
                .name("done")
                .info("Select task that you have already done.")
                .locality(ALL)
                .privacy(ADMIN)
                .action(ctx -> responseHandler.replyToDo(ctx.chatId()))
                .build();
    }

    public Ability replyToAnyMessage() {
        return Ability.builder()
                .name(DEFAULT) // DEFAULT ability is executed if user did not specify a command -> Bot needs to have access to messages (check FatherBot)
                .flag(TEXT)
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> silent.send("Hey, i'm just a to-do bot. I know only a few /commands.", ctx.chatId()))
                .build();
    }

    public Reply replyToDoButtons() {
        Consumer<Update> action = upd -> responseHandler.replyToDoButtons(getChatId(upd), upd.getCallbackQuery().getData());
        return Reply.of(action, CALLBACK_QUERY);
    }

    private Predicate<Update> isReplyToMessage(String message) {
        return upd -> {
            Message reply = upd.getMessage().getReplyToMessage();
            return reply.hasText() && reply.getText().equalsIgnoreCase(message);
        };
    }

    private Predicate<Update> isReplyToBot() {
        return upd -> upd.getMessage().getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(getBotUsername());
    }
}
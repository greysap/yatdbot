# yatdbot
Yet Another To-Do Bot is for managing list of tasks, which shared between some group of Telegram users.

Next commands are available to admins of the bot only:
1. /todo adds new task to list "TASKS". After adding task with "!" list is being re-sorted, so the most important tasks are at the top of the list.
2. /tasks shows elements of list "TASKS".
3. /done evokes inline keyboard with task as button's label. After pushing the button chosen task is moved to list "DONE".
4. /did shows elements in list "DONE"

Created with TelegramBots library and telegrambots-abilities abstraction. "Group management" is done via promote/demote commands from telegrambots-abilities. 

import java.lang.IllegalArgumentException

data class Message(
    val messageId: Int,
    val senderId: Int,
    val text: String,
    var isDeleted: Boolean = false,
    var isUnread: Boolean = true,
    var isSent: Boolean = true
)

data class Chat(
    val chatId: Int,
    val userId1: Int,
    val userId2: Int,
    var isDeleted: Boolean = false,
    val messages: MutableList<Message> = mutableListOf()
)

class ChatService {
    private val chats = mutableListOf<Chat>()

    fun addMessage(userId: Int, chatId: Int?, receiverId: Int, text: String) {
        var chat = findById(chatId)
        if (chat == null) {
            chat = createChat(userId, receiverId)
        }
        val chatMessages = chat.messages
        val message = Message(chatMessages.size, userId, text)
        chatMessages += message
    }

    fun deleteMessage(userId: Int, chatId: Int, messageId: Int) {
        val chat = findById(chatId) ?: return
        if (chat.userId1 != userId && chat.userId2 != userId) {
            throw IllegalArgumentException("This chat doesn't belong to this user!")
        }
        val messageToDelete = chat.messages.first { it.messageId == messageId && !it.isDeleted}
        messageToDelete.isDeleted = true
        if (chat.messages.none { !it.isDeleted }) {
            deleteChat(userId, chatId)
        }
    }

    fun deleteChat(userId: Int, chatId: Int) {
        val chat = findById(chatId) ?: return
        if (chat.userId1 != userId && chat.userId2 != userId) {
            throw IllegalArgumentException("This chat doesn't belong to this user!")
        }
        chat.isDeleted = true
    }

    fun getUnreadChatsCount(userId: Int): Int {
        return getChatsWithUnreadMessages(userId).size
    }

    fun getChats(userId: Int): List<Chat> {
        val chats = getChatsWithUnreadMessages(userId)
        if (chats.isEmpty()) {
            println("Нет сообщений")
        }
        return chats
    }

    fun getMessagesList(userId: Int, chatId: Int, firstUnreadMessageId: Int, unreadMessagesNumber: Int): List<Message> {
        val messagesList = findById(chatId)?.messages
            ?.filter { !it.isDeleted && it.senderId != userId && it.isUnread && it.messageId >= firstUnreadMessageId }
            ?.take(unreadMessagesNumber)
        messagesList?.forEach { it.isUnread = false }
        return messagesList ?: emptyList()
    }

    private fun getChatsWithUnreadMessages(userId: Int): List<Chat> {
        return chats.filter { (it.userId1 == userId || it.userId2 == userId) && !it.isDeleted }
            .filter { chat -> chat.messages.any { it.isUnread && it.senderId != userId && !it.isDeleted} }
    }

    private fun createChat(userId1: Int, userId2: Int): Chat {
        val chatId = chats.size
        val chat = Chat(chatId, userId1, userId2)
        chats += chat
        return chat
    }

    private fun findById(chatId: Int?): Chat? {
        return try {
            chats.first { it.chatId == chatId && !it.isDeleted }
        } catch (e: Exception) {
            null
        }
    }
}
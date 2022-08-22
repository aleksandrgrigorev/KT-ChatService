data class Message(
    val messageId: Int,
    val senderId: Int,
    val text: String,
    var isDeleted: Boolean = false,
    var isUnread: Boolean = true,
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

    fun addMessage(userId: Int, chatId: Int?, receiverId: Int, text: String): Message {
        var chat = findById(chatId)
        if (chat == null) {
            chat = createChat(userId, receiverId)
        }
        val chatMessages = chat.messages
        val message = Message(chatMessages.size, userId, text)
        chatMessages += message
        return message
    }

    fun deleteMessage(userId: Int, chatId: Int, messageId: Int): Boolean {
        val chat = findById(chatId) ?: return false
        if (chat.userId1 != userId && chat.userId2 != userId) {
            return false
        }
        try {
            val messageToDelete = chat.messages.first { it.messageId == messageId && !it.isDeleted}
            messageToDelete.isDeleted = true
        } catch (e: Exception) {
            return false
        }
        if (chat.messages.none { !it.isDeleted }) {
            deleteChat(userId, chatId)
        }
        return true
    }

    fun deleteChat(userId: Int, chatId: Int): Boolean {
        val chat = findById(chatId) ?: return false
        if (chat.userId1 != userId && chat.userId2 != userId) {
            return false
        }
        chat.isDeleted = true
        return true
    }

    fun getUnreadChatsCount(userId: Int): Int {
        return getChatsWithUnreadMessages(userId).size
    }

    fun getChats(userId: Int): List<Chat> {
        val chats = getChatsWithUnreadMessages(userId)
        if (chats.isEmpty()) {
            println("No messages")
        }
        return chats
    }

    fun getMessagesList(userId: Int, chatId: Int, firstUnreadMessageId: Int, unreadMessagesNumber: Int): List<Message> {
         return findById(chatId)?.messages?.asSequence()
            ?.filter { !it.isDeleted && it.senderId != userId && it.isUnread && it.messageId >= firstUnreadMessageId }
            ?.take(unreadMessagesNumber)
            ?.map {
                it.isUnread = false
                it
            }
            ?.toList()
            ?.ifEmpty { emptyList() } ?: emptyList()
    }

    fun findById(chatId: Int?): Chat? {
        return try {
            chats.first { it.chatId == chatId && !it.isDeleted }
        } catch (e: Exception) {
            null
        }
    }

    fun createChat(userId1: Int, userId2: Int): Chat {
        val chatId = chats.size
        val chat = Chat(chatId, userId1, userId2)
        chats += chat
        return chat
    }

    private fun getChatsWithUnreadMessages(userId: Int): List<Chat> {
        return chats.asSequence()
            .filter { (it.userId1 == userId || it.userId2 == userId) && !it.isDeleted }
            .filter { chat -> chat.messages.any { it.isUnread && it.senderId != userId && !it.isDeleted} }
            .toList()
    }
}
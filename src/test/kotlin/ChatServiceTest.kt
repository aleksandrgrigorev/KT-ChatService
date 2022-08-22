import org.junit.Test

import org.junit.Assert.*

class ChatServiceTest {

    @Test
    fun addMessage() {
        val chatService = ChatService()
        val messageId = 0
        val userId = 1
        val chatId = 0
        val receiverId = 2
        val text = "text"
        val expected = Message(messageId, userId, text, isDeleted=false, isUnread=true)

        val result = chatService.addMessage(userId, chatId, receiverId, text)

        assertEquals(expected, result)
    }

    @Test
    fun deleteMessage_success_notLastMessage() {
        val chatService = ChatService()
        val userId = 1
        val chatId = 0
        val receiverId = 2
        val messageId = 0
        chatService.addMessage(userId, chatId, receiverId, "text")
        chatService.addMessage(userId, chatId, receiverId, "text")

        val result = chatService.deleteMessage(userId, chatId, messageId)

        assertTrue(result)

        val chat = chatService.findById(chatId)
        assertNotNull(chat)
        assertFalse(chat!!.isDeleted)
    }

    @Test
    fun deleteMessage_success_lastMessage() {
        val chatService = ChatService()
        val userId = 1
        val chatId = 0
        val receiverId = 2
        val messageId = 0
        chatService.addMessage(userId, chatId, receiverId, "text")
        val newChat = chatService.findById(chatId)
        assertNotNull(newChat)

        val result = chatService.deleteMessage(userId, chatId, messageId)

        assertTrue(result)

        val chat = chatService.findById(chatId)
        assertNull(chat)
    }

    @Test
    fun deleteMessage_wrongUser() {
        val chatService = ChatService()
        val userId = 1
        val wrongUserId = 100500
        val chatId = 0
        val receiverId = 2
        val messageId = 0
        chatService.addMessage(userId, chatId, receiverId, "text")

        val result = chatService.deleteMessage(wrongUserId, chatId, messageId)

        assertFalse(result)
    }

    @Test
    fun deleteMessage_wrongMessageId() {
        val chatService = ChatService()
        val userId = 1
        val chatId = 0
        val receiverId = 2
        val wrongMessageId = 100
        chatService.addMessage(userId, chatId, receiverId, "text")

        val result = chatService.deleteMessage(userId, chatId, wrongMessageId)

        assertFalse(result)
    }

    @Test
    fun deleteChat_noChatToDelete() {
        val chatService = ChatService()
        val userId = 1
        val chatId = 0

        val result = chatService.deleteChat(userId, chatId)

        assertFalse(result)
    }

    @Test
    fun deleteChat_success() {
        val chatService = ChatService()
        val userId1 = 1
        val userId2 = 2
        val chat = chatService.createChat(userId1, userId2)

        val result = chatService.deleteChat(userId1, chat.chatId)

        assertTrue(result)
    }

    @Test
    fun deleteChat_wrongUser() {
        val chatService = ChatService()
        val userId1 = 1
        val userId2 = 2
        val userId3 = 3
        val chat = chatService.createChat(userId1, userId2)

        val result = chatService.deleteChat(userId3, chat.chatId)

        assertFalse(result)
    }

    @Test
    fun getUnreadChatsCount_emptyMessagesList() {
        val chatService = ChatService()
        val userId = 1

        val result = chatService.getUnreadChatsCount(userId)

        assertEquals(0, result)
    }

    @Test
    fun getUnreadChatsCount_success() {
        val chatService = ChatService()
        val userId = 1
        val otherUserId = 2
        val chatId = 0
        val otherChatId = 1
        val receiverId = 3
        val text = "text"
        chatService.addMessage(userId, chatId, receiverId, text)
        chatService.addMessage(otherUserId, otherChatId, receiverId, text)
        chatService.addMessage(userId, chatId, receiverId, text)

        val result = chatService.getUnreadChatsCount(receiverId)

        assertEquals(2, result)
    }

    @Test
    fun getChats_success() {
        val chatService = ChatService()
        val userId = 1
        val otherUserId = 2
        val chatId = 0
        val otherChatId = 1
        val receiverId = 3
        val text = "text"
        chatService.addMessage(userId, chatId, receiverId, text)
        chatService.addMessage(otherUserId, otherChatId, receiverId, text)
        chatService.addMessage(userId, chatId, receiverId, text)
        val expected = mutableListOf(chatService.findById(chatId), chatService.findById(otherChatId))

        val result = chatService.getChats(receiverId)

        assertEquals(expected, result)
    }

    @Test
    fun getChats_noMessages() {
        val chatService = ChatService()
        val receiverId = 3
        val expected = emptyList<Chat>()

        val result = chatService.getChats(receiverId)

        assertEquals(expected, result)
    }

    @Test
    fun getMessagesList_success() {
        val chatService = ChatService()
        val userId = 1
        val chatId = 0
        val receiverId = 3
        val text = "text"
        val firstMsg = 1
        val unreadMsgsCount = 2
        chatService.addMessage(userId, chatId, receiverId, text)
        val msg1 = chatService.addMessage(userId, chatId, receiverId, text)
        val msg2 = chatService.addMessage(userId, chatId, receiverId, text)
        val expected = mutableListOf(msg1, msg2)

        val result = chatService.getMessagesList(receiverId, chatId, firstMsg, unreadMsgsCount)

        assertEquals(expected, result)
    }

    @Test
    fun getMessagesList_emptyList() {
        val chatService = ChatService()
        val userId = 1
        val chatId = 0
        val receiverId = 3
        val text = "text"
        val firstMsg = 0
        val unreadMsgsCount = 1
        val msg1 = chatService.addMessage(userId, chatId, receiverId, text)
        chatService.deleteMessage(userId, chatId, msg1.messageId)
        val msg2 = chatService.addMessage(userId, chatId, receiverId, text)
        chatService.deleteMessage(userId, chatId, msg2.messageId)

        val expected = emptyList<Message>()

        val result = chatService.getMessagesList(receiverId, chatId, firstMsg, unreadMsgsCount)

        assertEquals(expected, result)
    }

    @Test
    fun findById_null() {
        val chatService = ChatService()
        val chatId = null

        val result = chatService.findById(chatId)

        assertNull(result)
    }

    @Test
    fun findById_success() {
        val chatService = ChatService()
        val userId1 = 1
        val userId2 = 2
        val chat = chatService.createChat(userId1, userId2)

        val result = chatService.findById(chat.chatId)

        assertEquals(chat, result)
    }

    @Test
    fun findById_otherChatId() {
        val chatService = ChatService()
        val userId1 = 1
        val userId2 = 2
        val otherChatId = 12413
        chatService.createChat(userId1, userId2)

        val result = chatService.findById(otherChatId)

        assertNull(result)
    }

    @Test
    fun findById_deletedChat() {
        val chatService = ChatService()
        val userId1 = 1
        val userId2 = 2
        val chat = chatService.createChat(userId1, userId2)
        chatService.deleteChat(userId1, chat.chatId)

        val result = chatService.findById(chat.chatId)

        assertNull(result)
    }

    @Test
    fun createChat_success() {
        val chatService = ChatService()
        val chatId = 0
        val userId1 = 1
        val userId2 = 2
        val expected = Chat(chatId, userId1, userId2)

        val result = chatService.createChat(userId1, userId2)

        assertEquals(expected, result)
    }
}
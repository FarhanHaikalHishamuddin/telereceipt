package com.telereceipt.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {

    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("message")
    private TelegramMessage message;

    @JsonProperty("callback_query")
    private TelegramCallbackQuery callbackQuery;

    public Long getUpdateId() { return updateId; }
    public void setUpdateId(Long updateId) { this.updateId = updateId; }

    public TelegramMessage getMessage() { return message; }
    public void setMessage(TelegramMessage message) { this.message = message; }

    public TelegramCallbackQuery getCallbackQuery() { return callbackQuery; }
    public void setCallbackQuery(TelegramCallbackQuery callbackQuery) { this.callbackQuery = callbackQuery; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramMessage {
        @JsonProperty("message_id")
        private Long messageId;

        @JsonProperty("from")
        private TelegramUser from;

        @JsonProperty("chat")
        private TelegramChat chat;

        @JsonProperty("text")
        private String text;

        @JsonProperty("caption")
        private String caption;

        @JsonProperty("photo")
        private List<TelegramPhotoSize> photo;

        @JsonProperty("date")
        private Long date;

        public Long getMessageId() { return messageId; }
        public void setMessageId(Long messageId) { this.messageId = messageId; }

        public TelegramUser getFrom() { return from; }
        public void setFrom(TelegramUser from) { this.from = from; }

        public TelegramChat getChat() { return chat; }
        public void setChat(TelegramChat chat) { this.chat = chat; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }

        public List<TelegramPhotoSize> getPhoto() { return photo; }
        public void setPhoto(List<TelegramPhotoSize> photo) { this.photo = photo; }

        public Long getDate() { return date; }
        public void setDate(Long date) { this.date = date; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramUser {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("username")
        private String username;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramChat {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("type")
        private String type;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramPhotoSize {
        @JsonProperty("file_id")
        private String fileId;

        @JsonProperty("file_unique_id")
        private String fileUniqueId;

        @JsonProperty("width")
        private Integer width;

        @JsonProperty("height")
        private Integer height;

        @JsonProperty("file_size")
        private Long fileSize;

        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }

        public String getFileUniqueId() { return fileUniqueId; }
        public void setFileUniqueId(String fileUniqueId) { this.fileUniqueId = fileUniqueId; }

        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }

        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramCallbackQuery {
        @JsonProperty("id")
        private String id;

        @JsonProperty("from")
        private TelegramUser from;

        @JsonProperty("message")
        private TelegramMessage message;

        @JsonProperty("data")
        private String data;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public TelegramUser getFrom() { return from; }
        public void setFrom(TelegramUser from) { this.from = from; }

        public TelegramMessage getMessage() { return message; }
        public void setMessage(TelegramMessage message) { this.message = message; }

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }
}

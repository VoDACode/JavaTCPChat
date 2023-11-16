package ua.nure.vovk.task3.core;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private final int code;
    private String from;
    private String to;
    private final String text;
    private boolean privateMessage = false;
    private Date date;

    public Message(String from, String to, String text, MessageCode code) {
        this(from, text, code);
        if(to == null) {
            throw new IllegalArgumentException("Receiver must not be null");
        }
        this.to = to;
    }

    public Message(String from, String text, MessageCode code) {
        if(from == null) {
            throw new IllegalArgumentException("From must not be null");
        }
        if(text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }
        this.from = from;
        this.text = text;
        this.code = code.getCode();
        this.date = new Date();
    }

    public void setPrivateMessage(boolean privateMessage) {
        this.privateMessage = privateMessage;
    }

    public boolean isPrivateMessage() {
        return privateMessage;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        if(to == null) {
            throw new IllegalArgumentException("Receiver must not be null");
        }
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        if(from == null) {
            throw new IllegalArgumentException("Sender must not be null");
        }
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public void setDateToNow() {
        this.date = new Date();
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", date.toString(), from, text);
    }
}

package fr.cawar.serveur;

public class Message {
    private String type;
    private Object data;

    public Message(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}


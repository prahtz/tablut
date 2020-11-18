package domain;

import java.io.Serializable;

public class ServerAction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    String from;
    String to;

    public ServerAction(String from, String to) {
        this.from = from;
        this.to = to;
    }
}

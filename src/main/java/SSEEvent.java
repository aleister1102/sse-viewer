public class SSEEvent {
    public String event;
    public String data;
    public String id;
    public Long retry;
    
    public SSEEvent() {
        this.event = null;
        this.data = null;
        this.id = null;
        this.retry = null;
    }
    
    @Override
    public String toString() {
        return "SSEEvent{" +
                "event='" + event + '\'' +
                ", data='" + data + '\'' +
                ", id='" + id + '\'' +
                ", retry=" + retry +
                '}';
    }
}

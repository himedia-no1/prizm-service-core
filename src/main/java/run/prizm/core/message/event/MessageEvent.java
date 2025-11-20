package run.prizm.core.message.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.prizm.core.message.entity.Message;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageEvent implements Serializable {
    
    private Long messageId;
    private Long channelId;
    private String eventType;
    private Message message;
    
    public MessageEvent(Message message, String eventType) {
        this.messageId = message.getId();
        this.channelId = message.getChannel().getId();
        this.eventType = eventType;
        this.message = message;
    }
}

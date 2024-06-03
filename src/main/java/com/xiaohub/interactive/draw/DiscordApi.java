package com.xiaohub.interactive.draw;

import com.xiaohub.config.DiscordConfig;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class DiscordApi {

    private final DiscordConfig config = new DiscordConfig();

    public void fetch(){
        // 从 Discord API 获取数据
        DiscordClient.create(config.getBotToken())
                .withGateway(client ->
                        client.on(MessageCreateEvent.class, event -> {
                            Message message = event.getMessage();

                            if (message.getContent().equalsIgnoreCase("!ping")) {
                                return message.getChannel()
                                        .flatMap(channel -> channel.createMessage("Pong!"));
                            }

                            return Mono.empty();
                        }))
                .block();
    }

}

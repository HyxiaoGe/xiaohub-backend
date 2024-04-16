package com.hyxiao.discord;

import com.hyxiao.config.LoadDiscordConfig;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class DiscordApi {

    private final LoadDiscordConfig config = new LoadDiscordConfig();

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

    public static void main(String[] args) {

        DiscordApi discordApi = new DiscordApi();
        discordApi.fetch();

    }

}

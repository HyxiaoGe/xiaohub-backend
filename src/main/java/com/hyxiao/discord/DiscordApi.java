package com.hyxiao.discord;

import com.hyxiao.config.LoadDiscordConfig;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

public class DiscordApi {

    private LoadDiscordConfig config = new LoadDiscordConfig();

    public void fetch() {
        DiscordClient.create(config.getUserToken()).withGateway(client -> client.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();

            if (message.getContent().equalsIgnoreCase("!ping")) {
                return message.getChannel().flatMap(channel -> channel.createMessage("Pong!"));
            }
            return message.getChannel().flatMap(channel -> channel.createMessage("I'm sorry, I don't understand."));
        })).block();
    }

    public static void main(String[] args) {
        new DiscordApi().fetch();
    }

}

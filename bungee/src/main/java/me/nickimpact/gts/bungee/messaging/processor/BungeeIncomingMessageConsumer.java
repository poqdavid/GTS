package me.nickimpact.gts.bungee.messaging.processor;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nickimpact.gts.api.messaging.IncomingMessageConsumer;
import me.nickimpact.gts.api.messaging.message.Message;
import me.nickimpact.gts.api.messaging.message.MessageConsumer;
import me.nickimpact.gts.api.messaging.message.OutgoingMessage;
import me.nickimpact.gts.api.messaging.message.type.MessageType;
import me.nickimpact.gts.api.messaging.message.type.UpdateMessage;
import me.nickimpact.gts.bungee.GTSBungeePlugin;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static me.nickimpact.gts.common.messaging.GTSMessagingService.NORMAL;

public class BungeeIncomingMessageConsumer implements IncomingMessageConsumer {

	private final GTSBungeePlugin plugin;
	private final Set<UUID> receivedMessages;

	private final Map<Class<?>, MessageConsumer<?>> consumers = Maps.newHashMap();

	public BungeeIncomingMessageConsumer(GTSBungeePlugin plugin) {
		this.plugin = plugin;
		this.receivedMessages = Collections.synchronizedSet(new HashSet<>());
	}

	@Override
	public <T extends MessageType.Response> void registerRequest(UUID request, Consumer<T> response) {}

	@Override
	public <T extends MessageType.Response> void processRequest(UUID request, T response) {}

	@Override
	public void cacheReceivedID(UUID id) {
		this.receivedMessages.add(id);
	}

	@Override
	public boolean consumeIncomingMessage(@NonNull Message message) {
		Objects.requireNonNull(message, "message");

		if (!this.receivedMessages.add(message.getID())) {
			return false;
		}

		this.processIncomingMessage(message);
		return true;
	}

	@Override
	public boolean consumeIncomingMessageAsString(@NonNull String encodedString) {
		Objects.requireNonNull(encodedString, "encodedString");
		JsonObject decodedObject = NORMAL.fromJson(encodedString, JsonObject.class).getAsJsonObject();

		// extract id
		JsonElement idElement = decodedObject.get("id");
		if (idElement == null) {
			throw new IllegalStateException("Incoming message has no id argument: " + encodedString);
		}
		UUID id = UUID.fromString(idElement.getAsString());

		// ensure the message hasn't been received already
		if (!this.receivedMessages.add(id)) {
			return false;
		}

		// extract type
		JsonElement typeElement = decodedObject.get("type");
		if (typeElement == null) {
			throw new IllegalStateException("Incoming message has no type argument: " + encodedString);
		}
		String type = typeElement.getAsString();

		// extract content
		@Nullable JsonElement content = decodedObject.get("content");

		// decode message
		Message decoded = GTSPlugin.getInstance().getMessagingService().getDecoder(type).apply(content, id);
		if(decoded == null) {
			return false;
		}

		// consume the message
		this.processIncomingMessage(decoded);
		return true;
	}

	@Override
	public <T extends Message, V extends T> void registerInternalConsumer(Class<T> parent, MessageConsumer<V> consumer) {
		this.consumers.put(parent, consumer);
	}

	@Override
	public MessageConsumer getInternalConsumer(Class<?> parent) {
		return this.consumers.get(parent);
	}

	@SuppressWarnings("unchecked")
	private void processIncomingMessage(Message message) {
		if (message instanceof UpdateMessage) {
			UpdateMessage msg = (UpdateMessage) message;
			this.plugin.getPluginLogger().info("[Messaging] Received message with id: " + msg.getID());
			this.getInternalConsumer(msg.getClass()).consume(message);
		} else {
			throw new IllegalArgumentException("Unknown message type: " + message.getClass().getName());
		}
	}
}

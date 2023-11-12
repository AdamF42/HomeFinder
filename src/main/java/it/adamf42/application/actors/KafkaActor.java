package it.adamf42.application.actors;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import it.adamf42.core.domain.ad.Ad;

public class KafkaActor extends AbstractBehavior<KafkaActor.Command>
{

	private final ActorRef<DatabaseActor.Command> databaseActor;
	private final KafkaClient kafkaClient;
	private Logger logger = getContext().getLog();

	// Define a scheduler for running asynchronous tasks

	private KafkaActor(ActorContext<Command> context, ActorRef<DatabaseActor.Command> databaseActor)
	{
		super(context);
		this.databaseActor = databaseActor;
		this.kafkaClient = new KafkaClient("localhost:29092", "ads", "ads-group", this::processKafkaMessage, logger); // TODO: configuration should be injected
	}

	public static Behavior<Command> create(ActorRef<DatabaseActor.Command> databaseActor)
	{
		return Behaviors.setup(context -> new KafkaActor(context, databaseActor));
	}

	// Define the command interface for KafkaActor
	public interface Command extends ManagerActor.Command
	{
	}

	// Define BootCommand within KafkaActor
	public static class BootCommand implements Command
	{
		private static final long serialVersionUID = 1L;
	}

	@Override
	public Receive<Command> createReceive()
	{
		return newReceiveBuilder().onMessage(KafkaActor.BootCommand.class, this::onBootCommand).build();
	}

	private Behavior<KafkaActor.Command> onBootCommand(KafkaActor.BootCommand bootCommand)
	{
		getContext().getLog().info("KafkaActor received BootCommand. Setting up Kafka consumer.");
		kafkaClient.start();
		return Behaviors.same();
	}

	private void processKafkaMessage(String partition, long offset, String key, Ad ad)
	{
		logger.info("Partition = {}, Offset = {}, Key = {}, Ad = {}", partition, offset, key, ad);
		DatabaseActor.SaveAdCommand adReceivedCommand = new DatabaseActor.SaveAdCommand(ad);
		databaseActor.tell(adReceivedCommand);
	}

	private static class KafkaClient extends Thread
	{
		private final KafkaConsumer<String, String> kafkaConsumer;
		private final String topic;
		private final MessageCallback messageCallback;
		private final Logger logger;

		public KafkaClient(String bootstrapServers, String topic, String groupId, MessageCallback messageCallback,
		Logger logger)
		{
			this.topic = topic;
			this.messageCallback = messageCallback;
			this.logger = logger;

			Properties properties = new Properties();
			properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
			properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.put("max.partition.fetch.bytes", "2097152");
			properties.put("max.poll.records", "500");

			this.kafkaConsumer = new KafkaConsumer<>(properties);
		}

		@Override
		public void run()
		{
			kafkaConsumer.subscribe(Collections.singletonList(topic));

			// Start the Kafka consumer loop
			while (true)
			{
				ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));

				for (ConsumerRecord<String, String> record : records)
				{
					messageCallback.onMessage(String.valueOf(record.partition()), record.offset(), record.key(),
					decodeAd(record.value()));
				}
			}
		}

		private Ad decodeAd(String json)
		{
			try
			{
				Gson gson = new Gson();
				return gson.fromJson(json, Ad.class);
			}
			catch (JsonSyntaxException e)
			{
				logger.error("Error decoding Ad from JSON: {}", e.getMessage());
				return null; // TODO avoid null
			}
		}

		public void close()
		{
			kafkaConsumer.close();
		}

		// Callback interface for processing Kafka messages
		public interface MessageCallback
		{
			void onMessage(String partition, long offset, String key, Ad ad);
		}
	}
}

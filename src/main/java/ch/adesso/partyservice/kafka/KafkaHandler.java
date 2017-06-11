package ch.adesso.partyservice.kafka;

import com.airhacks.porcupine.execution.boundary.Dedicated;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Created by aranda on 10.06.2017.
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class KafkaHandler {
    public static final String UNTIL_DATE = "UNTIL_DATE";
    public static final String DATE_FORMAT = "YYYYMMDDhhmm";

    @Inject
    KafkaConsumer<String, String> consumer;

    @Inject
    KafkaProducer<String, String> producer;

    @Dedicated
    @Inject
    ExecutorService kafka;

    @Inject
    Event<String> eventChannel;

    @PostConstruct
    public void onInit() {
        runAsync(this::handleKafkaEvent, kafka);
    }


    public void handleKafkaEvent() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(200);
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("Record value: " + record.value());
                switch (record.topic()) {
                    case KafkaProvider.TOPIC:
                        handleEvents(record);
                        break;
                    default:
                        handleEvents(record);
                        break;
                }
            }
        }
    }

    private void handleEvents(ConsumerRecord<String, String> record) {
        try {
            String eventText = record.value();
            System.out.println("eventText = " + eventText);
            eventChannel.fire(eventText);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

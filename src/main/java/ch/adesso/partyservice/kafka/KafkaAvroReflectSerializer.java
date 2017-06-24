package ch.adesso.partyservice.kafka;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.common.errors.SerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Extension of KafkaAvroSerializer that supports reflection-based serialization of values when
 * objects are wrapped in a ReflectContainer.
 */
public class KafkaAvroReflectSerializer extends KafkaAvroSerializer {

    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();
    private static final ReflectData REFLECT_DATA = ReflectData.get();

    @Override
    protected byte[] serializeImpl(String subject, Object object) throws SerializationException {
        if (object == null) {
            return null;
        } else {
            Schema schema = REFLECT_DATA.getSchema(object.getClass());
            try {
                int registeredSchemaId = this.schemaRegistry.register(subject, schema);

                System.out.println("Registered Schema: " + registeredSchemaId);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(0);
                out.write(ByteBuffer.allocate(4).putInt(registeredSchemaId).array());
                if (object instanceof byte[]) {
                    out.write((byte[]) ((byte[]) object));
                } else {
                    DatumWriter<Object> dw = new ReflectDatumWriter<>(schema);
                    Encoder encoder = ENCODER_FACTORY.directBinaryEncoder(out, null);
                    dw.write(object, encoder);
                    encoder.flush();
                }
                byte[] bytes = out.toByteArray();
                out.close();
                return bytes;
            } catch (RuntimeException | IOException e) {
                throw new SerializationException("Error serializing Avro message", e);
            } catch (RestClientException e) {
                throw new SerializationException("Error registering Avro schema: " + schema, e);
            }
        }
    }
}
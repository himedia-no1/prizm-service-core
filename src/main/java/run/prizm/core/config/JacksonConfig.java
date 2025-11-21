package run.prizm.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // Long → String (serialization)
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            
            // String → Long (deserialization)
            builder.deserializerByType(Long.class, new JsonDeserializer<Long>() {
                @Override
                public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String value = p.getValueAsString();
                    if (value == null || value.isEmpty()) {
                        return null;
                    }
                    try {
                        return Long.parseLong(value);
                    } catch (NumberFormatException e) {
                        // If already a number, get it directly
                        return p.getLongValue();
                    }
                }
            });
            
            builder.deserializerByType(long.class, new JsonDeserializer<Long>() {
                @Override
                public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String value = p.getValueAsString();
                    if (value == null || value.isEmpty()) {
                        return 0L;
                    }
                    try {
                        return Long.parseLong(value);
                    } catch (NumberFormatException e) {
                        return p.getLongValue();
                    }
                }
            });
        };
    }
}

package run.prizm.core.common.id;

import com.github.f4b6a3.uuid.UuidCreator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.UUID;

public class UuidV7LongGenerator implements IdentifierGenerator {

    public static long nextValue() {
        UUID uuid = UuidCreator.getTimeOrdered();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        buffer.flip();
        long id = buffer.getLong();
        return id < 0 ? -id : id;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) {
        return nextValue();
    }
}

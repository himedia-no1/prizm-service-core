package run.prizm.core.common.id;

import com.github.f4b6a3.uuid.UuidCreator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.UUID;

public class UuidV7LongGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) {
        UUID uuid = UuidCreator.getTimeOrdered(); // UUIDv7 생성
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        buffer.flip();
        long id = buffer.getLong(); // 상위 64비트만 사용
        return id < 0 ? -id : id; // 음수 방지 (양수만)
    }
}

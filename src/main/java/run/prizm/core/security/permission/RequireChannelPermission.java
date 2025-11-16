package run.prizm.core.security.permission;

import run.prizm.core.space.channel.permission.ChannelPermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireChannelPermission {
    ChannelPermission value();
}

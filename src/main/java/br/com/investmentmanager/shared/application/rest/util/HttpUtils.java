package br.com.investmentmanager.shared.application.rest.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@UtilityClass
public final class HttpUtils {
    public static URI createEntityLocation(@NonNull UUID entityId) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(entityId).toUri();
    }
}

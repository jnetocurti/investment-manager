package br.com.investmentmanager.shared.application.rest.config;

import br.com.investmentmanager.shared.application.rest.ApiErrorResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Investment Manager API definition",
                description = "Investment operations management"
        )
)
public class OpenApiConfig {

    public static final Schema DEFAULT_ERROR_SCHEMA = createSchema(ApiErrorResponse.class)
            .addProperty("title", new StringSchema().example("Unable to process request"))
            .addProperty("message", new StringSchema().example("Sorry something went wrong. Please try again later"))
            .addProperty("details", null);

    public static final Schema DEFAULT_BADREQUEST_ERROR_SCHEMA = createSchema(
            "BadRequestErrorResponse", ApiErrorResponse.class)
            .addProperty("title", new StringSchema().example("Unable to process request"))
            .addProperty("message", new StringSchema().example("Sorry something went wrong. Check your request"))
            .addProperty("details", new ArraySchema().items(new MapSchema()
                            .addProperty("field", new StringSchema().example("description"))
                            .addProperty("message", new StringSchema().example("must not be blank"))
                    )
            );

    public static final Schema DEFAULT_CONFLICT_ERROR_SCHEMA = createSchema(
            "ConflictErrorResponse", ApiErrorResponse.class)
            .addProperty("title", new StringSchema().example("Unable to process request"))
            .addProperty("message", new StringSchema().example("The entity already exists"))
            .addProperty("details", new MapSchema()
                    .addProperty("location", new StringSchema()
                            .example("http://investment.manager/v1/entity/02cdb75f-8377-4411-ada0-6b88ab7316dc")
                    )
            );

    public static Schema createSchema(Class type) {
        return createSchema(type.getSimpleName(), type);
    }

    public static Schema createSchema(String name, Class type) {
        return ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(type).resolveAsRef(false)).schema.name(name);
    }

    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();

            var schemas = new LinkedHashMap<String, Schema>();
            schemas.put(DEFAULT_ERROR_SCHEMA.getName(), DEFAULT_ERROR_SCHEMA);
            schemas.put(DEFAULT_BADREQUEST_ERROR_SCHEMA.getName(), DEFAULT_BADREQUEST_ERROR_SCHEMA);
            schemas.put(DEFAULT_CONFLICT_ERROR_SCHEMA.getName(), DEFAULT_CONFLICT_ERROR_SCHEMA);

            schemas.putAll(components.getSchemas());
            components.setSchemas(schemas);
        };
    }
}

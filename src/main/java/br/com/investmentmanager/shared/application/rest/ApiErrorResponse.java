package br.com.investmentmanager.shared.application.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    @Builder.Default
    private final String title = "Unable to process request";
    @Builder.Default
    private final String message = "Sorry something went wrong. Please try again later";
    @Builder.Default
    private final Object details = null;
}

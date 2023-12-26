package br.com.investmentmanager.tradingnote.application.rest;

import br.com.investmentmanager.tradingnote.application.output.TradingNote;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(
        name = "Trading Notes",
        description = "Operations with trading notes"
)
@RequestMapping("v1/trading-notes")
public interface TradingNoteController {
    @Operation(
            summary = "Create a trading note",
            description = "Upload file to create a trading note"
    )
    @ApiResponse(
            responseCode = "201",
            headers = @Header(
                    name = HttpHeaders.LOCATION,
                    schema = @Schema(
                            example = "http://api.host/v1/trading-notes/7ea1969b-a7b4-4b96-a669-eeff1e5a533b"
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            content = @Content(
                    schema = @Schema(
                            ref = "BadRequestErrorResponse"
                    ),
                    examples = @ExampleObject(value = """
                                {
                                    "title": "Unable to process request",
                                    "message": "Sorry something went wrong. Check the file sent in the request",
                                    "details": [
                                        {
                                            "field": "file",
                                            "message": "Invalid trading note file"
                                        }
                                    ]
                                }
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "409",
            content = @Content(
                    schema = @Schema(
                            ref = "ConflictErrorResponse"
                    ),
                    examples = @ExampleObject(value = """
                                {
                                    "title": "Unable to process request",
                                    "message": "The trading note already exists",
                                    "details": {
                                        "location": "http://api.host/v1/trading-notes/7ea1969b-a7b4-4b96-a669-eeff1e5a533b"
                                    }
                                }
                            """
                    )
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TradingNote> create(@RequestPart("file") MultipartFile file) throws IOException;
}

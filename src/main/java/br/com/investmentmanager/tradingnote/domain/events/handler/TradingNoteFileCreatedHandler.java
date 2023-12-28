package br.com.investmentmanager.tradingnote.domain.events.handler;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNoteFile;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteFileCreated;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TradingNoteFileCreatedHandler {

    @SneakyThrows
    @EventListener
    public void onMyDomainEvent(TradingNoteFileCreated event) {

        TradingNoteFile source = event.getSource();

        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint("http://127.0.0.1:9000")
                        .credentials("masoud", "Strong#Pass#2022")
                        .build();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("trading-notes")
                        .object(source.getName())
                        .contentType(source.getContentType())
                        .stream(source.getContent(), source.getContent().available(), -1)
                        .build()
        );

        System.out.println(event);
    }

}

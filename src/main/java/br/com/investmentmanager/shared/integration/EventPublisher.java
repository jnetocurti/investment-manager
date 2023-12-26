package br.com.investmentmanager.shared.integration;

public interface EventPublisher<T> {

    void publish(T event);
}

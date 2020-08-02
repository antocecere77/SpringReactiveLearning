package com.antocecere77.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static reactor.core.scheduler.Schedulers.parallel;

public class FluxAndMonoTransformTest {

    List<String> names = Arrays.asList("adam", "anna", "jack","jenny");

    @Test
    public void transformUsingMap() {
        Flux<String> namesFlux = Flux.fromIterable(names)
                .map(s -> s.toUpperCase()) //ADAM, ANNA, JACK, JENNY
                .log();

        StepVerifier.create(namesFlux)
                .expectNext("ADAM", "ANNA", "JACK", "JENNY")
                .verifyComplete();

        Flux<Integer> namesFlux2 = Flux.fromIterable(names)
                .map(s -> s.length()) //ADAM, ANNA, JACK, JENNY
                .log();

        StepVerifier.create(namesFlux2)
                .expectNext(4,4,4,5)
                .verifyComplete();
    }

    @Test
    public void transformUsingMap_Length_repeat() {

        Flux<Integer> namesFlux2 = Flux.fromIterable(names)
                .map(s -> s.length()) //ADAM, ANNA, JACK, JENNY
                .repeat(1)
                .log();

        StepVerifier.create(namesFlux2)
                .expectNext(4,4,4,5, 4,4,4,5)
                .verifyComplete();
    }

    @Test
    public void transformUsingMap_Filter() {

        Flux<String> namesFlux2 = Flux.fromIterable(names)
                .filter(s -> s.length() > 4)
                .map(s -> s.toUpperCase()) //JENNY
                .log();

        StepVerifier.create(namesFlux2)
                .expectNext("JENNY")
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMap() {
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F")) // A, B, C, D, E, F
                .flatMap(s -> {
                    return Flux.fromIterable(convertToList(s)); // A -> List(A, newValue), B -> List(B, newValue)
                })
                .log(); // db or external service call that returns a flux -> s -> Flux<String>

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMap_usingparallel() {
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F")) // Flux<String>
                .window(2) // Flux<Flux<String>> -> (A,B), (C,D), (E,F)
                .flatMap((s) ->
                        s.map(this::convertToList).subscribeOn(parallel())) // Flux<String>
                        .flatMap(s -> Flux.fromIterable(s)) //Flux<String>
                .log(); // db or external service call that returns a flux -> s -> Flux<String>

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMap_parallel_maintain_order() {
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F")) // Flux<String>
                .window(2) // Flux<Flux<String>> -> (A,B), (C,D), (E,F)
                .concatMap((s) ->
                        s.map(this::convertToList).subscribeOn(parallel())) // Flux<String>
                .flatMap(s -> Flux.fromIterable(s)) //Flux<String>
                .log(); // db or external service call that returns a flux -> s -> Flux<String>

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMap_parallel_flatmap_sequantial() {
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F")) // Flux<String>
                .window(2) // Flux<Flux<String>> -> (A,B), (C,D), (E,F)
                .flatMapSequential((s) ->
                        s.map(this::convertToList).subscribeOn(parallel())) // Flux<String>
                .flatMap(s -> Flux.fromIterable(s)) //Flux<String>
                .log(); // db or external service call that returns a flux -> s -> Flux<String>

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    private List<String> convertToList(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList(s, "newValue");
    }
}

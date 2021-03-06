package com.antocecere77.learnreactivespring.handler;

import com.antocecere77.learnreactivespring.constants.ItemsConstants;
import com.antocecere77.learnreactivespring.document.Item;
import com.antocecere77.learnreactivespring.repository.ItemReactiveRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ItemHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    List<Item> itemList = Arrays.asList(new Item(null, "Samsung TV", 400.0),
            new Item(null, "LG TV", 420.0),
            new Item(null, "Apple Watch", 299.99),
            new Item("ABC", "Beats Headphones", 149.99));

    @Before
    public void setUp() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(itemList))
                .flatMap(itemReactiveRepository::save)
                .doOnNext(item -> System.out.println("Inserted item is " + item))
                .blockLast(); //Block until save is finished, if not inserted test1 start before data is inserted
    }

    @Test
    public void getAllItems() {
        webTestClient.get()
                .uri(ItemsConstants.ITEM_FUNCTIONAL_END_POINT_V1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Item.class)
                .hasSize(4);
    }

    @Test
    public void getAllItems_approach2() {
        webTestClient.get()
                .uri(ItemsConstants.ITEM_FUNCTIONAL_END_POINT_V1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Item.class)
                .hasSize(4)
        .consumeWith(response -> {
            List<Item> items = response.getResponseBody();
            items.forEach(item -> assertTrue(item.getId()!=null));
        });
    }

    @Test
    public void getAllItems_approach3() {
        Flux<Item> itemFlux = webTestClient.get()
                .uri(ItemsConstants.ITEM_FUNCTIONAL_END_POINT_V1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .returnResult(Item.class)
                .getResponseBody();

        StepVerifier.create(itemFlux.log())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void getOneItem() {
        webTestClient.get()
                .uri(ItemsConstants.ITEM_FUNCTIONAL_END_POINT_V1.concat("/{id}"), "ABC")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", 149.99);
    }

    @Test
    public void getOneItem_notFound() {
        webTestClient.get()
                .uri(ItemsConstants.ITEM_FUNCTIONAL_END_POINT_V1.concat("/{id}"), "DEF")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testCreateItem() {

        Item item = new Item(null,"Iphone X", 999.99);
        webTestClient.post().uri(ItemsConstants.ITEM_FUNCTIONAL_END_POINT_V1)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo("Iphone X")
                .jsonPath("$.price").isEqualTo("999.99");
    }

    @Test
    public void testDeleteOneItem() {

        webTestClient.delete().uri(ItemsConstants.ITEM_FUNCTIONAL_END_POINT_V1.concat("/{id}"), "ABC")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
    }

    @Test
    public void testUpdateItem() {
        double newPrice = 129.99;
        Item item = new Item(null,"Beats HeadPhones", newPrice);
        webTestClient.put().uri(ItemsConstants.ITEM_FUNCTIONAL_END_POINT_V1.concat("/{id}"), "ABC")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price",newPrice);
    }

    @Test
    public void testUpdateItem_notFound() {
        double newPrice=129.99;
        Item item = new Item(null,"Beats HeadPhones", newPrice);
        webTestClient.put().uri(ItemsConstants.ITEM_FUNCTIONAL_END_POINT_V1.concat("/{id}"), "DEF") //no record with this ids
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void runTimeException() {
        webTestClient.get()
                .uri("/fun/runtimeException")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message", "RuntimException occurred");
    }
}

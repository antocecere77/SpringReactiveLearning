package com.antocecere77.learnreactivespring.repository;

import com.antocecere77.learnreactivespring.document.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
public class ItemReactiveRepositoryTest {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    List<Item> itemList = Arrays.asList(new Item(null, "Samsung TV", 400.0),
            new Item(null, "LG TV", 420.0),
            new Item(null, "Apple Watch", 299.99),
            new Item(null, "Beats Headphones", 149.99),
            new Item("ABC", "Bose Headphones", 149.99));

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
        StepVerifier.create(itemReactiveRepository.findAll()) //0
            .expectSubscription()
            .expectNextCount(5)
            .verifyComplete();
    }

    @Test
    public void getItemById() {
        StepVerifier.create(itemReactiveRepository.findById("ABC"))
            .expectSubscription()
            .expectNextMatches(item -> item.getDescription().equals("Bose Headphones"))
            .verifyComplete();
    }

    @Test
    public void findItemByDescription() {
        StepVerifier.create(itemReactiveRepository.findByDescription("Bose Headphones")
                .log())
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void saveItem() {
        Item item = new Item(null, "Google Home Mini", 30.00);
        Mono<Item> saveItem = itemReactiveRepository.save(item);
        StepVerifier.create(saveItem
                .log())
            .expectSubscription()
            .expectNextMatches(item1 -> item1.getId()!=null && item1.getDescription().equals("Google Home Mini"))
            .verifyComplete();
    }

    @Test
    public void updateItem() {
        double newPrice = 520.00;
        Flux<Item> updateItem = itemReactiveRepository.findByDescription("LG TV")
            .map(item -> {
                item.setPrice(newPrice); // Setting the new price
                return item;
            })
            .flatMap(item -> {
                return itemReactiveRepository.save(item); // Save the item with the new price
            });

        StepVerifier.create(updateItem.log())
                .expectSubscription()
                .expectNextMatches(item -> item.getPrice()==520.00)
                .verifyComplete();
    }
}

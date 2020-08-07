package com.antocecere77.learnreactivespring.controller.v1;

import com.antocecere77.learnreactivespring.document.ItemCapped;
import com.antocecere77.learnreactivespring.repository.ItemReactiveCappedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static com.antocecere77.learnreactivespring.constants.ItemsConstants.ITEM_STREAM_ENDPOINT_V1;

@RestController
public class ItemStreamController {

    @Autowired
    ItemReactiveCappedRepository itemReactiveCappedRepository;

    @GetMapping(value = ITEM_STREAM_ENDPOINT_V1, produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<ItemCapped> getItemsStream() {
        return itemReactiveCappedRepository.findItemsBy();
    }

}

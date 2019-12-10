/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.projectriff.cartprocessor;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import static org.junit.Assert.assertEquals;

public class NoBuyDetectorTests {

    @Test
    public void test() {
        CartEvent cartEvent1 = new CartEvent();
        cartEvent1.setUser("homer");
        cartEvent1.setProduct("widget");
        cartEvent1.setQuantity(3);

        CartEvent cartEvent2 = new CartEvent();
        cartEvent2.setUser("homer");
        cartEvent2.setProduct("gadget");
        cartEvent2.setQuantity(7);

        CartEvent cartEvent3 = new CartEvent();
        cartEvent3.setUser("homer");
        cartEvent3.setProduct("widget");
        cartEvent3.setQuantity(0);

        CheckoutEvent checkoutEvent = new CheckoutEvent();
        checkoutEvent.setUser("homer");
    
        Flux<CartEvent> cartEvents = Flux.just(cartEvent1, cartEvent2, cartEvent3);
        Flux<CheckoutEvent> checkoutEvents = Flux.just(checkoutEvent);

        NoBuyDetector processor = new NoBuyDetector();

        Flux<NoBuyEvent> orders = processor.apply(Tuples.of(cartEvents, checkoutEvents));
        List<NoBuyEvent> results = orders.collectList().block();
        assertEquals(1, results.size());
        NoBuyEvent event = results.get(0);
        assertEquals("homer", event.getUser());
        assertEquals(cartEvent1.getProduct(), event.getProduct());
    }
}
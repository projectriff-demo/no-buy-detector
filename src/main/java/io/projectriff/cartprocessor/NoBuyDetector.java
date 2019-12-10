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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * Function that processes cart events and checkout events.
 */
public class NoBuyDetector implements Function<Tuple2<Flux<CartEvent>, Flux<CheckoutEvent>>, Flux<NoBuyEvent>> {

	private final Logger logger = LoggerFactory.getLogger(NoBuyDetector.class);

	private Map<String, Map<String, Integer>> carts = new HashMap<>();

	@Override
	public Flux<NoBuyEvent> apply(Tuple2<Flux<CartEvent>, Flux<CheckoutEvent>> inputs) {
		Flux<CartEvent> cartEvents = inputs.getT1();
		Flux<CheckoutEvent> checkoutEvents = inputs.getT2();
		cartEvents.subscribe(e -> updateCart(e));
//		return checkoutEvents.map(e -> checkout(e));
		return checkoutEvents.flatMap(e -> checkout(e));
	}

	private void updateCart(CartEvent e) {
		carts.putIfAbsent(e.getUser(), new HashMap<String, Integer>());
		carts.get(e.getUser()).put(e.getProduct(), e.getQuantity());
		logger.info("updated cart for " + e.getUser() + ": " + e.getProduct() + "=" + e.getQuantity());
	}

	private Flux<NoBuyEvent> checkout(CheckoutEvent e) {
		logger.info("received checkout event for " + e.getUser());
		List<NoBuyEvent> events = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : carts.get(e.getUser()).entrySet()) {
			if (entry.getValue() == 0) {
				logger.info("detected no-buy event for " + e.getUser() + ": " + entry.getKey());
				NoBuyEvent noBuy = new NoBuyEvent();
				noBuy.setUser(e.getUser());
				noBuy.setProduct(entry.getKey());
				events.add(noBuy);
			}
		}
		carts.put(e.getUser(), new HashMap<String, Integer>());
		return Flux.fromIterable(events);
	}
}

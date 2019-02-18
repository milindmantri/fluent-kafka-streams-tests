/*
 * The MIT License
 *
 * Copyright (c) 2019 bakdata GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bakdata.fluent_kafka_streams_tests;

import com.bakdata.fluent_kafka_streams_tests.test_applications.CountInhabitantsWithAvro;
import com.bakdata.fluent_kafka_streams_tests.test_types.City;
import com.bakdata.fluent_kafka_streams_tests.test_types.Person;
import org.apache.kafka.common.serialization.Serdes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


class CountInhabitantsWithAvroTest {
    private static final String CITY1 = "City1";
    private static final String CITY2 = "City2";

    private static final String ID1 = "Person1";
    private static final String ID2 = "Person2";
    private static final String ID3 = "Person3";

    private final CountInhabitantsWithAvro app = new CountInhabitantsWithAvro();

    @RegisterExtension
    final
    TestTopology<String, Person> testTopology = new TestTopology<>(this.app::getTopology, this.app.getKafkaProperties());

    @Test
    void shouldAggregateInhabitants() {
        this.testTopology.input()
                .add(new Person(ID1, "Huey", CITY1))
                .add(new Person(ID2, "Dewey", CITY2))
                .add(new Person(ID3, "Louie", CITY1));

        this.testTopology.tableOutput().withValueType(City.class)
                .expectNextRecord().hasKey(CITY1).hasValue(new City(CITY1, 2))
                .expectNextRecord().hasKey(CITY2).hasValue(new City(CITY2, 1))
                .expectNoMoreRecord();
    }

    @Test
    void shouldWorkForEmptyInput() {
        this.testTopology.tableOutput().withSerde(Serdes.String(), Serdes.Long())
                .expectNoMoreRecord();
    }
}
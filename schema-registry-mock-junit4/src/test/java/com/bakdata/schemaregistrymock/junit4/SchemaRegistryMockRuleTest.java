/*
 * MIT License
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
package com.bakdata.schemaregistrymock.junit4;

import static org.assertj.core.api.Assertions.assertThat;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.apache.avro.Schema;
import org.junit.Rule;
import org.junit.Test;

public class SchemaRegistryMockRuleTest {
    @Rule
    public final SchemaRegistryMockRule schemaRegistry = new SchemaRegistryMockRule();

    private static Schema createSchema(final String name) {
        return Schema.createRecord(name, "no doc", "", false, Collections.emptyList());
    }

    @Test
    public void shouldRegisterKeySchema() throws IOException, RestClientException {
        final Schema keySchema = createSchema("key_schema");
        final int id = this.schemaRegistry.registerKeySchema("test-topic", keySchema);

        final AvroSchema retrievedSchema = (AvroSchema) this.schemaRegistry.getSchemaRegistryClient().getSchemaById(id);
        assertThat(retrievedSchema.rawSchema()).isEqualTo(keySchema);
    }

    @Test
    public void shouldRegisterValueSchema() throws IOException, RestClientException {
        final Schema valueSchema = createSchema("value_schema");
        final int id = this.schemaRegistry.registerValueSchema("test-topic", valueSchema);

        final AvroSchema retrievedSchema = (AvroSchema) this.schemaRegistry.getSchemaRegistryClient().getSchemaById(id);
        assertThat(retrievedSchema.rawSchema()).isEqualTo(valueSchema);
    }

    @Test
    public void shouldRegisterKeySchemaWithClient() throws IOException, RestClientException {
        final Schema keySchema = createSchema("key_schema");
        final int id =
                this.schemaRegistry.getSchemaRegistryClient().register("test-topic-key", new AvroSchema(keySchema));

        final AvroSchema retrievedSchema = (AvroSchema) this.schemaRegistry.getSchemaRegistryClient().getSchemaById(id);
        assertThat(retrievedSchema.rawSchema()).isEqualTo(keySchema);
    }

    @Test
    public void shouldRegisterValueSchemaWithClient() throws IOException, RestClientException {
        final Schema valueSchema = createSchema("value_schema");
        final int id =
                this.schemaRegistry.getSchemaRegistryClient().register("test-topic-value", new AvroSchema(valueSchema));

        final AvroSchema retrievedSchema = (AvroSchema) this.schemaRegistry.getSchemaRegistryClient().getSchemaById(id);
        assertThat(retrievedSchema.rawSchema()).isEqualTo(valueSchema);
    }

    @Test
    public void shouldHaveSchemaVersions() throws IOException, RestClientException {
        final Schema valueSchema = createSchema("value_schema");
        final String topic = "test-topic";
        final int id = this.schemaRegistry.registerValueSchema(topic, valueSchema);

        final List<Integer> versions = this.schemaRegistry.getSchemaRegistryClient().getAllVersions(topic + "-value");
        assertThat(versions.size()).isOne();

        final SchemaMetadata metadata =
                this.schemaRegistry.getSchemaRegistryClient().getSchemaMetadata(topic + "-value", versions.get(0));
        assertThat(metadata.getId()).isEqualTo(id);
        final String schemaString = metadata.getSchema();
        final Schema retrievedSchema = new Schema.Parser().parse(schemaString);
        assertThat(retrievedSchema).isEqualTo(valueSchema);
    }

    @Test
    public void shouldHaveLatestSchemaVersion() throws IOException, RestClientException {
        final Schema valueSchema1 = createSchema("value_schema");
        final String topic = "test-topic";
        final int id1 = this.schemaRegistry.registerValueSchema(topic, valueSchema1);

        final List<Schema.Field> fields = Collections.singletonList(
                new Schema.Field("f1", Schema.create(Schema.Type.STRING), "", null));
        final Schema valueSchema2 = Schema.createRecord("value_schema", "no doc", "", false, fields);
        final int id2 = this.schemaRegistry.registerValueSchema(topic, valueSchema2);

        final List<Integer> versions = this.schemaRegistry.getSchemaRegistryClient().getAllVersions(topic + "-value");
        assertThat(versions.size()).isEqualTo(2);

        final SchemaMetadata metadata =
                this.schemaRegistry.getSchemaRegistryClient().getLatestSchemaMetadata(topic + "-value");
        final int metadataId = metadata.getId();
        assertThat(metadataId).isNotEqualTo(id1);
        assertThat(metadataId).isEqualTo(id2);
        final String schemaString = metadata.getSchema();
        final Schema retrievedSchema = new Schema.Parser().parse(schemaString);
        assertThat(retrievedSchema).isEqualTo(valueSchema2);
    }
}

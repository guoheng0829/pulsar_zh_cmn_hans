/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.functions.instance.producers;

import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.functions.instance.FunctionResultRouter;

public abstract class AbstractOneOuputTopicProducers implements Producers {

    protected final PulsarClient client;
    protected final String outputTopic;

    AbstractOneOuputTopicProducers(PulsarClient client,
                                   String outputTopic)
            throws PulsarClientException {
        this.client = client;
        this.outputTopic = outputTopic;
    }

    static ProducerBuilder<byte[]> newProducerBuilder(PulsarClient client) {
        // use function result router to deal with different processing guarantees.
        return client.newProducer() //
                .blockIfQueueFull(true) //
                .enableBatching(true) //
                .batchingMaxPublishDelay(1, TimeUnit.MILLISECONDS) //
                .compressionType(CompressionType.LZ4) //
                .hashingScheme(HashingScheme.Murmur3_32Hash) //
                .messageRoutingMode(MessageRoutingMode.CustomPartition) //
                .messageRouter(FunctionResultRouter.of());
    }

    protected Producer<byte[]> createProducer(String topic)
            throws PulsarClientException {
        return createProducer(client, topic);
    }

    public static Producer<byte[]> createProducer(PulsarClient client, String topic)
            throws PulsarClientException {
        return newProducerBuilder(client).topic(topic).create();
    }

    protected Producer<byte[]> createProducer(String topic, String producerName)
            throws PulsarClientException {
        return createProducer(client, topic, producerName);
    }

    public static Producer<byte[]> createProducer(PulsarClient client, String topic, String producerName)
            throws PulsarClientException {
        return newProducerBuilder(client).topic(topic).producerName(producerName).create();
    }
}

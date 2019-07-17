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
package org.apache.pulsar.broker;

import java.io.IOException;

import org.apache.bookkeeper.client.BookKeeper;
import org.apache.pulsar.broker.ServiceConfiguration;
import org.apache.zookeeper.ZooKeeper;

/**
 * Provider of a new BookKeeper client instance
 *
 * <p>
 *     这是一个工厂类接口，用来提供{@link BookKeeper} 客户端实例。
 * </p>
 *
 * @see BookKeeper
 * @see ZooKeeper
 */
public interface BookKeeperClientFactory {
    /**
     *
     * @param conf
     * @param zkClient 使用的是zookeeper原生客户端{@link ZooKeeper}
     * @return {@link BookKeeper} 客户端实例
     * @throws IOException IO交互异常
     */
    BookKeeper create(ServiceConfiguration conf, ZooKeeper zkClient) throws IOException;
    /**
     * <em>注意：这里的{@link #close()} 方法
     *        <b>不是</b>
     *        用来关闭 {@link BookKeeper} 客户端的</em>
     *
     * @see BookKeeperClientFactoryImpl#close()
     */
    void close();
}

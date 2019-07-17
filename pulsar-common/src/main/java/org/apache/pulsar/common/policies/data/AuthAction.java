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
package org.apache.pulsar.common.policies.data;

/**
 * Authorization action for Pulsar policies
 *
 * <p>
 *     Pulsar授权的操作
 * </p>
 * <em>枚举常量建议大写</em>
 */
public enum AuthAction {
    /**
     * Permission to produce/publish messages
     *
     * <p>
     * 允许生产/发布消息
     * </p>
     */
    produce,

    /**
     * Permission to consume messages
     *
     * <p>
     *     允许消费消息
     * </p>
     */
    consume,
}

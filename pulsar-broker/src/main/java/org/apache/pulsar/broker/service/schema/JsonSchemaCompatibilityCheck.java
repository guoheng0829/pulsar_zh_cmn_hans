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
package org.apache.pulsar.broker.service.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import java.io.IOException;
import org.apache.pulsar.common.schema.SchemaData;
import org.apache.pulsar.common.schema.SchemaType;

@SuppressWarnings("unused")
public class JsonSchemaCompatibilityCheck implements SchemaCompatibilityCheck {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.JSON;
    }

    @Override
    public boolean isCompatible(SchemaData from, SchemaData to) {
        try {
            JsonSchema fromSchema = objectMapper.readValue(from.getData(), JsonSchema.class);
            JsonSchema toSchema = objectMapper.readValue(to.getData(), JsonSchema.class);
            return fromSchema.getId().equals(toSchema.getId());
        } catch (IOException e) {
            return false;
        }
    }
}

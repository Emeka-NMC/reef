/**
 * Copyright (C) 2014 Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.reef.webserver;

import com.microsoft.reef.driver.evaluator.EvaluatorDescriptor;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serializer for Evaluator list
 */
public class AvroEvaluatorListSerializer {
  public AvroEvaluatorList toAvro(final Map<String, EvaluatorDescriptor> evaluatorMap, final int totalEvaluators, final String startTime) {
    final List<AvroEvaluatorEntry> EvaluatorEntities = new ArrayList<>();
    for (final Map.Entry<String, EvaluatorDescriptor> entry : evaluatorMap.entrySet()) {
      final String key = entry.getKey();
      final EvaluatorDescriptor descriptor = entry.getValue();
      EvaluatorEntities.add(AvroEvaluatorEntry.newBuilder()
          .setId(key)
          .setName(descriptor.getNodeDescriptor().getName())
          .build());
    }

    return AvroEvaluatorList.newBuilder()
        .setEvaluators(EvaluatorEntities)
        .setTotal(totalEvaluators)
        .setStartTime(startTime)
        .build();
  }

  public String toString(final AvroEvaluatorList avroEvaluatorList) {
    final DatumWriter<AvroEvaluatorList> evaluatorWriter = new SpecificDatumWriter<>(AvroEvaluatorList.class);
    final String result;
    try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      final JsonEncoder encoder = EncoderFactory.get().jsonEncoder(avroEvaluatorList.getSchema(), out);
      evaluatorWriter.write(avroEvaluatorList, encoder);
      encoder.flush();
      out.flush();
      result = out.toString(AvroHttpSerializer.JSON_CHARSET);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }
}

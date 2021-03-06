/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.genomics.dataflow.readers;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.SearchVariantsRequest;
import com.google.api.services.genomics.model.Variant;
import com.google.cloud.genomics.utils.GenomicsFactory;
import com.google.cloud.genomics.utils.Paginator;
import com.google.cloud.genomics.utils.Paginator.ShardBoundary;

import java.io.IOException;
import java.util.logging.Logger;

public class VariantReader extends GenomicsApiReader<SearchVariantsRequest, Variant> {
  private static final Logger LOG = Logger.getLogger(VariantReader.class.getName());
  private final ShardBoundary shardBoundary;

  /**
   * Create a VariantReader using a auth and fields parameter. All fields not specified under 
   * variantFields will not be returned in the API response.
   * 
   * @param auth Auth class containing credentials.
   * @param variantFields Fields to return in responses.
   */
  public VariantReader(GenomicsFactory.OfflineAuth auth, ShardBoundary shardBoundary, String variantFields) {
    super(auth, variantFields);
    this.shardBoundary = shardBoundary;
  }

  /**
   * Create a VariantReader with no fields parameter, all information will be returned.
   * @param auth Auth class containing credentials.
   */
  public VariantReader(GenomicsFactory.OfflineAuth auth, ShardBoundary shardBoundary) {
    this(auth, shardBoundary, null);
  }

  @Override
  protected void processApiCall(Genomics genomics, ProcessContext c, SearchVariantsRequest request)
      throws IOException {
    LOG.info("Starting Variants read loop: " + request);

    int numberOfVariants = 0;
    for (Variant variant : Paginator.Variants.create(genomics, shardBoundary).search(request, fields)) {
      c.output(variant);
      ++numberOfVariants;
      itemCount.addValue(1L);
    }

    LOG.info("Read " + numberOfVariants + " variants at: " + request.getReferenceName() + "-" + "["
        + request.getStart() + ", " + request.getEnd() + "]");
  }
}
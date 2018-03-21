/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.giraph.partition;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

/**
 * Divides the vertices into partitions by their hash code using a simple
 * round-robin hash for great balancing if given a random hash code.
 *
 * @param <I> Vertex id value
 * @param <V> Vertex value
 * @param <E> Edge value
 */
public class HashPartitionerFactory<I extends WritableComparable,
    V extends Writable, E extends Writable>
    extends GraphPartitionerFactory<I, V, E> {

  @Override
  public int getPartition(I id, int partitionCount, int workerCount) {
    return Math.abs(id.hashCode() % partitionCount);
        /*
    int id1 = Integer.parseInt(id.toString());
    if(0 <= id1 && id1 <= 500000){
        return 0;
    }
    if(500001 <= id1 && id1 <= 1000000){
        return 3;
    }
    if(1000001 <= id1 && id1 <= 1500000){
        return 6;
    }
    if(1500001 <= id1 && id1 <= 1666668){
        return 1;
    }
    if(1666669 <= id1 && id1 <= 1833336){
        return 4;
    }
    if(1833337 <= id1 && id1 <= 2000000){
        return 7;
    }
    if(2000001 <= id1 && id1 <= 3000000){
        return 2;
    }
    if(3000001 <= id1 && id1 <= 4000000){
        return 5;
    }
    if(4000001 <= id1 && id1 <= 4847570){
        return 8;
    }else{
        return 0;
    }
    */
  }

  @Override
  public int getWorker(int partition, int partitionCount, int workerCount) {
    return partition % workerCount;
  }
}

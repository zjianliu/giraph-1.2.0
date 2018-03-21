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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * Used to keep track of statistics of every {@link Partition}. Contains no
 * actual partition data, only the statistics.
 */
public class PartitionStats implements Writable {
  /** Id of partition to keep stats for */
  private int partitionId = -1;
  /** Vertices in this partition */
  private long vertexCount = 0;
  /** Finished vertices in this partition */
  private long finishedVertexCount = 0;
  /** computed vertices in this partition */
  private long computedVertexCount = 0;
  /** Edges in this partition */
  private long edgeCount = 0;
  /** Messages sent from this partition to other worker*/
  private long messagesSentToOtherWorkerCount = 0;
  /** Messages sent from this partition to itself*/
  private long messagesSentToItselfCount = 0;
  /** Message byetes sent from this partition */
  private long messageBytesSentCount = 0;

  /**
   * Default constructor for reflection.
   */
  public PartitionStats() { }

  /**
   * Constructor with the initial stats.
   *
   * @param partitionId Partition count.
   * @param vertexCount Vertex count.
   * @param finishedVertexCount Finished vertex count.
   * @param computedVertexCount Computed vertices count.
   * @param edgeCount Edge count.
   * @param messagesSentToOtherWorkerCount Number of messages sent to other worker
   * @param messageBytesSentCount Number of message bytes sent
   */
  public PartitionStats(int partitionId,
      long vertexCount,
      long finishedVertexCount,
      long computedVertexCount,
      long edgeCount,
      long messagesSentToOtherWorkerCount,
      long messagesSentToItselfCount,
      long messageBytesSentCount) {
    this.partitionId = partitionId;
    this.vertexCount = vertexCount;
    this.finishedVertexCount = finishedVertexCount;
    this.computedVertexCount = computedVertexCount;
    this.edgeCount = edgeCount;
    this.messagesSentToOtherWorkerCount = messagesSentToOtherWorkerCount;
    this.messagesSentToItselfCount = messagesSentToItselfCount;
    this.messageBytesSentCount = messageBytesSentCount;
  }

  /**
   * Set the partition id.
   *
   * @param partitionId New partition id.
   */
  public void setPartitionId(int partitionId) {
    this.partitionId = partitionId;
  }

  /**
   * Get partition id.
   *
   * @return Partition id.
   */
  public int getPartitionId() {
    return partitionId;
  }

  /**
   * Increment the vertex count by one.
   */
  public void incrVertexCount() {
    ++vertexCount;
  }

  /**
   * Get the vertex count.
   *
   * @return Vertex count.
   */
  public long getVertexCount() {
    return vertexCount;
  }

  /**
   * Increment the finished vertex count by one.
   */
  public void incrFinishedVertexCount() {
    ++finishedVertexCount;
  }

  /**
   * Get the finished vertex count.
   *
   * @return Finished vertex count.
   */
  public long getFinishedVertexCount() {
    return finishedVertexCount;
  }

  /**
   * Add edges to the edge count.
   *
   * @param edgeCount Number of edges to add.
   */
  public void addEdgeCount(long edgeCount) {
    this.edgeCount += edgeCount;
  }

  /**
   * Increment the computed vertex count by one.
   */
  public void increComputedVertexCount() {
    ++computedVertexCount;
  }

  /**
   * Get the computed vertex count.
   * @return Computed vertex count.
   */
  public long getComputedVertexCount() {
    return computedVertexCount;
  }

  /**
   * Get the edge count.
   *
   * @return Edge count.
   */
  public long getEdgeCount() {
    return edgeCount;
  }

  /**
   * Add messages to the messages sent count.
   *
   * @param messagesSentCount Number of messages to add.
   */
  public void addMessagesSentToItselfCount(long messagesSentCount) {
    this.messagesSentToItselfCount += messagesSentCount;
  }

  /**
   * Get the messages sent count.
   *
   * @return Messages sent count.
   */
  public long getMessagesSentToItselfCount() {
    return messagesSentToItselfCount;
  }

  /**
   * Add messages to the messages sent count.
   *
   * @param messagesSentCount Number of messages to add.
   */
  public void addMessagesSentToOtherWorkerCount(long messagesSentCount) {
    this.messagesSentToOtherWorkerCount += messagesSentCount;
  }

  /**
   * Get the messages sent count.
   *
   * @return Messages sent count.
   */
  public long getMessagesSentToOtherWorkerCount() {
    return messagesSentToOtherWorkerCount;
  }

  /**
   * Add message bytes to messageBytesSentCount.
   *
   * @param messageBytesSentCount Number of message bytes to add.
   */
  public void addMessageBytesSentCount(long messageBytesSentCount) {
    this.messageBytesSentCount += messageBytesSentCount;
  }

  /**
   * Get the message bytes sent count.
   *
   * @return Message bytes sent count.
   */
  public long getMessageBytesSentCount() {
    return messageBytesSentCount;
  }

  @Override
  public void readFields(DataInput input) throws IOException {
    partitionId = input.readInt();
    vertexCount = input.readLong();
    finishedVertexCount = input.readLong();
    computedVertexCount = input.readLong();
    edgeCount = input.readLong();
    messagesSentToOtherWorkerCount = input.readLong();
    messagesSentToItselfCount = input.readLong();
    messageBytesSentCount = input.readLong();
  }

  @Override
  public void write(DataOutput output) throws IOException {
    output.writeInt(partitionId);
    output.writeLong(vertexCount);
    output.writeLong(finishedVertexCount);
    output.writeLong(computedVertexCount);
    output.writeLong(edgeCount);
    output.writeLong(messagesSentToOtherWorkerCount);
    output.writeLong(messagesSentToItselfCount);
    output.writeLong(messageBytesSentCount);
  }

  @Override
  public String toString() {
    return "(id=" + partitionId + ",vtx=" + vertexCount + ",finVtx=" +
        finishedVertexCount + ",computedVtx=" + computedVertexCount +
            ",edges=" + edgeCount + ",msgsSentToOther=" +
        messagesSentToOtherWorkerCount + "msgsSentToItself" +
        messagesSentToItselfCount + ",msgBytesSent=" +
          messageBytesSentCount + ")";
  }
}

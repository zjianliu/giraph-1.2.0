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

package org.apache.giraph.master;

import org.apache.giraph.bsp.ApplicationState;
import org.apache.giraph.bsp.BspService;
import org.apache.giraph.bsp.CentralizedServiceMaster;
import org.apache.giraph.bsp.SuperstepState;
import org.apache.giraph.counters.GiraphTimers;
import org.apache.giraph.graph.Computation;
import org.apache.giraph.metrics.GiraphMetrics;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static org.apache.giraph.conf.GiraphConstants.USE_SUPERSTEP_COUNTERS;

/**
 * Master thread that will coordinate the activities of the tasks.  It runs
 * on all task processes, however, will only execute its algorithm if it knows
 * it is the "leader" from ZooKeeper.
 *
 * @param <I> Vertex id
 * @param <V> Vertex value
 * @param <E> Edge value
 */
@SuppressWarnings("rawtypes")
public class MasterThread<I extends WritableComparable, V extends Writable,
    E extends Writable> extends Thread {
  /** Counter group name for the Giraph timers */
  public static final String GIRAPH_TIMERS_COUNTER_GROUP_NAME = "Giraph Timers";
  /** Class logger */
  private static final Logger LOG = Logger.getLogger(MasterThread.class);
  /** Reference to shared BspService */
  private CentralizedServiceMaster<I, V, E> bspServiceMaster = null;
  /** Context (for counters) */
  private final Context context;
  /** Use superstep counters? */
  private final boolean superstepCounterOn;
  /** Setup seconds */
  private double setupSecs = 0d;
  /** Superstep timer (in seconds) map */
  private final Map<Long, List<Long>> superstepSecsMap =
      new TreeMap();

  /**
   * Constructor.
   *
   * @param bspServiceMaster Master that already exists and setup() has
   *        been called.
   * @param context Context from the Mapper.
   */
  public MasterThread(CentralizedServiceMaster<I, V, E> bspServiceMaster,
      Context context) {
    super(MasterThread.class.getName());
    this.bspServiceMaster = bspServiceMaster;
    this.context = context;
    GiraphTimers.init(context);
    superstepCounterOn = USE_SUPERSTEP_COUNTERS.get(context.getConfiguration());
  }

  /**
   * The master algorithm.  The algorithm should be able to withstand
   * failures and resume as necessary since the master may switch during a
   * job.
   */
  @Override
  public void run() {
    // Algorithm:
    // 1. Become the master
    // 2. If desired, restart from a manual checkpoint
    // 3. Run all supersteps until complete
    try {
      long startMillis = System.currentTimeMillis();
      long initializeMillis = 0;
      long endMillis = 0;
      bspServiceMaster.setup();
      SuperstepState superstepState = SuperstepState.INITIAL;

      if (bspServiceMaster.becomeMaster()) {
        // First call to checkWorkers waits for all pending resources.
        // If these resources are still available at subsequent calls it just
        // reads zookeeper for the list of healthy workers.
        bspServiceMaster.checkWorkers();// 循环检测直到满足数量的worker数
        initializeMillis = System.currentTimeMillis();
        GiraphTimers.getInstance().getInitializeMs().increment(
            initializeMillis - startMillis);
        // Attempt to create InputSplits if necessary. Bail out if that fails.
        // Attempt to create InputSplits if necessary. Bail out if that fails.
        //master开始去创建InputSplits，worker在 startSuperstep中等待呢
        if (bspServiceMaster.getRestartedSuperstep() !=
            BspService.UNSET_SUPERSTEP ||
            (bspServiceMaster.createMappingInputSplits() != -1 &&   // 如果用户在命令行中没有提供对应的InputFormat，
                                                                    // 返回0;如成功，返回inputSplit的个数
                bspServiceMaster.createVertexInputSplits() != -1 && // 这里会创建好splits  _vertexInputSplitDir => _vertexInputSplitsAllReady这两个都会创建成功
                bspServiceMaster.createEdgeInputSplits() != -1)) {
          long setupMillis = System.currentTimeMillis() - initializeMillis;
          GiraphTimers.getInstance().getSetupMs().increment(setupMillis);
          setupSecs = setupMillis / 1000.0d;

          /** Nothing happened yet
           =>INITIAL(false),
           A worker died during this superstep
           =>WORKER_FAILURE(false),
           This superstep completed correctly
           =>THIS_SUPERSTEP_DONE(false),
           All supersteps are complete
           =>ALL_SUPERSTEPS_DONE(true),
           Execution halted
           =>CHECKPOINT_AND_HALT(true);
           */
          //在超步开始之前，master先完成对输入文件切块产生inputSplits
          while (!superstepState.isExecutionComplete()) {
            long startSuperstepMillis = System.currentTimeMillis();
            long cachedSuperstep = bspServiceMaster.getSuperstep();
            GiraphMetrics.get().resetSuperstepMetrics(cachedSuperstep);
            Class<? extends Computation> computationClass =
                bspServiceMaster.getMasterCompute().getComputation();
            LOG.info("==============================================================="); //从superstep -1 开始
            LOG.info("The superstep " + cachedSuperstep + " starts....");
            superstepState = bspServiceMaster.coordinateSuperstep();
            LOG.info("The superstep " + cachedSuperstep + " ends....");
            LOG.info("===============================================================");
            long superstepMillis = System.currentTimeMillis() -
                startSuperstepMillis;
            List<Long> superstepTime = new ArrayList<>();
            superstepTime.add(startSuperstepMillis);
            superstepTime.add(superstepMillis);
            superstepSecsMap.put(cachedSuperstep, superstepTime);
            if (LOG.isInfoEnabled()) {
              LOG.info("masterThread: Coordination of superstep " +
                  cachedSuperstep + " took " +
                  superstepMillis / 1000.0d +
                  " seconds ended with state " + superstepState +
                  " and is now on superstep " +
                  bspServiceMaster.getSuperstep());
            }
            if (superstepCounterOn) {
              String computationName = (computationClass == null) ?
                  null : computationClass.getSimpleName();
              GiraphTimers.getInstance().getSuperstepMs(cachedSuperstep,
                  computationName).increment(superstepMillis);
            }

            bspServiceMaster.postSuperstep();

            // If a worker failed, restart from a known good superstep
            if (superstepState == SuperstepState.WORKER_FAILURE) {
              bspServiceMaster.restartFromCheckpoint(
                  bspServiceMaster.getLastGoodCheckpoint());
            }
            endMillis = System.currentTimeMillis();
          }
          bspServiceMaster.setJobState(ApplicationState.FINISHED, -1, -1);
        }
      }
      bspServiceMaster.cleanup(superstepState);
      if (!superstepSecsMap.isEmpty()) {
        GiraphTimers.getInstance().getShutdownMs().
          increment(System.currentTimeMillis() - endMillis);
        if (LOG.isInfoEnabled()) {
          LOG.info("setup: Took " + setupSecs + " seconds.");
        }

        bspServiceMaster.writeSuperstepTimeIntoHDFS(superstepSecsMap);

        for (Entry<Long, List<Long>> entry : superstepSecsMap.entrySet()) {
          if (LOG.isInfoEnabled()) {
            if (entry.getKey().longValue() ==
                BspService.INPUT_SUPERSTEP) {
              LOG.info("input superstep: Took " +
                  entry.getValue().get(1)/1000d + " seconds.");
            } else {
              LOG.info("superstep " + entry.getKey() + ": Took " +
                  entry.getValue().get(1)/1000d + " seconds.");
            }
          }
          context.progress();
        }
        if (LOG.isInfoEnabled()) {
          LOG.info("shutdown: Took " +
              (System.currentTimeMillis() - endMillis) /
              1000.0d + " seconds.");
          LOG.info("total: Took " +
              ((System.currentTimeMillis() - initializeMillis) /
              1000.0d) + " seconds.");
        }
        GiraphTimers.getInstance().getTotalMs().
          increment(System.currentTimeMillis() - initializeMillis);
      }
      bspServiceMaster.postApplication();
      // CHECKSTYLE: stop IllegalCatchCheck
    } catch (Exception e) {
      // CHECKSTYLE: resume IllegalCatchCheck
      LOG.error("masterThread: Master algorithm failed with " +
          e.getClass().getSimpleName(), e);
      bspServiceMaster.failureCleanup(e);
      throw new IllegalStateException(e);
    }
  }
}

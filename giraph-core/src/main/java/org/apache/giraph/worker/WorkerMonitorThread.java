package org.apache.giraph.worker;

import org.apache.giraph.bsp.BspService;
import org.apache.giraph.graph.GraphTaskManager;
import org.apache.giraph.monitor.Monitor;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.net.Socket;

public class WorkerMonitorThread<I extends WritableComparable, V extends Writable,
        E extends Writable> extends Thread {
    private final Logger LOG = Logger.getLogger(WorkerMonitorThread.class);

    private GraphTaskManager<I, V, E> graphTaskManager;
    private Mapper<?, ?, ?, ?>.Context context;
    private long rxBytesBefore = 0;
    private long txBytesBefore = 0;
    private long timeBefore = 0;

    public WorkerMonitorThread(GraphTaskManager<I, V, E> graphTaskManager, Mapper<?, ?, ?, ?>.Context context){
        this.graphTaskManager = graphTaskManager;
        this.context = context;
    }

    @Override
    public void run() {
        try{
            if(LOG.isInfoEnabled()){
                LOG.info("WorkerMonitorThread starts to monitor the giraph system.");
            }
            Socket socket = graphTaskManager.getMonitorSocket();
            final PrintWriter pw = new PrintWriter(socket.getOutputStream());


            Monitor monitor = new Monitor(context);

            while(!graphTaskManager.isApplicationFinished()){
                String[] systemStatus = graphTaskManager.getWorkerSystemStatus(monitor);
                StringBuffer status = new StringBuffer();

                String hostname = systemStatus[0];
                long time = Long.parseLong(systemStatus[1]);
                double cpuUser = Double.parseDouble(systemStatus[2]);
                double memoryUsage = Double.parseDouble(systemStatus[3]);
                long rxBytesAfter = Long.parseLong(systemStatus[4]);
                long txBytesAfter = Long.parseLong(systemStatus[5]);

                long rxbps = 0;
                long txbps = 0;

                if(rxBytesBefore != 0 && txBytesBefore != 0){
                    rxbps = (rxBytesAfter - rxBytesBefore) * 8 / (time - timeBefore) * 1000;
                    txbps = (txBytesAfter - txBytesBefore) * 8 / (time - timeBefore) * 1000;
                }

                status.append("giraph." + hostname + ".cpuUser " + cpuUser + " " + time/1000 + "\n");
                status.append("giraph." + hostname + ".memoryUsage " + memoryUsage + " " + time/1000 + "\n");
                status.append("giraph." + hostname + ".totalNetworkup " + txbps + " " + time/1000 + "\n");
                status.append("giraph." + hostname + ".totalNetworkdown " + rxbps + " " + time/1000);

                pw.println(status);
                pw.flush();

                rxBytesBefore = rxBytesAfter;
                txBytesBefore = txBytesAfter;
                timeBefore = time;

                Thread.sleep(200);
            }
            socket.shutdownOutput();
            socket.close();

        } catch (Exception e){
            if(LOG.isInfoEnabled()) {
                LOG.info("WorkerMonitorThread run failed: " + e);
            }
        }
    }
}



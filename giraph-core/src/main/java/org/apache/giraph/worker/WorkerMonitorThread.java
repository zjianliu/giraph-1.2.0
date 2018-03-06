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
                String systemStatus = graphTaskManager.getWorkerSystemStatus(monitor);
                pw.println(systemStatus);
                pw.flush();

                Thread.sleep(300);
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



package org.apache.giraph.monitor;

import java.util.Date;

/**
 * Created by 11363 on 5/31/2017.
 */
public class Metrics {
    public double getCpuUser() {
        return cpuUser;
    }

    public double getMemoryUsed() {
        return memoryUsed;
    }

    public long getMemoryTotal() {
        return memoryTotal;
    }

    public long getRxBytes() {
        return rxBytes;
    }

    public long getTxBytes() {
        return txBytes;
    }

    public Date getTime() {
        return time;
    }

    private double cpuUser;
    private double memoryUsed;
    private long memoryTotal;
    private long rxBytes;
    private long txBytes;
    private Date time;

    public Metrics(double cpuUser, double memoryUsed, long memoryTotal,
                   long rxBytes, long txBytes, Date time){
        this.cpuUser = cpuUser;
        this.memoryUsed = memoryUsed;
        this.memoryTotal = memoryTotal;
        this.rxBytes = rxBytes;
        this.txBytes = txBytes;
        this.time = time;
    }

}

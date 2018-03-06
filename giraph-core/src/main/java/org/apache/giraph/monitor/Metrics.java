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

    public int getTotalNetworkup() {
        return totalNetworkup;
    }

    public int getTotalNetworkdown() {
        return totalNetworkdown;
    }

    public Date getTime() {
        return time;
    }

    private double cpuUser;
    private double memoryUsed;
    private long memoryTotal;
    private int totalNetworkup;
    private int totalNetworkdown;
    private Date time;

    public Metrics(double cpuUser, double memoryUsed, long memoryTotal,
                   int totalNetworkup, int totalNetworkdown, Date time){
        this.cpuUser = cpuUser;
        this.memoryUsed = memoryUsed;
        this.memoryTotal = memoryTotal;
        this.totalNetworkup = totalNetworkup;
        this.totalNetworkdown = totalNetworkdown;
        this.time = time;
    }

}

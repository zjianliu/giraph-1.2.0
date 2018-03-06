package org.apache.giraph.monitor;

import org.apache.giraph.utils.SigarUtil;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.util.Date;

public class Monitor {
    private Sigar sigar;
    private static final Logger LOG = Logger.getLogger(Monitor.class);

    public Monitor(Mapper<?, ?, ?, ?>.Context context) {
        try {
            sigar = SigarUtil.getSigar(context);

            if(sigar == null)
                throw new NullPointerException("sigar is null!");

        }catch (Exception e){
            LOG.info("Monitor: initialization failed.");
            throw new IllegalStateException("Monitor: " + e);
        }
    }

    public Metrics getMetrics() throws SigarException, UnsatisfiedLinkError {
        double cpuUser = sigar.getCpuPerc().getUser();
        double memoryUsed = sigar.getMem().getActualUsed();
        long memoryTotal = sigar.getMem().getTotal();
        int totalNetworkup = sigar.getNetStat().getAllOutboundTotal();
        int totalNetworkdown = sigar.getNetStat().getAllInboundTotal();
        Date time = new Date();

        return new Metrics(cpuUser, memoryUsed, memoryTotal, totalNetworkup, totalNetworkdown, time);
    }
}

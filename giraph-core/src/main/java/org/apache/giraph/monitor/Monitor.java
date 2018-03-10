package org.apache.giraph.monitor;

import org.apache.giraph.utils.SigarUtil;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
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
        Date time = new Date();
        double cpuUser = sigar.getCpuPerc().getUser();
        double memoryUsed = sigar.getMem().getActualUsed();
        long memoryTotal = sigar.getMem().getTotal();
        //int totalNetworkup = sigar.getNetStat().getAllOutboundTotal();
        //int totalNetworkdown = sigar.getNetStat().getAllInboundTotal();
        //String ifNames[] = sigar.getNetInterfaceList();
        NetInterfaceStat ifstat = sigar.getNetInterfaceStat("ens3");
        long rxBytesStart = ifstat.getRxBytes();
        long txBytesStart = ifstat.getTxBytes();



        return new Metrics(cpuUser, memoryUsed, memoryTotal, rxBytesStart, txBytesStart, time);
    }
}

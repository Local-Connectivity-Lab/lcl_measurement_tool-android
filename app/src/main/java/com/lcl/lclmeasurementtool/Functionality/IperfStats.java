package com.lcl.lclmeasurementtool.Functionality;

public class IperfStats {

    private double senderBandwidth;
    private double receiverBandwidth;
    private String bandwidth;
    private String senderBandwidthUnits;
    private String receiverBandwidthUnits;
    private String fullOutput;
    private IperfError error;

    public IperfStats () {
        this.senderBandwidth = 0.0;
        this.bandwidth = "1M";
    }

    public void setFullOutput(String FullOutput) {
        fullOutput = FullOutput;
    }

    public void setSenderBandwidth(double d) {
        this.senderBandwidth = d;
    }

    public void setReceiverBandwidth(double d) { this.receiverBandwidth = d;}

    public void setBandwidth (String s) {
        this.bandwidth = s;
    }

    public void setError(IperfError error) {
        this.error = error;
    }

    public String getBandwidth() {
        return bandwidth;
    }

    public double getSenderBandwidth() {
        return senderBandwidth;
    }
    public double getReceiverBandwidth() { return receiverBandwidth;}
    public boolean hasError() {
        return error != null;
    }

    public IperfError getError() {
        return error;
    }
    public String getFullOutput() {
        return fullOutput;
    }

    public static String setTestOutput()
    {
        StringBuilder test =  new StringBuilder()
                .append("[  4] local 192.168.1.30 port 53688 connected to 192.168.1.30 port 5201\n")
                .append("[ ID] Interval           Transfer     Bandwidth\n")
                .append("[  4]   0.00-1.00   sec   112 MBytes   940 Mbits/sec\n")
                .append("[  4]   0.00-1.00   sec   112 MBytes   940 Mbits/sec\n")
                .append("[  4]   0.00-1.00   sec   112 MBytes   940 Mbits/sec\n")
                .append("[  4]   1.00-2.00   sec   119 MBytes   996 Mbits/sec\n")
                .append("[  4]   2.00-3.01   sec   119 MBytes   999 Mbits/sec\n")
                .append("[  4]   3.01-4.01   sec   120 MBytes  1.01 Gbits/sec\n")
                .append("[  4]   4.01-5.00   sec   118 MBytes   997 Mbits/sec\n")
                .append("[  4]   5.00-6.01   sec   118 MBytes   978 Mbits/sec\n")
                .append("[  4]   6.01-7.01   sec   119 MBytes  1.00 Gbits/sec\n")
                .append("[  4]   7.01-8.01   sec   119 MBytes  1.00 Gbits/sec\n")
                .append("[  4]   8.01-9.01   sec   119 MBytes  1.00 Gbits/sec\n")
                .append("[  4]   9.01-10.01  sec   121 MBytes  1.02 Gbits/sec\n")
                .append("- - - - - - - - - - - - - - - - - - - - - - - - -\n")
                .append("[ ID] Interval           Transfer     Bandwidth\n")
                .append("[  4]   0.00-10.01  sec  1.16 GBytes   994 Mbits/sec                  sender\n")
                .append("[  4]   0.00-10.01  sec  1.16 GBytes   994 Mbits/sec                  receiver\n");
        return test.toString();


    }


    public void setSenderBandwidthUnits(String senderBandwidthUnits) {
        this.senderBandwidthUnits = senderBandwidthUnits;
    }

    public void setReceiverBandwidthUnits(String receiverBandwidthUnits) {
        this.receiverBandwidthUnits = receiverBandwidthUnits;
    }
}

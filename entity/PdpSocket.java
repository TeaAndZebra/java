package entity;

import java.io.Serializable;

public class PdpSocket implements Serializable {
    private int pdpAdd; //pdp地址
    private byte pdpPort; //端口

    public PdpSocket(int pdpAdd, byte pdpPort){
        this.pdpAdd = pdpAdd;
        this.pdpPort = pdpPort;
    }

    public void setPdpAdd(int pdpAdd) {
        this.pdpAdd = pdpAdd;
    }

    public void setPdpPort(byte pdpPort) {
        this.pdpPort = pdpPort;
    }

    public byte getPdpPort() {
        return pdpPort;
    }

    public int getPdpAdd() {
        return pdpAdd;
    }

    @Override
    public String toString() {
        return pdpAdd+":"+pdpPort;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PdpSocket other = (PdpSocket) obj;
        if (pdpAdd != other.pdpAdd)
            return false;
        if (pdpPort != other.pdpPort)
            return false;
        return true;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pdpPort;
        result = prime * result + pdpAdd;
        return result;
    }

}

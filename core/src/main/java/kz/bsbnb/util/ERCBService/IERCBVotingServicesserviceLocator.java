/**
 * IERCBVotingServicesserviceLocator.java
 * <p>
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package kz.bsbnb.util.ERCBService;

import org.springframework.beans.factory.annotation.Autowired;

public class IERCBVotingServicesserviceLocator extends org.apache.axis.client.Service implements IERCBVotingServicesservice {

    @Autowired
    private ERCBProperties ercbProperties;

    public IERCBVotingServicesserviceLocator() {
    }


    public IERCBVotingServicesserviceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public IERCBVotingServicesserviceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for IERCBVotingServicesPort
    private java.lang.String IERCBVotingServicesPort_address = "http://192.168.100.2:8083/soap/IERCBVotingServices";

    public java.lang.String getIERCBVotingServicesPortAddress() {
        return ercbProperties.getSoap_port();
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String IERCBVotingServicesPortWSDDServiceName = "IERCBVotingServicesPort";

    public java.lang.String getIERCBVotingServicesPortWSDDServiceName() {
        return IERCBVotingServicesPortWSDDServiceName;
    }

    public void setIERCBVotingServicesPortWSDDServiceName(java.lang.String name) {
        IERCBVotingServicesPortWSDDServiceName = name;
    }

    public IERCBVotingServices getIERCBVotingServicesPort() throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(IERCBVotingServicesPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getIERCBVotingServicesPort(endpoint);
    }

    public IERCBVotingServices getIERCBVotingServicesPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            IERCBVotingServicesbindingStub _stub = new IERCBVotingServicesbindingStub(portAddress, this);
            _stub.setPortName(getIERCBVotingServicesPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setIERCBVotingServicesPortEndpointAddress(java.lang.String address) {
        IERCBVotingServicesPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (IERCBVotingServices.class.isAssignableFrom(serviceEndpointInterface)) {
                IERCBVotingServicesbindingStub _stub = new IERCBVotingServicesbindingStub(new java.net.URL(IERCBVotingServicesPort_address), this);
                _stub.setPortName(getIERCBVotingServicesPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("IERCBVotingServicesPort".equals(inputPortName)) {
            return getIERCBVotingServicesPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "IERCBVotingServicesservice");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "IERCBVotingServicesPort"));
        }
        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

        if ("IERCBVotingServicesPort".equals(portName)) {
            setIERCBVotingServicesPortEndpointAddress(address);
        }
        else
        { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

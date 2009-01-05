
/*
 * 
 */

package org.openehealth.ipf.platform.camel.lbs.process.cxf;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * This class was generated by Apache CXF 2.1.3
 * Fri Dec 19 11:18:32 CET 2008
 * Generated source version: 2.1.3
 * 
 */


@WebServiceClient(name = "SOAPService", 
                  wsdlLocation = "file:/C:/dev/svn/ipf-research/platform-camel/lbs/src/test/resources/hello_world.wsdl",
                  targetNamespace = "http://cxf.process.lbs.camel.platform.ipf.openehealth.org/") 
public class SOAPService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://cxf.process.lbs.camel.platform.ipf.openehealth.org/", "SOAPService");
    public final static QName SoapOverHttp = new QName("http://cxf.process.lbs.camel.platform.ipf.openehealth.org/", "SoapOverHttp");
    static {
        URL url = null;
        try {
            url = new URL("file:/C:/dev/svn/ipf-research/platform-camel/lbs/src/test/resources/hello_world.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:/C:/dev/svn/ipf-research/platform-camel/lbs/src/test/resources/hello_world.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public SOAPService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public SOAPService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SOAPService() {
        super(WSDL_LOCATION, SERVICE);
    }

    /**
     * 
     * @return
     *     returns Greeter
     */
    @WebEndpoint(name = "SoapOverHttp")
    public Greeter getSoapOverHttp() {
        return super.getPort(SoapOverHttp, Greeter.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns Greeter
     */
    @WebEndpoint(name = "SoapOverHttp")
    public Greeter getSoapOverHttp(WebServiceFeature... features) {
        return super.getPort(SoapOverHttp, Greeter.class, features);
    }

}

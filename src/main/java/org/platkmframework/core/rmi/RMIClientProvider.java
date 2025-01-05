/**
 * ****************************************************************************
 *  Copyright(c) 2023 the original author Eduardo Iglesias Taylor.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	 https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *  	Eduardo Iglesias Taylor - initial API and implementation
 * *****************************************************************************
 */
package org.platkmframework.core.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class RMIClientProvider {

    /**
     * Atributo logger
     */
    private static Logger logger = LoggerFactory.getLogger(RMIClientProvider.class);

    /**
     * Atributo hostname
     */
    private String hostname;

    /**
     * Atributo port
     */
    private String port;

    /**
     * Atributo lookupName
     */
    private String lookupName;

    /**
     * Constructor RMIClientProvider
     * @param hostname hostname
     * @param port port
     * @param lookupName lookupName
     */
    public RMIClientProvider(String hostname, String port, String lookupName) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.lookupName = lookupName;
    }

    /**
     * provide 
     * @param <E> E
     * @param client client
     * @return Remote
     * @throws RMIException RMIException
     */
    public <E extends Remote> E provide(Class<E> client) throws RMIException {
        try {
            Registry registry;
            if (!hostname.isBlank() && !port.isBlank()) {
                registry = LocateRegistry.getRegistry(hostname, Integer.valueOf(port));
            } else if (!hostname.isBlank()) {
                registry = LocateRegistry.getRegistry(hostname);
            } else if (!port.isBlank()) {
                registry = LocateRegistry.getRegistry(Integer.valueOf(port));
            } else {
                registry = LocateRegistry.getRegistry();
            }
            return client.cast(registry.lookup(lookupName));
        } catch (RemoteException | NotBoundException | IllegalArgumentException e) {
            logger.error(e.getMessage());
            throw new RMIException(e);
        }
    }

    /**
     * getHostname
     * @return String
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * setHostname
     * @param hostname hostname
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * getPort
     * @return String
     */
    public String getPort() {
        return port;
    }

    /**
     * setPort
     * @param port port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * getLookupName
     * @return String
     */
    public String getLookupName() {
        return lookupName;
    }

    /**
     * setLookupName
     * @param lookupName lookupName
     */
    public void setLookupName(String lookupName) {
        this.lookupName = lookupName;
    }
}

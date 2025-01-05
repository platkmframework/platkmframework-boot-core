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

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.platkmframework.annotation.rmi.RMIServer;
import org.platkmframework.context.ObjectContainer;
import org.platkmframework.context.project.ProjectContent;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class RMIServerManager {

    /**
     * Atributo schedulerManager
     */
    private static RMIServerManager schedulerManager;

    /**
     * Atributo map
     */
    Map<String, Remote> map;

    /**
     * Atributo running
     */
    Map<String, Remote> running;

    /**
     * Constructor RMIServerManager
     */
    private RMIServerManager() {
        map = new HashMap<>();
        running = new HashMap<>();
    }

    /**
     * instance
     * @return RMIServerManager
     */
    public static RMIServerManager instance() {
        if (schedulerManager == null)
            schedulerManager = new RMIServerManager();
        return schedulerManager;
    }

    /**
     * run
     * @param name name
     * @throws RMIException RMIException
     */
    public void run(String name) throws RMIException {
        try {
            Object rmiServerObj = map.get(name);
            RMIServer rmiServer = rmiServerObj.getClass().getAnnotation(RMIServer.class);
            String port = ProjectContent.instance().parseValue(rmiServer.port());
            Remote stub = (Remote) UnicastRemoteObject.exportObject((Remote) rmiServerObj, Integer.valueOf(port));
            Registry registry = LocateRegistry.createRegistry(8087);
            registry.rebind(name, stub);
            running.put(name, (Remote) rmiServerObj);
            map.remove(name);
        } catch (RemoteException e) {
            throw new RMIException(e);
        }
    }

    /**
     * runAllOnStart
     * @throws RMIException RMIException
     */
    public void runAllOnStart() throws RMIException {
        try {
            RMIServer rmiServer;
            String runOnStart;
            String name;
            String port;
            List<Object> list = ObjectContainer.instance().getListObjectByAnnontation(RMIServer.class);
            for (Object rmiServerObj : list) {
                rmiServer = rmiServerObj.getClass().getAnnotation(RMIServer.class);
                runOnStart = ProjectContent.instance().parseValue(rmiServer.runOnStart());
                name = ProjectContent.instance().parseValue(rmiServer.name());
                if (isRunOnStart(runOnStart)) {
                    port = ProjectContent.instance().parseValue(rmiServer.port());
                    Remote stub = (Remote) UnicastRemoteObject.exportObject((Remote) rmiServerObj, Integer.valueOf(port));
                    Registry registry = LocateRegistry.createRegistry(8087);
                    registry.rebind(name, stub);
                    running.put(name, (Remote) rmiServerObj);
                } else {
                    map.put(name, (Remote) rmiServerObj);
                }
            }
        } catch (RemoteException e) {
            throw new RMIException(e);
        }
    }

    /**
     * isRunOnStart
     * @param runOnStart runOnStart
     * @return boolean
     */
    private boolean isRunOnStart(String runOnStart) {
        return StringUtils.isNotBlank(runOnStart) && Boolean.TRUE.toString().toLowerCase().equals(runOnStart.toLowerCase().trim());
    }

    /**
     * stopService
     * @param name name
     * @throws RMIException RMIException
     */
    public void stopService(String name) throws RMIException {
        try {
            UnicastRemoteObject.unexportObject(running.get(name), true);
            map.put(name, running.get(name));
            running.remove(name);
        } catch (NoSuchObjectException e) {
            throw new RMIException(e);
        }
    }
}

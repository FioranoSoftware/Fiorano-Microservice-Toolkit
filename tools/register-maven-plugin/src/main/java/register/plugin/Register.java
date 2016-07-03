package register.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fiorano.openesb.rmiconnector.api.IRmiManager;
import com.fiorano.openesb.rmiconnector.api.IServiceManager;
import com.fiorano.openesb.rmiconnector.client.FioranoRMIClientSocketFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "register", defaultPhase = LifecyclePhase.INSTALL)
public class Register extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-bin.zip", property = "fileName", required = true)
    private File fileName;
    @Parameter(defaultValue = "${project.artifactId}", property = "serviceName", required = true)
    private String serviceName;
    @Parameter(defaultValue = "4.0", property = "version", required = true)
    private String version;
    @Parameter(defaultValue = "false", property = "skipRegister", required = false)
    private boolean skipRegister;
    @Parameter(defaultValue = "localhost", property = "host", required = true)
    private String host;
    @Parameter(defaultValue = "2099", property = "port", required = true)
    private int port;
    @Parameter(defaultValue = "karaf", property = "username", required = true)
    private String username;
    @Parameter(defaultValue = "karaf", property = "password", required = true)
    private String password;

    public void execute() throws MojoExecutionException {
        if (!skipRegister) {
            try {
                getLog().info("Deploying " + fileName + " to server");
                List<String> servicesToImport = new ArrayList<String>();
                servicesToImport.add(serviceName + ":" + version);
                getLog().info("Services to import - " + servicesToImport);
                FioranoRMIClientSocketFactory csf = new FioranoRMIClientSocketFactory();
                Registry registry = LocateRegistry.getRegistry(host, port, csf);
                IRmiManager manager = (IRmiManager) registry.lookup("rmi");
                String handleId = manager.login(username, password);
                IServiceManager serviceManager = manager.getServiceManager(handleId);
                serviceManager.deployService(Files.readAllBytes(fileName.toPath()), true, true, true, servicesToImport);
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }
}

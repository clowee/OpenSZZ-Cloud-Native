package com.rest.szz.helpers;

import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;


/**
 * Class for monitoring whether there are or not docker containers for the project
 * 
 * @author LucaPellegrini
 *
 */
public class  DockerHelper {
	 final String localDockerHost = SystemUtils.IS_OS_WINDOWS ? "tcp://localhost:2375" : "unix:///var/run/docker.sock";
	 Logger l = Logger.getLogger(Logger.class);
	 final DefaultDockerClientConfig config = DefaultDockerClientConfig
	            .createDefaultConfigBuilder()
	            .withDockerHost(localDockerHost)
	            .build();
	
	 private String hostName = "";
	 private final DockerClient dc = DockerClientBuilder
	            .getInstance(config)
	            .build();
	 

	 /**
	  * DockerHelper class
	  * @param project
	  */
	public DockerHelper(){
		hostName = System.getenv("HOSTNAME");
	}
	
	/**
	 * It checks whether a docker container is already running for the project
	 * @return
	 */
	public String getPort(){
		InspectContainerResponse container = dc.inspectContainerCmd(hostName).exec();
		Binding b = container.getNetworkSettings().getPorts().getBindings().get(new ExposedPort(8080))[0];
		return b.getHostPortSpec();
	}
	
	

	
	
	
}

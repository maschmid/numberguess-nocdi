/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.numberguess;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.quickstarts.numberguess.Deployments;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class NumberGuessClusterTest {
   
    private static final String CONTAINER1 = "container1"; 
    private static final String CONTAINER2 = "container2"; 
    private static final String DEPLOYMENT1 = "dep.container1";
    private static final String DEPLOYMENT2 = "dep.container2"; 
   
    @ArquillianResource
    private ContainerController controller;
    
    @ArquillianResource
    private Deployer deployer;
   
    @Deployment(name = DEPLOYMENT1, managed=false)
    @TargetsContainer(CONTAINER1)
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }
    
    @Deployment(name = DEPLOYMENT2, managed=false)
    @TargetsContainer(CONTAINER2)
    public static WebArchive createTestDeployment2() {
        return Deployments.createDeployment();
    }
    
    @Test
    @RunAsClient
    public void test() {
        System.out.println("=== Before Containers Started ===");
        controller.start(CONTAINER1);
        controller.start(CONTAINER2);
        
        System.out.println("=== Containers Started ===");
        
        deployer.deploy(DEPLOYMENT1);
        deployer.deploy(DEPLOYMENT2);
        
        System.out.println("=== Deployments Done ===");
        
        controller.stop(CONTAINER1);
        controller.stop(CONTAINER2);  
        
        System.out.println("=== Containers Stopped ===");
    }
    
   /* @Test
    public void stopTest() throws Exception {
       System.out.println("=== Before Container 2 Started ===");
       controller.start(CONTAINER2); //, new Config().add("managementPort", "29999").map()
       System.out.println("===Container2 started===");
       controller.stop(CONTAINER2);
       System.out.println("===Container2 stopped===");
    }*/
    /*
    @Test
    public void killTest() throws Exception {
       controller.start(CONTAINER2, new Config().add("managementPort", "9999").map());
       System.out.println("===Container2 started again===");
       controller.kill(CONTAINER2);
       //this is now implemented the same way as stop -> softly
       System.out.println("===Container2 killed===");
    }*/
}

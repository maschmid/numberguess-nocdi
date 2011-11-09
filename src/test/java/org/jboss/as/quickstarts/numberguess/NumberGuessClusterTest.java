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

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.IdLocator;
import org.jboss.arquillian.ajocado.locator.XPathLocator;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.arquillian.ajocado.locator.LocatorFactory.id;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.xp;
import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 
 * @author mgencur
 * @author kpiwko
 * @author maschmid
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class NumberGuessClusterTest {
   
    protected String MAIN_PAGE = "home.jsf";
    
    private static final String CONTAINER1 = "container1"; 
    private static final String CONTAINER2 = "container2"; 
    private static final String DEPLOYMENT1 = "dep.container1";
    private static final String DEPLOYMENT2 = "dep.container2";
    
    protected IdLocator GUESS_MESSAGES = id("numberGuess:messages");
    protected XPathLocator GUESS_STATUS = xp("//div[contains(text(),'I'm thinking of ')]");
    
    protected IdLocator GUESS_FIELD = id("numberGuess:inputGuess");
    protected XPathLocator GUESS_FIELD_WITH_VALUE = xp("//input[@id='numberGuess:inputGuess'][@value=3]");

    protected IdLocator GUESS_SUBMIT = id("numberGuess:guessButton");
    protected IdLocator GUESS_RESTART = id("numberGuess:restartButton");
    protected IdLocator GUESS_SMALLEST = id("numberGuess:smallest");
    protected IdLocator GUESS_BIGGEST = id("numberGuess:biggest");

    protected String WIN_MSG = "Correct!";
    protected String LOSE_MSG = "No guesses left!";
    protected String HIGHER_MSG = "Higher!";
   
    @ArquillianResource
    private ContainerController controller;
    
    @ArquillianResource
    private Deployer deployer;
    
    @Drone
    AjaxSelenium selenium;
    
    String contextPath1;    
    String contextPath2;
   
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
    
    @Before
    public void before() throws MalformedURLException {
        // We can't use @ArquillianResource URL here as we are using unmanaged deployments
        contextPath1 = System.getProperty("node1.contextPath");
        contextPath2 = System.getProperty("node2.contextPath");
    }
    
  /*  @Test
    @RunAsClient
    public void test() throws MalformedURLException {
        System.out.println("=== Before Containers Started ===");
        controller.start(CONTAINER1);
        controller.start(CONTAINER2);
        
        System.out.println("=== Containers Started ===");
        
        deployer.deploy(DEPLOYMENT1);
        deployer.deploy(DEPLOYMENT2);
        
        System.out.println("=== Deployments Done ===");
                
        selenium.open(new URL(contextPath1 + "/" + MAIN_PAGE));
        
        Assert.assertTrue(selenium.isTextPresent("Guess a number..."));
        
        selenium.open(new URL(contextPath2 + "/" + MAIN_PAGE));
        
        Assert.assertTrue(selenium.isTextPresent("Guess a number..."));
        
        controller.stop(CONTAINER1);
        controller.stop(CONTAINER2);  
        
        System.out.println("=== Containers Stopped ===");
    }*/
    
    @Test
    @RunAsClient
    public void guessingWithFailoverTest() throws MalformedURLException, InterruptedException {
        System.out.println("=== Before Containers Started ===");
        controller.start(CONTAINER1);
        controller.start(CONTAINER2);
        
        System.out.println("=== Containers Started ===");
        
        deployer.deploy(DEPLOYMENT1);
        deployer.deploy(DEPLOYMENT2);
        
        selenium.open(new URL(contextPath1 + "/" + MAIN_PAGE));
   
        preFailurePart();

        String newAddress = getAddressForSecondInstance();

        // controller.stop(CONTAINER1);
        
        Thread.sleep(10000);

        selenium.open(new URL(contextPath2 + "/" + newAddress));
        
        Thread.sleep(5000);

        assertTrue("Page should contain message Higher!", selenium.isTextPresent(HIGHER_MSG));
        assertEquals("Page should contain smallest number equal to 4", 4, Integer.parseInt(selenium.getText(GUESS_SMALLEST)));
        assertEquals("Page should contain biggest number equal to 100", 100, Integer.parseInt(selenium.getText(GUESS_BIGGEST)));
        assertTrue("Page should contain input field with value of 3", selenium.isElementPresent(GUESS_FIELD_WITH_VALUE));

        postFailurePart();

        assertTrue("Win page expected after playing smart.", isOnWinPage());      
        
        //controller.stop(CONTAINER1);
        //controller.stop(CONTAINER2);  
   }
    
    protected void preFailurePart() {
        int numberOfGuesses = 3;
        int guess = 0;

        // enter several guesses (3)
        while (true) {
            while (isOnGuessPage() && guess < numberOfGuesses) {
                enterGuess(++guess);
            }

            // we always want to enter at least 3 guesses so that we can continue
            // in the other browser window with expected results
            if (guess < numberOfGuesses) {
                resetForm();
                guess = 0;
            } else {
                break;
            }
        }
    }
    
    protected void postFailurePart() {
        int min, max, guess;
        int i = 0;

        //selenium.deleteAllVisibleCookies();

        while (isOnGuessPage()) {
            //selenium.deleteAllVisibleCookies();
            /*
            * 3+8 = 11 -> even though we have 10 attempts, it is possible to enter
            * value 11 times, but the 11th time it is actually not guessing but
            * only validating that 10 times has gone and the game is finished (no
            * 11th guessing)
            */
            if (i >= 8) {
                fail("Game should not be longer than 7 guesses in the second selenium after failover");
            }

            assertTrue("Expected smallest number on page", selenium.isElementPresent(GUESS_SMALLEST));
            assertTrue("Expected biggest number on page", selenium.isElementPresent(GUESS_BIGGEST));

            min = Integer.parseInt(selenium.getText(GUESS_SMALLEST));
            max = Integer.parseInt(selenium.getText(GUESS_BIGGEST));
            guess = min + ((max - min) / 2);
            enterGuess(guess);
            i++;
        }
    }
    
    protected void resetForm() {
        waitForHttp(selenium).click(GUESS_RESTART);
    }

    protected void enterGuess(int guess) {
        selenium.type(GUESS_FIELD, String.valueOf(guess));
        waitForHttp(selenium).click(GUESS_SUBMIT);
    }
    
    protected boolean isOnGuessPage() {
        return !(isOnWinPage() || isOnLosePage());
    }

    protected boolean isOnWinPage() {
        String text = selenium.getText(GUESS_MESSAGES);
        return WIN_MSG.equals(text);
    }

    protected boolean isOnLosePage() {
        String text = selenium.getText(GUESS_MESSAGES);
        return LOSE_MSG.equals(text);
    }
    
    public String getAddressForSecondInstance() {
        String loc = selenium.getLocation().toString();
        String[] parsedStrings = loc.split("/");
        // localContextPath = "/" + parsedStrings[3] + "/";
        StringBuilder sb = new StringBuilder();
        for (int i = 4; i < parsedStrings.length; i++) {
            sb.append("/").append(parsedStrings[i]);
        }

        String newAddress = sb.toString();
        String firstPart = "";
        String sid = "";

        if (selenium.isCookiePresent("JSESSIONID")) {
            sid = selenium.getCookieByName("JSESSIONID").getValue();
            firstPart = newAddress;
        } else {
            // get sessionid directly from browser URL if JSESSIONID cookie is not
            // present
            firstPart = newAddress.substring(0, newAddress.indexOf(";"));
            sid = loc.substring(loc.indexOf("jsessionid=") + "jsessionid=".length(), loc.length());
        }

        newAddress = firstPart + ";jsessionid=" + sid;

        return newAddress;
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

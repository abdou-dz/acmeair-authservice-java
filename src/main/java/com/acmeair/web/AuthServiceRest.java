/*******************************************************************************
* Copyright (c) 2016 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.acmeair.web;

import com.acmeair.client.CustomerClient;
import com.acmeair.securityutils.SecurityUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/")
public class AuthServiceRest {
    
  private static final Logger logger =  Logger.getLogger(AuthServiceRest.class.getName());
        
  private static final String JWT_COOKIE_NAME = "jwt_token";
  private static final String USER_COOKIE_NAME = "loggedinuser";   
    
  private static final Boolean SECURE_USER_CALLS = 
          Boolean.valueOf((System.getenv("SECURE_USER_CALLS") == null) ? "true" 
                  : System.getenv("SECURE_USER_CALLS"));
    
  @Inject
  private CustomerClient customerClient;
    
  @Inject
  private SecurityUtils secUtils;
    
  static {
    System.out.println("SECURE_USER_CALLS: " + SECURE_USER_CALLS); 
  }
    
  /**
   * Login with username/password.
   */
  @POST
  @Consumes({"application/x-www-form-urlencoded"})
  @Produces("text/plain")
  @Path("/login")
  public Response login(@FormParam("login") String login, @FormParam("password") String password) {
    try {       
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("attempting to login : login " + login + " password " + password);
      }
            
      if (!validateCustomer(login,password)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    
      // Generate simple JWT with login as the Subject 
      String token = "";
      if (SECURE_USER_CALLS) { 
        token = secUtils.generateJwt(login);
      }
            
      return Response.ok("logged in")
              .header("Set-Cookie", JWT_COOKIE_NAME + "=" + token + "; Path=/")
              .header("Set-Cookie", USER_COOKIE_NAME + "=" + login + "; Path=/") .build();
      
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
            
  @GET
  public Response checkStatus() {
    return Response.ok("OK").build();
  }
    
  private boolean validateCustomer(String login, String password) {
    return customerClient.validateCustomer(login, password);
  }
}
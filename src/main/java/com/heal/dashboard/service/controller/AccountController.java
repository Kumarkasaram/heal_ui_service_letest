package com.heal.dashboard.service.controller;


import com.heal.dashboard.service.beans.*;
import com.heal.dashboard.service.businesslogic.*;
import com.heal.dashboard.service.exception.ClientException;
import com.heal.dashboard.service.exception.DataProcessingException;
import com.heal.dashboard.service.exception.ServerException;
import com.heal.dashboard.service.pojo.ResponseBean;
import com.heal.dashboard.service.util.JsonFileParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Api(value = "Accounts")
public class AccountController {

    @Autowired
    JsonFileParser headersParser;
    @Autowired
    GetAccountsBL getAccountsBL;
    @Autowired
    TopologyServiceBL topologyServiceBL;
    @Autowired
    DateComponentBL dateComponentBL;
    @Autowired
    MasterFeaturesBL masterFeaturesBL;

    @ApiOperation(value = "Retrieve accounts list", response = AccountBean.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public ResponseEntity<ResponseBean<List<AccountBean>>> getAccountList(@RequestHeader(value = "Authorization", required = false) String authorizationToken)
            throws ClientException, ServerException, DataProcessingException {

        UtilityBean<String> utilityBean = getAccountsBL.clientValidation(null, authorizationToken);
        UserAccessAccountsBean userAccessBean = getAccountsBL.serverValidation(utilityBean);
        List<AccountBean> accounts = getAccountsBL.process(userAccessBean);
        ResponseBean<List<AccountBean>> responseBean = new ResponseBean<>("Accounts fetching successfully", accounts, HttpStatus.OK);

        return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(responseBean);
    }

    @ApiOperation(value = "Retrieve account-wise topology details", response = TopologyDetails.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/accounts/{identifier}/topology", method = RequestMethod.GET)
    public ResponseEntity<ResponseBean<TopologyDetails>> getTopologyDetails(@RequestHeader(value = "Authorization") String
                                                                      authorizationToken, @PathVariable("identifier") String
                                                                      identifier, @RequestParam(value = "applicationId", required = false) String applicationId)
            throws ClientException, ServerException, DataProcessingException {

        UtilityBean<String> utilityBean = topologyServiceBL.clientValidation(null, authorizationToken, identifier, applicationId);
        TopologyValidationResponseBean topologyValidationResponseBean = topologyServiceBL.serverValidation(utilityBean);
        TopologyDetails topologyDetails = topologyServiceBL.process(topologyValidationResponseBean);
        ResponseBean<TopologyDetails> responseBean = new ResponseBean<>("Topology fetching successfully", topologyDetails, HttpStatus.OK);

        return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(responseBean);
    }

    @ApiOperation(value = "Retrieve features List", response = AccountBean.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/features", method = RequestMethod.GET)
    public ResponseEntity<List<MasterFeaturesBean>> getMasterFeatures(@RequestHeader(value = "Authorization") String authorizationToken)
            throws ClientException, DataProcessingException {
        masterFeaturesBL.clientValidation(null, authorizationToken);
        List<MasterFeaturesBean> response = masterFeaturesBL.process(null);
        return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(response);
    }

    @ApiOperation(value = "Retrieve date components List", response = AccountBean.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/date-components", method = RequestMethod.GET)
    public ResponseEntity<List<DateComponentBean>> getDateTimeDropdownList(@RequestHeader(value = "Authorization") String authorizationToken)
            throws ClientException, DataProcessingException {
        dateComponentBL.clientValidation(null, authorizationToken);
        List<DateComponentBean> response = dateComponentBL.process(null);
        return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(response);
    }
}

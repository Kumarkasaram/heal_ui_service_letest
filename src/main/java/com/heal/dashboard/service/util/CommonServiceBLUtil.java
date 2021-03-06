package com.heal.dashboard.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.heal.dashboard.service.beans.*;
import com.heal.dashboard.service.dao.mysql.ComponentInstanceDao;
import com.heal.dashboard.service.exception.ServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.heal.dashboard.service.dao.mysql.ControllerDao;
import com.heal.dashboard.service.dao.mysql.MasterDataDao;
import com.heal.dashboard.service.dao.mysql.TagsDao;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommonServiceBLUtil {
	
	@Autowired
	MasterDataDao masterDao;
	
	@Autowired
	ControllerDao controllerDao;
	@Autowired
	TagsDao tagDao;
	@Autowired
	ComponentInstanceDao componentInstanceDao;
	

	 public  Map<String,List<Controller>> getServiceApplicationMap(int accountId) {
	       	Map<String,List<Controller>> map = new HashMap();
	        try {
	            long start = System.currentTimeMillis();
	            List<Controller> apps = getControllersByType(Constants.APPLICATION_CONTROLLER_TYPE, accountId);
	            log.debug("Time taken to fetch applications list is {} ms.", (System.currentTimeMillis() - start));

	            @SuppressWarnings("unchecked")
				Map<String, Controller> appsMap = (Map<String, Controller>) apps.stream()
	                    .collect(Collectors.toMap(Controller::getAppId, app -> app));

	            start = System.currentTimeMillis();
	            List<TagMapping> allAccountTagList = tagDao.getTagMappingDetailsByAccountId(accountId);
	            log.debug("Time taken for fetching account details cache data is {} ms.", (System.currentTimeMillis() - start));

	            TagDetails controllerTagDetail = tagDao.getTagDetails(Constants.CONTROLLER_TAG,Constants.DEFAULT_ACCOUNT_ID);
	            if (controllerTagDetail == null) 
	            	return map;

	            List<TagMapping> tagMappingDetails = allAccountTagList.stream()
	                    .filter(tag -> tag.getObjectRefTable()
	                            .equalsIgnoreCase(Constants.CONTROLLER) && tag.getTagId() == controllerTagDetail.getId())
	                    .collect(Collectors.toList());

	            start = System.currentTimeMillis();
	            tagMappingDetails.forEach(tag -> {
	                String service = tag.getTagValue();
	                List<Controller> list = map.computeIfAbsent(service, k -> new ArrayList<>());
	                if (appsMap.get(tag.getObjectId() + "") != null) list.add(appsMap.get(tag.getObjectId() + ""));

	            });
	            log.debug("Time taken to map service to application is {} ms.", (System.currentTimeMillis() - start));


	        } catch (Exception e) {
	            log.error("Error in getting service application map");
	        }
	        return map;
	    }
	 
	  public  List<Controller> getControllersByType(String serviceType, int accountId) {
	        List<Controller> filtratedControllerList = new ArrayList<>();
	        try {
	            //get the service mst sub type
	        	  Optional<ViewTypeBean> subTypeOptional = masterDao.getAllViewTypes()       
	  	                .stream()
	  	                .filter(it -> (Constants.CONTROLLER_TYPE_NAME_DEFAULT.trim().equalsIgnoreCase(it.getTypeName())))
	  	                .filter(it -> (serviceType.trim().equalsIgnoreCase(it.getSubTypeName())))
	  	                .findAny();
	  	        if(subTypeOptional.isPresent()) {
	  	        ViewTypeBean subTypeBean = subTypeOptional.get();
	  	   		 List<Controller> controllerList = controllerDao.getControllerList(accountId);

	            //filter with controller_type_id
	            filtratedControllerList = controllerList.stream()
	                    .filter(t -> t.getControllerTypeId() == subTypeBean.getSubTypeId())
	                    .collect(Collectors.toList());
	  	        }
	  	        
	           
	        } catch (Exception e) {
	            log.error("Error occurred while fetching controller details for service name: " + serviceType + ", account id: " + accountId, e);
	        }
	        return filtratedControllerList;
	    }

	public AllAccountDetails loadAccountDetails(Integer accountId) throws ServerException {

		AllAccountDetails allAccountDetails = new AllAccountDetails();

		long start = System.currentTimeMillis();
		allAccountDetails.setTagMappingDetailsList(tagDao.getTagMappingDetailsByAccountId(accountId));
		log.debug("Time taken to fetch tag mapping for account is {} ms.",(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		allAccountDetails.setConnectionDetailsList(masterDao.getConnectionDetails(accountId));
		log.debug("Time taken for fetching connection details is {} ms.",(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		allAccountDetails.setCompInstanceDetailsList(componentInstanceDao.getCompInstClusterListByAccountId(accountId));
		log.debug("Time taken to fetch comp instance details is {} ms.",(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		//allAccountDetails.setComponentKpisList(masterDataService.getCompKpiMapping(accountId));
		log.debug("Time taken to fetch comp kpi details is {} ms.",(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		allAccountDetails.setTagDetailsBeanList(tagDao.getTagDetailsByAccountId(accountId));
		log.debug("Time taken to fetch tag meta details is {} ms.",(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		//allAccountDetails.setAllKpiLists(getAllKpi());
		log.debug("Time taken to fetch all kpi details is {} ms.",(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		//allAccountDetails.setClusterInstanceMappingList(MasterDataService.getClusterInstanceMapping(accountId));
		log.debug("Time taken to fetch cluster inst mapping details is {} ms.",(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		//allAccountDetails.setTxnAndGroupBeanList(TransactionDataService.getTxnAndGroupList(accountId));
		log.debug("Time taken to fetch txn details is {} ms.",(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		//allAccountDetails.setWindowProfileBeanList(MasterDataService.getWindowProfileList(accountId));
		log.debug("Time taken to fetch maintenance window details is {} ms.",(System.currentTimeMillis()-start));
		return allAccountDetails;
	}
}

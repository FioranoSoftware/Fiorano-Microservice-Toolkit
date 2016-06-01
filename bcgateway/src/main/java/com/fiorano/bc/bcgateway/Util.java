package com.fiorano.bc.bcgateway;

import com.fiorano.openesb.application.application.InputPortInstance;
import com.fiorano.openesb.application.application.OutputPortInstance;
import com.fiorano.openesb.application.application.PortInstance;

/**
 * Created by root on 14/4/16.
 */
public class Util {


    public static void copy(PortInstance source, fiorano.tifosi.dmi.application.PortInstance destination){


        destination.setEnabled(source.isEnabled());
        destination.setAppContextAction(source.getAppContextAction());
        destination.setAllowPaddingToKey(source.isAllowPaddingToKey());
        destination.setCalloutEnabled(source.isCalloutEnabled());
        destination.setClientID(source.getClientID());
        destination.seIstMessageFilterSet(source.getIsMessageFilterSet());
//            destination.setDbCallOutParameterList(List<DBCallOutParameter>source.getDbCallOutParameterList());
        destination.setDestination(source.getDestination());
        destination.setDestinationEncrypted(source.isDestinationEncrypted());
        destination.setDestinationConfigName(source.getDestinationConfigName());
        destination.setDestinationType(source.getDestinationType());
        destination.setDescription(source.getDescription());
        destination.setEncryptionAlgorithm(source.getEncryptionAlgorithm());
        destination.setEncryptionKey(source.getEncryptionKey());
        destination.setInitializationVector(source.getInitializationVector());
        destination.setMessageFilterConfigName(source.getMessageFilterConfigName());
        destination.setMessageFilters(source.getMessageFilters());
        destination.setName(source.getName());
        destination.setPassword(source.getPassword());
        destination.setProxyPassword(source.getProxyPassword());
        destination.setProxyURL(source.getProxyURL());
        destination.setProxyUsed(source.isProxyUsed());
        destination.setProxyUser(source.getProxyUser());
        destination.setRequestReply(source.isRequestReply());
        destination.setSecurityManager(source.getSecurityManager());
        destination.setSpecifiedDestinationUsed(source.isSpecifiedDestinationUsed());
//          destination.setSchema(source.getSchema());
        destination.setUser(source.getUser());
        destination.setWorkflow(source.getWorkflow());
        destination.setWorkflowConfigName(source.getWorkflowConfigName());
        destination.setWorkflowDataType(source.getWorkflowDataType());


    }


    public static void copy(InputPortInstance source, fiorano.tifosi.dmi.application.InputPortInstance destination){

        destination.setAcknowledgementMode(source.getAcknowledgementMode());
        destination.setDurableSubscription(source.isDurableSubscription());
        destination.setMessageSelector(source.getMessageSelector());
        destination.setSessionCount(source.getSessionCount());
        destination.setSubscriberConfigName(source.getSubscriberConfigName());
        destination.setSubscriptionName(source.getSubscriptionName());
        destination.setTransacted(source.isTransacted());
        destination.setTransactionSize(source.getTransactionSize());


        copy((PortInstance) source, (fiorano.tifosi.dmi.application.PortInstance) destination);


    }


    public static void copy(OutputPortInstance source, fiorano.tifosi.dmi.application.OutputPortInstance destination){

        destination.setCompressMessages(source.isCompressMessages());
        destination.setPersistent(source.isPersistent());
        destination.setPriority(source.getPriority());
        destination.setPublisherConfigName(source.getPublisherConfigName());
        destination.setTimeToLive(source.getTimeToLive());

        //@todo -- "
        //         destination.setApplicationContextTransformation(source.getApplicationContextTransformation());
        //


        copy((PortInstance)source, (fiorano.tifosi.dmi.application.PortInstance)destination);


    }



}


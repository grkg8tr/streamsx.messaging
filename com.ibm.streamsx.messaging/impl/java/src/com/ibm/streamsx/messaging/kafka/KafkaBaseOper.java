//
// *******************************************************************************
// * Copyright (C)2014, International Business Machines Corporation and *
// * others. All Rights Reserved. *
// *******************************************************************************
//
package com.ibm.streamsx.messaging.kafka;


import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.ibm.streams.operator.AbstractOperator;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streams.operator.model.Libraries;
import com.ibm.streams.operator.model.Parameter;

/**
 * Base operator for all the common functions
 *
 */
@Libraries({"opt/downloaded/*"})
public abstract class KafkaBaseOper extends AbstractOperator {
	
	protected Properties properties = new Properties(), finalProperties = new Properties();
	protected String propFile = null;
	protected AttributeHelper topicAH = new AttributeHelper("topic"),
							keyAH = new AttributeHelper("key"),
							messageAH =  new AttributeHelper("message");
	protected List<String> topics = new ArrayList<String>();
	protected KafkaClient client= null;
	private final Logger trace = Logger.getLogger(KafkaBaseOper.class.getCanonicalName());
	
	public void initialize(OperatorContext context) //, StreamSchema ss)
			throws Exception {
		super.initialize(context);
		
		if(propFile != null) {
			finalProperties.load(new FileReader(propFile));
		}
		finalProperties.putAll(properties);
		
		if(finalProperties == null || finalProperties.isEmpty())
			throw new Exception("Kafka connection properties must be specified.");
		
	}
	public void initSchema (StreamSchema ss ) throws Exception {
		trace.log(TraceLevel.INFO, "Connection properties: " + finalProperties);
		topicAH.initialize(ss, false);
		keyAH.initialize(ss, false);
		messageAH.initialize(ss, true);
		
		trace.log(TraceLevel.INFO, "Creating client");
		client = new KafkaClient(topicAH, keyAH, messageAH, finalProperties);
	}
			
	@Parameter(name="kafkaProperty", cardinality=-1, optional=true, 
			description="Specify a Kafka property \\\"key=value\\\" form. This will override any property specified in the properties file.")
	public void setKafkaProperty(List<String> values) {
		for(String value : values) {
			String [] arr = value.split("=");
			if(arr.length < 2) throw new IllegalArgumentException("Invalid property: " + value);
			String name = arr[0];
			String v = value.substring(arr[0].length()+1, value.length());
			properties.setProperty(name, v);
		}
	}
	
	@Parameter(name="propertiesFile", optional=true,
			description="Properties file containing kafka properties.")
	public void setPropertiesFile(String value) {
		this.propFile = value;
	}	
	
	@Parameter(optional=true, 
			description="Name of the attribute for the message. This attribute is required. Default is \\\"message\\\"")
	public void setMessageAttribute(String value) {
		messageAH.setName (value);
	}
	@Parameter(optional=true, description="Name of the attribute for the key. Default is \\\"key\\\"")
	public void setKeyAttribute(String value) {
		keyAH.setName (value);
	}
	
	@Override
	public void shutdown() {
		client.shutdown();
	}
}


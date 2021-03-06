/*
 * CINI, Consorzio Interuniversitario Nazionale per l'Informatica
 * Copyright 2013 CINI and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 
package eu.cloudtm.wpm.hw_probe;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import eu.cloudtm.resources.MonitorableResources;
import eu.cloudtm.wpm.consumer.AckConsumer;
import eu.reservoir.monitoring.core.AbstractProbe;
import eu.reservoir.monitoring.core.DefaultProbeAttribute;
import eu.reservoir.monitoring.core.DefaultProbeValue;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.ProducerMeasurement;
import eu.reservoir.monitoring.core.Rational;
import eu.reservoir.monitoring.core.TypeException;

/**
* @author Roberto Palmieri
* @author Sebastiano Peluso
*/
public class CpuResourceProbe extends AbstractProbe implements Probe {
	
	private final static Logger log = Logger.getLogger(CpuResourceProbe.class);
	private final static boolean INFO = log.isInfoEnabled();
	
	private CpuInfo monitored_cpu;
	//probe_timeout in millisecond
	public CpuResourceProbe(String name,int probe_timeout){
		setName(name);
		//Logical group of VM
		//ID gr_id = new ID(group_id);
		//setGroupID(gr_id);
		//Specified in measurements per hour
		int milliseconds_each_hour = 3600000;
		Rational probe_rate = new Rational(milliseconds_each_hour,1000);
		try{
			probe_rate = new Rational(milliseconds_each_hour, probe_timeout);
		}catch(Exception e){
			e.printStackTrace();
		}
        setDataRate(probe_rate);
        monitored_cpu = new CpuInfo();
        setProbeAttributes();
	}
	private void setProbeAttributes(){
		int attributeKey = 0;
		//Cpu Attributes
		for(int i=0;i<monitored_cpu.getNumberOfCpu();i++){
			addProbeAttribute(new DefaultProbeAttribute(attributeKey++, i+"-sys", ProbeAttributeType.DOUBLE, "%"));
			addProbeAttribute(new DefaultProbeAttribute(attributeKey++, i+"-user", ProbeAttributeType.DOUBLE, "%"));
			//addProbeAttribute(new DefaultProbeAttribute(attributeKey++, "cpu"+i+"-nice", ProbeAttributeType.DOUBLE, "percent"));
			//addProbeAttribute(new DefaultProbeAttribute(attributeKey++, "cpu"+i+"-idle", ProbeAttributeType.DOUBLE, "percent"));
		}
	}
	
	public ProbeMeasurement collect() {
		if(INFO)
			log.info("Start collecting at: "+System.currentTimeMillis());
		// list of proble values
		ArrayList<ProbeValue> list = new ArrayList<ProbeValue>();
		ArrayList<CpuValue> cpuValues = null;
		int attributeKey = 0;
		//collect data from cpu
		cpuValues = monitored_cpu.getCpuValues();
		for(CpuValue cpu : cpuValues){
			try {
				list.add(new DefaultProbeValue(attributeKey++, cpu.getSystem()));
				list.add(new DefaultProbeValue(attributeKey++, cpu.getUser()));
				//list.add(new DefaultProbeValue(attributeKey++, cpu.getNice()));
				//list.add(new DefaultProbeValue(attributeKey++, cpu.getIdle()));
			} catch (TypeException e) {
				e.printStackTrace();
			}
		}
		return new ProducerMeasurement(this, list, MonitorableResources.CPU.toString());	
	}
}
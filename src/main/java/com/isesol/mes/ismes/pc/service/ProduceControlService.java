package com.isesol.mes.ismes.pc.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.mes.ismes.pc.constant.CustomConstant;

public class ProduceControlService {

	public void query_bglsb (Parameters parameters,Bundle bundle){
		StringBuffer conditionSb = new StringBuffer(" 1= 1 ");
		List<Object> conditionValue = new ArrayList<Object>();
		String gdid = parameters.getString("gdid");
		if(StringUtils.isNotBlank(gdid)){
			conditionSb = conditionSb.append(" and gdid = ? ");
			conditionValue.add(gdid);
		}
		String sbid = parameters.getString("sbid");
		if(StringUtils.isNotBlank(sbid)){
			conditionSb = conditionSb.append(" and sbid = ? ");
			conditionValue.add(sbid);
		}
		String jgdyid = parameters.getString("jgdyid");
		if(StringUtils.isNotBlank(jgdyid)){
			conditionSb = conditionSb.append(" and jgdyid = ? ");
			conditionValue.add(jgdyid);
		}
		String yggh = parameters.getString("yggh");
		if(StringUtils.isNotBlank(yggh)){
			conditionSb = conditionSb.append(" and yggh = ? ");
			conditionValue.add(yggh);
		}
		if(parameters.get("bgrq") != null){
			Date bgrq = parameters.getDate("bgrq");
			conditionSb = conditionSb.append(" and bgrq = ? ");
			conditionValue.add(bgrq);
		}
		if(parameters.get("bgrq_start") != null){
			Date bgrq = parameters.getDate("bgrq_start");
			conditionSb = conditionSb.append(" and bgrq >= ? ");
			conditionValue.add(bgrq);
		}
		if(parameters.get("bgrq_end") != null){
			Date bgrq = parameters.getDate("bgrq_end");
			conditionSb = conditionSb.append(" and bgrq <= ? ");
			conditionValue.add(bgrq);
		}
		Dataset dataset = Sys.query("pc_bglsb", "bglsid,gdid,bgrq,bgsl,yggh,sbid", 
				conditionSb.toString(), "bgrq", conditionValue.toArray());
		bundle.put("bgls", dataset.getMap());
		bundle.put("bglsList", dataset.getList());
	}
	
	/**
	 * 触摸屏保存报工流水,设备ID暂时不用
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void save_bglsb(Parameters parameters, Bundle bundle) {
		String gdid = (String) parameters.get("gdid");
		//String sbid = (String) parameters.get("sbid");
		String jgdyid = (String) parameters.get("jgdyid");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(CustomConstant.报工流水表_工单ID, gdid);
		map.put(CustomConstant.报工流水表_报工日期, new Date());
		map.put(CustomConstant.报工流水表_报工时间, new Date());
		if(StringUtils.isNotBlank(parameters.getString("bgsl"))){
			map.put(CustomConstant.报工流水表_报工数量, Integer.parseInt(parameters.getString("bgsl")));
		}
		if(StringUtils.isNotBlank(parameters.getString("bfsl"))){
			map.put(CustomConstant.报工流水表_报废数量, Integer.parseInt(parameters.getString("bfsl")));
		}
		map.put(CustomConstant.报工流水表_员工工号, Sys.getUserIdentifier());
//		map.put(CustomConstant.报工流水表_设备ID, sbid);
		map.put("jgdyid", jgdyid);
		map.put("bgbj", "-1");
		int count = Sys.insert(CustomConstant.报工流水表, map);
		bundle.put("count", count);
	}
}

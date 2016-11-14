package com.isesol.mes.ismes.pc.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;

public class BglsService {
	/**
	 * 保存自动报工流水，盒子返回的数据，每完成一个会报一个数，数字会依次增加 1，2，3，4..
	 * 若机器有重启等相关动作，报的数字可能会重复 4，5，6...
	 * remark 用来记录盒子返回的数字
	 * 无论盒子如何返回数据，我们系统都应该把数据存储，而查询时，查remark不同的数据
	 */
	public void saveBgls(Parameters parameters, Bundle bundle) {
		String remark = parameters.getString("pc_remark");
		String gdid = parameters.getString("gdid");
		String sbid = parameters.getString("sbid");
		String jgdyid = parameters.getString("jgdyid");
		Dataset dataset = Sys.query("pc_bglsb", "gdid",
				" sbid = ? and gdid = ? and bgbj = ? ", null, new Object[] { sbid, gdid,remark});
		if(MapUtils.isEmpty(dataset.getMap())){
			bundle.put("nc_data_update_flag", true);
		}else{
			bundle.put("nc_data_update_flag", false);
		}
		
		Map<String, Object> bglsmap = new HashMap<String, Object>();
		bglsmap.put("gdid", gdid);
		bglsmap.put("bgrq", new Date());
		bglsmap.put("bgsj", new Date());
		bglsmap.put("bgsl", 1);
		bglsmap.put("sbid", sbid);
		bglsmap.put("jgdyid", jgdyid);
		bglsmap.put("bgbj", remark);
		int count = Sys.insert("pc_bglsb", bglsmap);
		bundle.put("count", count);
	}
	
	
	public void queryYbgsl(Parameters parameters, Bundle bundle) {
		int sbid = Integer.parseInt(parameters.get("sbid").toString());
		int gdid = Integer.parseInt(parameters.get("gdid").toString());
		int count = 0;
		Dataset dataset = Sys.query("pc_bglsb", "sum(bgsl) ybgsl",
				" sbid = ? and gdid = ? ", null, new Object[] { sbid, gdid});
		
		if (null != dataset && dataset.getList().size() > 0) {
			if(null == dataset.getList().get(0).get("ybgsl")){
				bundle.put("ybgsl", count);
			} else {
				bundle.put("ybgsl", Integer.parseInt(dataset.getList().get(0).get("ybgsl").toString()));
			}
		} else {
			bundle.put("ybgsl", count);
		}
	}
}

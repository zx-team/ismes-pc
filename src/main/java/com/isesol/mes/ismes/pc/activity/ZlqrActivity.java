package com.isesol.mes.ismes.pc.activity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;

import net.sf.json.JSONArray;

public class ZlqrActivity {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	DecimalFormat fnum = new DecimalFormat("##0.00"); 
	
	/**根据批次ID查询工单进度
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String query_gdjd(Parameters parameters, Bundle bundle) {
		String pcid= parameters.getString("pcid"); 
		parameters.set("gdztdm", " ( '20', '30', '40','50' ) ");//查询工单状态是已下发、加工中、质检中, 增加已完成状态的工单
		if(StringUtils.isBlank(pcid))
		{
			return "json:";
		}
		Bundle b_gdxx = Sys.callModuleService("pl", "plservice_gdxxfyByZtdm", parameters);
		if(null!=b_gdxx)
		{
			List<Map<String, Object>> gdjd = (List<Map<String, Object>>) b_gdxx.get("rows");
			for(int i=0;i<gdjd.size();i++){
				parameters.set("gxzid", gdjd.get(i).get("gxid"));
				Bundle b_gxzxx = Sys.callModuleService("pm", "queryGxzxxByGxid_new", parameters);
				Map<String, Object> gxzxx = (Map<String, Object>) b_gxzxx.get("gxxx");
				gdjd.get(i).put("gxmc", gxzxx.get("gxzmc"));
			}
			//计算生产任务百分比
			for (int i = 0; i < gdjd.size(); i++) {
				gdjd.get(i).put("wcjd",""+(Math.round((Integer.parseInt(gdjd.get(i).get("gdywcsl").toString())*10000)/Integer.parseInt(gdjd.get(i).get("jgsl").toString()))/100.0));
			}
			bundle.put("rows", gdjd);
			bundle.put("totalPage",  b_gdxx.get("totalPage"));
			bundle.put("currentPage",  b_gdxx.get("currentPage"));
			bundle.put("totalRecord",  b_gdxx.get("totalRecord"));
		}
		return "json:";
	}
	
	/**加工任务列表
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String table_jgrw(Parameters parameters, Bundle bundle) {
		//查询零件信息
		Bundle b_ljxx = Sys.callModuleService("pm", "pmservice_ljxxbybhmc", parameters);
		if (null==b_ljxx) {
			return "json:";
		}
		List<Map<String, Object>> ljxx = (List<Map<String, Object>>) b_ljxx.get("ljxx");
		if (ljxx.size()<=0) {
			return "json:";
		}
		String val_lj = "(";
		for (int i = 0; i < ljxx.size(); i++) {
			if(i!=0)
			{
				val_lj = val_lj +",";
			}
			val_lj += "'" +ljxx.get(i).get("ljid")+"'";
		} 
		val_lj = val_lj +")";
		parameters.set("val_lj", val_lj);
		
		//查询状态为执行中的生产任务 
		parameters.set("query_rwzt", "20");
		parameters.set("sortName", "scrwlrsj");
		parameters.set("sortOrder", "desc");
		Bundle b_scrw = Sys.callModuleService("pro", "proService_scrw", parameters);
		List<Map<String, Object>> scrw = (List<Map<String, Object>>) b_scrw.get("rows");
		bundle.put("totalPage", b_scrw.get("totalPage"));
		bundle.put("currentPage",b_scrw.get("currentPage"));
		bundle.put("totalRecord", b_scrw.get("totalRecord"));
		if (scrw.size()<=0) {
			return "json:";
		}
		String val_scrw = "(";
		for (int i = 0; i < scrw.size(); i++) {
			scrw.get(i).put("scrwzt", scrwzt(scrw.get(i).get("scrwztdm")));
			for (int j = 0; j < ljxx.size(); j++) {
				if (scrw.get(i).get("ljid").toString().equals(ljxx.get(j).get("ljid").toString())) {
					scrw.get(i).put("ljbh", ljxx.get(j).get("ljbh"));
					scrw.get(i).put("ljmc", ljxx.get(j).get("ljmc"));
					break;
				}
			}
			String scrwid = scrw.get(i).get("scrwid") + "";
			parameters.set("scrwid", scrwid);
			Bundle ydclxx_bundle = Sys.callModuleService("pl", 
					"plservice_query_EquivalentYieldByScrw", parameters);
			
			if(ydclxx_bundle == null){
				scrw.get(i).put("ywcsl", 0);
				scrw.get(i).put("wxwcsl", 0);
				scrw.get(i).put("wcjd", 0.0);
				scrw.get(i).put("wxwcjd", 0.0);
				continue;
			}
			
			Map<String, Object> ydcl_map = (Map<String, Object>)ydclxx_bundle.get("map");
			
			scrw.get(i).put("ywcsl",ydcl_map.get("scrw_ydclsl"));
			scrw.get(i).put("wcjd", new BigDecimal(ydcl_map.get("scrw_ydcljd") + "").
					multiply(new BigDecimal(100)));
			scrw.get(i).put("wxwcsl", 0);
			scrw.get(i).put("wxwcjd", 0.0);
			
			//初始化完成数量
//			scrw.get(i).put("ywcsl", 0);
//			scrw.get(i).put("wxwcsl", 0);
//			scrw.get(i).put("wcjd", 0.0);
//			scrw.get(i).put("wxwcjd", 0.0);
//			
//			if(i!=0)
//			{
//				val_scrw = val_scrw +",";
//			}
//			val_scrw += scrw.get(i).get("scrwid");
//		}
//		val_scrw = val_scrw +")";
//		parameters.set("val_scrw", val_scrw);
//		
//		//根据生产任务ID，查询生产任务批次信息
//		Bundle b_pcxx = Sys.callModuleService("pro", "proService_pcxx", parameters);
//		List<Map<String, Object>> pcxx = (List<Map<String, Object>>) b_pcxx.get("pcxx");
//		String val_pc = "";
//		for(int i = 0, len = pcxx.size(); i < len; i++){
//			if(i==0)
//			{
//				val_pc += "(";
//			}
//			val_pc += "'"+pcxx.get(i).get("scrwpcid")+"'";
//			if(i < (len - 1)){
//				val_pc += ",";
//			} else{
//				val_pc += ")";
//			}
//			pcxx.get(i).put("ywcsl", 0);
//		}
//		parameters.set("val_pc", val_pc.toString());
//		
//		Bundle b_gdxx = Sys.callModuleService("pl", "plservice_gdjgjd", parameters);
//		if(null!=b_gdxx)
//		{
//			List<Map<String, Object>> pcgdxx = (List<Map<String, Object>>) b_gdxx.get("pcgdxx");
//			
//			for (int i = 0; i < pcxx.size(); i++) {
//				for (int j = 0; j < pcgdxx.size(); j++) {
//					if (pcxx.get(i).get("scrwpcid").toString().equals(pcgdxx.get(j).get("pcid").toString())) {
//						pcxx.get(i).put("ywcsl",Float.parseFloat(pcgdxx.get(j).get("gdywcsl").toString()));
//					}
//				}
//			}
//
//			//计算生产任务已完成数量
//			for (int i = 0; i < scrw.size(); i++) {
//				for (int j = 0; j < pcxx.size(); j++) {
//					if (scrw.get(i).get("scrwid").toString().equals(pcxx.get(j).get("scrwid").toString())) {
//						scrw.get(i).put("ywcsl",Float.parseFloat(scrw.get(i).get("ywcsl").toString())+ Float.parseFloat(pcxx.get(j).get("ywcsl").toString()));
//					}
//				}
//			}
//			//计算生产任务百分比
//			for (int i = 0; i < scrw.size(); i++) {
//				scrw.get(i).put("wcjd",""+(Math.round((Float.parseFloat(scrw.get(i).get("ywcsl").toString())*10000)/Float.parseFloat(scrw.get(i).get("jgsl").toString()))/100.0));
//			}
		}
		bundle.put("rows", scrw);
		return "json:";
	}
	
	/**工单约当产量的计算,递归
	 * @param parameters
	 * @param bundle
	 */
	public Map<String, Object> gx_ydcl(List<Map<String, Object>> gxxx ,Map<String, Object> gxMap) {
		Map<String, Object> gxxxMap = new HashMap<String, Object>();
		gxxxMap.put("ydcl", 0);//约当产量
		gxxxMap.put("wxbz", "1");//尾序标识
		for (int i = 0; i < gxxx.size(); i++) {
			if(gxMap.get("gxid").equals(gxxx.get(i).get("qxid")))
			{
				
				gxxx.get(i).put("qxjgsj", Integer.parseInt(gxxx.get(i).get("jgfs").toString()) + Integer.parseInt(gxMap.get("qxjgsj").toString()));
				gxxxMap = gx_ydcl(gxxx,gxxx.get(i));   
				
				if("1".equals(gxxxMap.get("wxbz")))
				{
					gxxxMap.put("wxwcsl", gxxx.get(i).get("wcsl"));
					gxxx.get(i).put("zzpsl", Integer.parseInt(gxMap.get("wcsl").toString()));
				}else{
					gxxxMap.put("wxwcsl", gxxxMap.get("wxwcsl"));
					gxxx.get(i).put("zzpsl", Integer.parseInt(gxMap.get("wcsl").toString()) - Integer.parseInt(gxxx.get(i).get("wcsl").toString()));
				}
				if(0!=Integer.parseInt(gxxx.get(i).get("jgsl").toString()))
				{
					float zzpsl = Float.parseFloat(gxxx.get(i).get("zzpsl").toString());
					float wcsl = Float.parseFloat(gxxx.get(i).get("wcsl").toString());
					float jgsl = Float.parseFloat(gxxx.get(i).get("jgsl").toString());
					float jgfs = Float.parseFloat(gxxx.get(i).get("jgfs").toString());
					float zjgsj = Float.parseFloat(gxxx.get(i).get("zjgsj").toString());
					float qxjgsj = Float.parseFloat(gxMap.get("qxjgsj").toString());
					gxxxMap.put("ydcl", Float.parseFloat(gxxxMap.get("ydcl").toString())+(zzpsl*((wcsl*100/jgsl)*jgfs+qxjgsj*100)/zjgsj)/100.0);
				}
				gxxxMap.put("wxbz", "0");
				break;
			}
		}
		
		return gxxxMap;
	}
	
	/**加工任务批次
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String table_jgrwpcxx(Parameters parameters, Bundle bundle) {
		String val_scrw = parameters.getString("parentRowid");
		if (StringUtils.isBlank(val_scrw)) {
			return "json:";
		}
		parameters.set("val_scrw", "('"+val_scrw + "')"); 
		//根据生产任务ID，查询生产任务批次信息
		Bundle b_pcxx = Sys.callModuleService("pro", "proService_pcxxfy", parameters);
		if (null==b_pcxx) {
			return "json:";
		} 
		List<Map<String, Object>> pcxx = (List<Map<String, Object>>) b_pcxx.get("pcxx");
		if (null==pcxx||pcxx.size()<=0){
			return "json:";
		}
		bundle.put("totalPage", b_pcxx.get("totalPage"));
		bundle.put("currentPage",b_pcxx.get("currentPage"));
		bundle.put("totalRecord", b_pcxx.get("totalRecord"));
		
		String val_pc = "";
		for (int i = 0, len = pcxx.size(); i < len; i++) {
			pcxx.get(i).put("pcjhzt", pcjhzt(pcxx.get(i).get("pcjhztdm")));
			parameters.set("pcid", pcxx.get(i).get("scrwpcid"));
			Bundle ydcl_bundle = Sys.callModuleService("pl", "plservice_query_EquivalentYieldByPc", parameters);
			
			Map<String, Object> ydcl_map = (Map<String, Object>)ydcl_bundle.get("map");
			pcxx.get(i).put("wxwcsl", ydcl_map.get("wcsl"));
			pcxx.get(i).put("ywcsl", ydcl_map.get("pc_ydclsl"));
			pcxx.get(i).put("wcjd", new BigDecimal(ydcl_map.get("pc_ydcljd") + "").
					multiply(new BigDecimal(100)));
		}
		
//		parameters.set("val_pc", val_pc.toString());
//		Bundle b_gdxx = Sys.callModuleService("pl", "plservice_gdjgjd", parameters);
//		if (null != b_gdxx) {
//			List<Map<String, Object>> pcgdxx = (List<Map<String, Object>>) b_gdxx.get("pcgdxx");
//			for (int i = 0; i < pcxx.size(); i++) {
//				for (int j = 0; j < pcgdxx.size(); j++) {
//					if (pcxx.get(i).get("scrwpcid").toString().equals(pcgdxx.get(j).get("pcid").toString())) {
//						pcxx.get(i).put("ywcsl",Float.parseFloat(pcgdxx.get(j).get("gdywcsl").toString()));
//						pcxx.get(i).put("wcjd",""+(Math.round((Float.parseFloat(pcxx.get(i).get("ywcsl").toString())*10000)/Float.parseFloat(pcxx.get(i).get("pcsl").toString()))/100.0));
//					}
//				}
//			}
//		}
		bundle.put("rows",pcxx);
		return "json:";
	}
	
	/**跳转质量确认界面
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String query_zlqr(Parameters parameters, Bundle bundle) {
		return "pc_zlqr";
	}
	
	/**保存报工数量
	 * @param parameters
	 * @param bundle
	 */
	public void save_bgsl(Parameters parameters, Bundle bundle) {
		// 保存报完工数量
		Sys.callModuleService("pl", "plservice_save_bgsl", parameters);
		
		// 获取工单ID
		String data_list = parameters.getString("data_list");
		JSONArray data_jsonarray = JSONArray.fromObject(data_list);
		String gdid = ((Map<String,Object>)data_jsonarray.get(0)).get("gdid").toString();
		List<String> gdidList = new ArrayList<String>();
		for(int i = 0; i < data_jsonarray.size(); i++){
			Map<String, Object> m = data_jsonarray.getJSONObject(i);
			if(m.get("gdid") != null){
				gdidList.add(m.get("gdid").toString());
			}
		}
		parameters.set("gdidList", gdidList);
		// 获取工单信息
		Bundle b_gdxx = Sys.callModuleService("pl", "plservice_gdxxByGdID", parameters);
		Map<Object,Map<String,Object>> rwpcxx = new HashMap<Object,Map<String,Object>>();
		// 进行业务处理
		if (null != b_gdxx) {
			List<Map<String, Object>> gdxx = (List<Map<String, Object>>) b_gdxx.get("gdxx");
			if (!CollectionUtils.isEmpty(gdxx)){
				for(Map<String, Object> m : gdxx){
					Integer jhjgsl = (Integer) m.get("jgsl");
					String gdywcsl = String.valueOf(m.get("gdywcsl"));
					String tssyx = (String) m.get("tssyx");
					String bfgf = (String) m.get("bfgf");
					String bflf = (String) m.get("bflf");
					String pcid = m.get("pcid").toString();
					parameters.set("gdid", m.get("gdid"));
					parameters.set("gdywcsl", m.get("gdywcsl"));
					parameters.set("tssyx", tssyx==null?0:tssyx);
					parameters.set("bfgf", bfgf==null?0:bfgf);
					parameters.set("bflf", bflf==null?0:bflf);
					
					// 判断报完工数量是否大于计划加工数量，大于时更新工单状态为已完成状态，小于时更新工单状态为加工中状态
					if (jhjgsl <=Integer.parseInt(gdywcsl)) {
						// 更新工单状态为已完成状态
						parameters.set("gdzt", "50");
						parameters.set("gdwcsj", new Date()); //工单状态完成时，更新工单完成时间。add by yangfan 2016/8/29
						Sys.callModuleService("pl", "plservice_updategdxxByGdID", parameters);
						//判断生产任务下所有工单状态
						boolean gd_state = check_gd_state(pcid, parameters);
						if(gd_state){
							//更新批次状态为加工完成（80）
							parameters.set("scrwpcid", pcid);
							parameters.set("pcjhztdm", "80");
							Sys.callModuleService("pro", "proService_updateScrwpczt", parameters);
							
//							// 获取生产任务ID
//							parameters.set("scrwpcid", pcid);
//							Bundle b_scrwxx = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
//							if (null == b_scrwxx) {
//								continue;
//							}
//							String scrwid = ((Map<String, Object>)b_scrwxx.get("scrwandpc")).get("scrwid").toString();
//							//判断生产任务下所有批次状态
//							boolean pc_state = check_pc_state(scrwid, parameters);
//							if(pc_state){
//								//更新生产任务状态为加工完成（30）
//								parameters.set("scrwid", scrwid);
//								parameters.set("scrwztdm", "30");
//								Sys.callModuleService("pro", "proService_updateScrwzt", parameters);
//							}
						}
					}else if(Integer.parseInt(gdywcsl) < jhjgsl && Integer.parseInt(gdywcsl) > 0 ){
						// 更新工单状态为加工中状态
						parameters.set("gdzt", "30");
						Sys.callModuleService("pl", "plservice_updategdxxByGdID", parameters);
						//更新批次状态为加工中（70）
						parameters.set("scrwpcid", pcid);
						parameters.set("pcjhztdm", "70");
						Sys.callModuleService("pro", "proService_updateScrwpczt", parameters);
						
//						// 获取生产任务ID
//						parameters.set("scrwpcid", pcid);
//						Bundle b_scrwxx = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
//						if (null == b_scrwxx) {
//							continue;
//						}
//						String scrwid = ((Map<String, Object>)b_scrwxx.get("scrwandpc")).get("scrwid").toString();
//						//更新生产任务状态为执行中（20）
//						parameters.set("scrwid", scrwid);
//						parameters.set("scrwztdm", "20");
//						Sys.callModuleService("pro", "proService_updateScrwzt", parameters);
					}
					//update by duanpeng for add scrw and pcxx activity
					parameters.set("scrwpcid", pcid);
					Bundle activity_scrwxx = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
					if (null == activity_scrwxx) {
						continue;
					}
					rwpcxx.put(pcid, (Map<String, Object>)activity_scrwxx.get("scrwandpc"));
				}
			}
		} 
		
		// update by duanpeng for add activity
		// 工单信息
		List<Map<String, Object>> gdxx = (List<Map<String, Object>>) b_gdxx.get("gdxx");
		// 查询零件信息
		StringBuffer ljids = new StringBuffer();
		for (Map<String, Object> gd : gdxx) {
			ljids.append(gd.get("ljid")).append(",");
		}
		ljids.append("-1");
		parameters.set("val_lj", "(" + ljids + ")");
		Bundle result = Sys.callModuleService("pm", "pmservice_ljxx", parameters);
		List<Map<String, Object>> ljxx = (List<Map<String, Object>>) result.get("ljxx");
		//caculate pcjd and rwjd
		Object pcjd = null;// 批次进度
		Object scrwjd = null;// 生产任务进度
		for (Map<String, Object> gd : gdxx) {
			Object gdbh = gd.get("gdbh");// 工单编号
			Object pcid = Integer.valueOf(gd.get("pcid").toString());// 批次id
			Object ljmc = null;
			Object ljbh = null;
			if (ljxx != null && ljxx.size() > 0) {
				for (Map<String, Object> lj : ljxx) {
					if (gd.get("ljid") != null && gd.get("ljid").toString().equals(gd.get("ljid").toString())) {
						ljmc = ljxx.get(0).get("ljmc");// 零件名称
						ljbh = ljxx.get(0).get("ljbh");// 零件编号
						break;
					}
				}
			}
			// 获取生产任务及批次信息
			Map<String, Object> pcxx = rwpcxx.get(pcid.toString());
			Object scrwbh = pcxx.get("scrwbh");// 生产编号
			Object pcbh = pcxx.get("pcbh");// 批次编号
			Object pcmc = pcxx.get("pcmc");// 批次编号
			Object jgsl = gd.get("jgsl");// 加工数量
			Object bcwcsl = null;// 本次完工数量
			for (int i = 0; i < data_jsonarray.size(); i++) {
				Map<String, Object> m = data_jsonarray.getJSONObject(i);
				if (m.get("gdywcsl") != null && gd.get("gdid").toString().equals(m.get("gdid").toString())) {
					bcwcsl = m.get("gdywcsl");// 本次完工数量
					break;
				}
			}
			
			Object jhkssj = gd.get("jhkssj");//计划开始时间
			Object jhjssj = gd.get("jhjssj");//计划结束时间
			long jhkssjStr = 0;
			long jhjssjStr = 0;
			if(jhkssj!=null){
				jhkssjStr = ((Date)jhkssj).getTime();
			}
			if(jhjssj!=null){
				jhjssjStr = ((Date)jhjssj).getTime();
			}
			Object gxid = gd.get("gxid");//工序id
			Object sbid = gd.get("sbid");//设备id
			Parameters gxCond = new Parameters();
//			gxCond.set("gxid", gxid);
//			Bundle gxxx = Sys.callModuleService("pm", "queryGxxxByGxid", gxCond);
//			Object gxmc = ((Map)gxxx.get("gxxx")).get("gxmc");
			gxCond.set("gxzid", gxid);
			Bundle gxxx = Sys.callModuleService("pm", "queryGxzxxByGxid_new", gxCond);
			Object gxmc = ((Map)gxxx.get("gxxx")).get("gxzmc");
			Parameters sbCond = new Parameters();
			sbCond.set("sbid", sbid);
			Bundle sbxx = Sys.callModuleService("em", "emservice_jgdyInfo", sbCond);
//			Object sbbh = ((Map)sbxx.get("sbxxList")).get("sbbh");
			List<Map> sbxxlist = (List<Map>)sbxx.get("sbxxList");
			String sbbh = (String) sbxxlist.get(0).get("sbbh");
			
			
			if(pcjd == null && scrwjd == null){
				Parameters jdCondition = new Parameters();
				jdCondition.set("scrwbh", scrwbh);
				jdCondition.set("pcbh", pcbh);
				caculateProgress(jdCondition,bundle);
				scrwjd = bundle.get("scrwjd");
				pcjd = bundle.get("pcjd");
			}
			Object gdjd = null;// 工单进度
			if(jgsl != null && bcwcsl != null){
				gdjd = Double.valueOf(bcwcsl.toString()) / Double.valueOf(jgsl.toString())  * 100;
			}else{
				gdjd = 0;
			}
			//查询零件图片
			Parameters ljparams = new Parameters();
			ljparams.set("ljid", gd.get("ljid"));
			Bundle resultLjUrl = Sys.callModuleService("pm", "partsInfoService", ljparams);
			Object ljtp = ((Map)resultLjUrl.get("partsInfo")).get("url");
			String activityType = "0"; // 生产任务
			String[] roles = new String[] { "PLAN_MANAGEMENT_ROLE", "MANUFACTURING_MANAGEMENT_ROLE" };// 关注该动态的角色
			String templateId = "bwg_tp";// 报完工动态模板
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("pcjd", pcjd);// 批次进度
			data.put("gdjd", gdjd);// 工单进度
			data.put("scrwjd", scrwjd);// 生产任务进度
			data.put("gdbh", gdbh);// 工单编号
			data.put("pcid", pcid);// 批次ID
			data.put("ljmc", ljmc);// 零件名称
			data.put("ljbh", ljbh);// 零件编号
			data.put("scrwbh", scrwbh);// 生产任务编号
			data.put("pcbh", pcbh);// 批次编号 
			data.put("pcmc", pcmc);// 批次编号 
			data.put("jgsl", jgsl);// 加工数量
			data.put("bcwcsl", bcwcsl);// 本次完成数量
			data.put("userid", Sys.getUserIdentifier());// 操作人id
			data.put("username", Sys.getUserName());// 操作人姓名
			data.put("jhkssj", jhkssjStr);//计划开始时间
			data.put("jhjssj", jhjssjStr);//计划结束时间
			data.put("gxmc", gxmc);//工序名称
			data.put("sbbh", sbbh);//设备编号
			data.put("ljtp", ljtp);//操作人
			sendActivity(activityType, templateId, true, roles, null, null, data);
		}
	}
	private Bundle sendActivity(String type, String templateId, boolean isPublic, String[] roles, String[] users,
			String[] group, Map<String, Object> data) {
		String PARAMS_TYPE = "type";
		String PARAMS_TEMPLATE_ID = "template_id";
		String PARAMS_PUBLIC = "public";
		String PARAMS_ROLE = "role";
		String PARAMS_USER = "user";
		String PARAMS_GROUP = "group";
		String PARAMS_DATA = "data";
		String SERVICE_NAME = "activity";
		String METHOD_NAME = "send";
		Parameters parameters = new Parameters();
		parameters.set(PARAMS_TYPE, type);
		parameters.set(PARAMS_TEMPLATE_ID, templateId);
		if (isPublic)
			parameters.set(PARAMS_PUBLIC, "1");
		if (roles != null && roles.length > 0)
			parameters.set(PARAMS_ROLE, roles);
		if (users != null && users.length > 0)
			parameters.set(PARAMS_USER, users);
		if (group != null && group.length > 0)
			parameters.set(PARAMS_GROUP, group);
		if (data != null && !data.isEmpty())
			parameters.set(PARAMS_DATA, data);
		return Sys.callModuleService(SERVICE_NAME, METHOD_NAME, parameters);
	}
	
	@SuppressWarnings("unchecked")
	public void caculateProgress(Parameters parameters, Bundle bundle) {

		String pcbh = parameters.getString("pcbh");
//		String ljbh = parameters.getString("query_ljbh");
		parameters.set("page", 1);
		parameters.set("pageSize", 10);
		//查询生产任务
		Bundle b_scrw = Sys.callModuleService("pro", "proService_scrw", parameters);
		List<Map<String, Object>> scrw = (List<Map<String, Object>>) b_scrw.get("rows");
		if (scrw.size()<=0) {
			return;
		}
		String val_scrw = "(";
		for (int i = 0; i < scrw.size(); i++) {
			//初始化完成数量
			scrw.get(i).put("ywcsl", 0);
			scrw.get(i).put("wxwcsl", 0);
			scrw.get(i).put("wcjd", 0.0);
			scrw.get(i).put("wxwcjd", 0.0);
			if(i!=0){
				val_scrw = val_scrw +",";
			}
			val_scrw += scrw.get(i).get("scrwid");
		}
		val_scrw = val_scrw +")";
		parameters.set("val_scrw", val_scrw);
		
		//根据生产任务ID，查询生产任务批次信息
		Bundle b_pcxx = Sys.callModuleService("pro", "proService_pcxx", parameters);
		List<Map<String, Object>> pcxx = (List<Map<String, Object>>) b_pcxx.get("pcxx");
		String val_pc = "";
		for(int i = 0, len = pcxx.size(); i < len; i++){
			if(i==0)
			{
				val_pc += "(";
			}
			val_pc += "'"+pcxx.get(i).get("scrwpcid")+"'";
			if(i < (len - 1)){
				val_pc += ",";
			} else{
				val_pc += ")";
			}
			pcxx.get(i).put("ywcsl", 0);
			pcxx.get(i).put("pcjd", 0);
		}
		parameters.set("val_pc", val_pc.toString());
		
		Bundle b_gdxx = Sys.callModuleService("pl", "plservice_gdjgjd", parameters);
		Object pcjd = null;
		if(null!=b_gdxx)
		{
			List<Map<String, Object>> pcgdxx = (List<Map<String, Object>>) b_gdxx.get("pcgdxx");
			
			for (int i = 0; i < pcxx.size(); i++) {
				for (int j = 0; j < pcgdxx.size(); j++) {
					if (pcxx.get(i).get("scrwpcid").toString().equals(pcgdxx.get(j).get("pcid").toString())) {
						pcxx.get(i).put("ywcsl",Float.parseFloat(pcgdxx.get(j).get("gdywcsl").toString()));
						pcxx.get(i).put("wcjd",""+(Math.round((Float.parseFloat(pcxx.get(i).get("ywcsl").toString())*10000)/Float.parseFloat(pcxx.get(i).get("pcsl").toString()))/100.0));
					}
					if(StringUtils.isNotEmpty(pcbh) && pcxx.get(i).get("pcbh") != null && pcxx.get(i).get("pcbh").equals(pcbh)){
						pcjd = pcxx.get(i).get("wcjd");
					}
				}
			}

			//计算生产任务已完成数量
			for (int i = 0; i < scrw.size(); i++) {
				for (int j = 0; j < pcxx.size(); j++) {
					if (scrw.get(i).get("scrwid").toString().equals(pcxx.get(j).get("scrwid").toString())) {
						scrw.get(i).put("ywcsl",Float.parseFloat(scrw.get(i).get("ywcsl").toString())+ Float.parseFloat(pcxx.get(j).get("ywcsl").toString()));
					}
				}
			}
			//计算生产任务百分比
			for (int i = 0; i < scrw.size(); i++) {
				scrw.get(i).put("wcjd",""+(Math.round((Float.parseFloat(scrw.get(i).get("ywcsl").toString())*10000)/Float.parseFloat(scrw.get(i).get("jgsl").toString()))/100.0));
			}
		}
		if(pcjd == null) pcjd = "0";
		bundle.put("scrwjd", scrw.get(0).get("wcjd"));
		bundle.put("pcjd", pcjd);
		bundle.put("pcxx", pcxx);
	}
	/**申请质检
	 * @param parameters
	 * @param bundle
	 */
	public void save_sqzj(Parameters parameters, Bundle bundle) {
		Sys.callModuleService("pl", "plservice_save_sqzj", parameters);
	}
	
	/**判定指定批次下所有工单是否都已完成
	 * @param parameters
	 * @param bundle
	 */
	private boolean check_gd_state(String pcid, Parameters parameters){
		String pc_id = "(" + pcid + ")";
		parameters.set("val_pc", pc_id);
		Bundle dg_list = Sys.callModuleService("pl", "plservice_gdxxByPcid", parameters);
		boolean close_flag = true;
		List<Map<String, Object>> gdxx = (List<Map<String, Object>>) dg_list.get("gdxx");
		
		//判断是否全部工单都已加工完成 
		//全部加工完成 close_flag=true;
		//未全部加工完成 close_flag=false;
		for(int i = 0; i< gdxx.size(); i++){
			String gdzt = gdxx.get(i).get("gdztdm").toString();
			//判断工单状态是否为加工完成状态（50）
			if (!gdzt.equals("50")){
				close_flag = false;
			}
		}
		return close_flag;
	}
	
	/**判定指定生产任务下所有批次是否都已完成
	 * @param parameters
	 * @param bundle
	 */
	private boolean check_pc_state(String scrwid, Parameters parameters){
		String scrw_id = "(" + scrwid + ")";
		parameters.set("val_scrw", scrw_id);
		Bundle pc_list = Sys.callModuleService("pro", "proService_pcxx", parameters);
		boolean close_flag = true;
		List<Map<String, Object>> pcxx = (List<Map<String, Object>>) pc_list.get("pcxx");
		
		//判断是否全部工单都已加工完成 
		//全部加工完成 close_flag=true;
		//未全部加工完成 close_flag=false;
		for(int i = 0; i< pcxx.size(); i++){
			String pczt = pcxx.get(i).get("pcjhztdm").toString();
			//判断批次状态是否为加工完成状态（80）
			if (!pczt.equals("80")){
				close_flag = false;
			}
		}
		return close_flag;
	}
	
	/**
	 * 根据生产任务状态代码获取状态名称
	 * @param dm
	 * @return
	 */
	private String scrwzt(Object dm) {
		String name = "";
		if (dm != null) {
			switch (Integer.parseInt(String.valueOf(dm))) {
			case 10:
				name = "未执行";
				break;
			case 20:
				name = "执行中";
				break;
			case 30:
				name = "已完成";
				break;
			case 40:
				name = "已终止";
				break;
			}
		}
		return name;
	}
	
	private String pcjhzt(Object dm) {
		String name = "";
		if (dm != null) {
			switch (Integer.parseInt(String.valueOf(dm))) {
			case 10:
				name = "未下发";
				break;
			case 20:
				name = "已下发";
				break;
			case 30:
				name = "计划制定中";
				break;
			case 40:
				name = "工单已生成";
				break;
			case 50:
				name = "工单已下发";
				break;
			case 70:
				name = "加工中";
				break;
			case 80:
				name = "加工完成";
				break;
			case 85:
				name = "已入库";
				break;
			case 90:
				name = "已终止";
				break;
			}
		}
		return name;
	}
}





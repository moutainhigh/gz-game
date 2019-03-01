package com.walmart.mobile.checkout.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.walmart.mobile.checkout.domain.GlobalPromotion;
import com.walmart.mobile.checkout.domain.GpItemMapping;
import com.walmart.mobile.checkout.domain.ScanPriceLog;
import com.walmart.mobile.checkout.domain.ScanPriceLogMSSql;
import com.walmart.mobile.checkout.domain.StoreInventoryPriceMSSql;
import com.walmart.mobile.checkout.entity.GPOffer;
import com.walmart.mobile.checkout.entity.IPOffers;
import com.walmart.mobile.checkout.entity.InventoryPrice;
import com.walmart.mobile.checkout.entity.ProductDetail;
import com.walmart.mobile.checkout.entity.Store;
import com.walmart.mobile.checkout.entity.StoreElabelConfig;
import com.walmart.mobile.checkout.exception.exceptionType.ApplicationException;
import com.walmart.mobile.checkout.mapper.informix.ScanPriceLogMapper;
import com.walmart.mobile.checkout.mapper.mssql.ScanPriceLogMSSqlMapper;
import com.walmart.mobile.checkout.mapper.mssql.StoreInventoryPriceMSSqlMapper;
import com.walmart.mobile.checkout.repository.InventoryPriceRepository;
import com.walmart.mobile.checkout.repository.StoreElabelConfigRepository;
import com.walmart.mobile.checkout.utils.AppUtils;
import com.walmart.mobile.checkout.utils.DateUtil;
import com.walmart.mobile.checkout.utils.SignUtils;
import com.walmart.mobile.checkout.vo.BatchAndItemLine;
import com.walmart.mobile.checkout.vo.ItemLine;

@Service
@EnableMongoRepositories("com.walmart.mobile.checkout.repository")
public class SyncInventoryPriceService extends BaseService {

	private final static Logger LOGGER = LoggerFactory.getLogger(SyncInventoryPriceService.class);

	@Autowired
	ScanPriceLogMapper scanPriceLogMapper;

	@Autowired
	InventoryPriceRepository inventoryPriceRepository;

	@Autowired
	StoreInventoryPriceMSSqlMapper storeInventoryPriceMSSqlMapper;

	@Autowired
	IdService idService;

	@Autowired
	ScanPriceLogMSSqlMapper scanPriceLogMSSqlMapper;

	@Autowired
	private StorePriceService storePriceService;

	@Autowired
	private ProductDetailService productDetailService;

	@Autowired
	private ResolveVatPriceService resolveVatPriceService;

	@Autowired
	private WeightItemService weightItemService;

	@Autowired
	private RelationItemService relationItemService;

	@Autowired
	private IpProducerService ipProducerService;
	
	@Value("${sendToElabel.flag}")
	private Boolean sendToElabelFlag;

	@Value("${sendToElabel.size:100}")
	private Integer size;

	@Value("${sendToElabel.write.log}")
	private Boolean writeLog;

	@Value("${begin.time.interval:5}")
	private int interval;

	@Value("${elabel.qrCode.url}")
	private String elabelQrCodeUrl;

	@Value("${ip.increment.delete.switch:false}")
	private boolean ipIncrementDeleteSwitch;

	@Value("${elabel.isTest:false}")
	private boolean elabelIsTest;

	@Autowired
	private SendToElabelService sendToElabelService;

	@Autowired
	StoreElabelConfigRepository storeElabelConfigRepository;
	
	// @Autowired
	// private SyncInventoryService syncFindMyItemService;

	@Value("${tax.change.isRefreshWasPrice:false}")
	private Boolean isRefreshWasPrice;

	public static List<Integer> StringToIntegerLst(List<String> inList) throws ApplicationException {
		List<Integer> outList = new ArrayList<>(inList.size());
		try {
			for (int i = 0, j = inList.size(); i < j; i++) {
				outList.add(Integer.parseInt(inList.get(i)));
			}
		} catch (Exception e) {
			throw new ApplicationException("StringToIntegerLst failed. cause:{}", e);
		}

		return outList;
	}

	/**
	 * Async deal with the changed ItemNbr List from
	 * smm006_chg_proc_log,smm012_chg_proc_log
	 * 
	 * @param store
	 *            门店
	 * @param lastRun
	 *            上次运行时间
	 * @param countDown
	 * 
	 * @throws ApplicationException
	 */
	@Async
	public void syncOneStore(Store store, String lastRun, CountDownLatch countDown) throws ApplicationException {
		// Map<Long, Item> map = null;// changed Upc Map

		Integer storeId = store.getStoreId();
		LOGGER.info("增量inventoryprice begin, 门店号：{}------", storeId);
		boolean bool = false;
		StoreInventoryPriceMSSql storeInventoryPriceMSSql = new StoreInventoryPriceMSSql();
		Date currentDate = new Date();// 程序执行时间

		try {
			List<ScanPriceLog> scanPriceLogList = checkDate(store, lastRun, currentDate);
			if (scanPriceLogList == null || scanPriceLogList.isEmpty()) {
				// no new data in trigger table
				LOGGER.info("---checkDate result:门店号:{}, trigger表 smm006_chg_proc_log,smm012_chg_proc_log size is 0",
						storeId);
			} else {
				LOGGER.info(
						"---checkDate result：门店号:{}, trigger表 smm006_chg_proc_log,smm012_chg_proc_log大小：{} 条记录, items：{}",
						storeId, scanPriceLogList.size(), JSONObject.toJSONString(scanPriceLogList));
				bool = true;
				String batchId = idService.buildLogId(store.getStoreId());
				insertStoreInventoryPriceMSSql(storeInventoryPriceMSSql, store, batchId);
				storeInventoryPriceMSSql.setStatus(2);// 失败
				
				doSyncOneStore(store, batchId, scanPriceLogList);

				// map = doSyncOneStore(store, batchId, scanPriceLogList);
				// 发送给Wm FMI For Price
				// syncFindMyItemService.sendToWmFmiForPrice(store, map);
				
				storeInventoryPriceMSSql.setLastRun(currentDate);// 设置为程序这次运行的时间为下次的[开始时间]
				storeInventoryPriceMSSql.setStatus(1);// 成功
			}
		} catch (Exception e) {
			throw new ApplicationException("invoke doSyncOneStore failed. cause:{}", e);
		} finally {
			countDown.countDown();
			if (bool) {
				updateStoreInventoryPriceMSSql(storeInventoryPriceMSSql);
			}
		}
	}
	
	public List<ScanPriceLog> checkDate(Store store, String lastRun, Date currentDate)
			throws ApplicationException, ParseException {
		Integer storeId = store.getStoreId();
		LOGGER.info("checkDate 门店号：{}------", storeId);

		// 切换smart数据源
		switchDataSourceByStoreId(storeId);

		String logTsStart = lastRun;
		String logTsEnd = DateUtil.getDateString(currentDate);// 程序执行时间
		LOGGER.info("扫描trigger表,smm006_chg_proc_log,smm012_chg_proc_log表:门店号：{}, 参数logTsStart 时间为：{}, logTsEnd 时间为：{}",
				storeId, logTsStart, logTsEnd);

		// 执行sql时，logTsStart往前推1分钟（60秒），防止smart写trigger表没有写完的情况
		logTsStart = getLogTsStartBeforeOneMin(store, logTsStart);
		LOGGER.info(
				"扫描trigger表,smm006_chg_proc_log,smm012_chg_proc_log表:门店号：{}, 参数logTsStartBeforeOneMin 时间为：{}, logTsEnd 时间为：{}",
				storeId, logTsStart, logTsEnd);

		long begin;
		long end;
		begin = System.currentTimeMillis();
		List<ScanPriceLog> scanPriceLogList = scanPriceLogMapper.getAllTriggerItemList(logTsStart, logTsEnd);
		end = System.currentTimeMillis();
		LOGGER.info("扫描trigger表结果：门店号：{} ,performance:used time:{} s", storeId, (end - begin) / 1000);

		// return check result
		return scanPriceLogList;
	}

	private String getLogTsStartBeforeOneMin(Store store, String logTsStart)
			throws ApplicationException, ParseException {
		LOGGER.info("扫描trigger表结果：门店号：{} ,input logTsStart:{}", store.getStoreId(), logTsStart);
		String logTsStartBeforeOneMin = DateUtil.getSecondsBefore(logTsStart, interval);
		LOGGER.info("扫描trigger表结果：门店号：{} ,output logTsStartBeforeOneMin:{}", store.getStoreId(),
				logTsStartBeforeOneMin);
		return logTsStartBeforeOneMin;
	}

	public void insertStoreInventoryPriceMSSql(StoreInventoryPriceMSSql storeInventoryPriceMSSql, Store store,
			String batchId) throws ApplicationException {
		storeInventoryPriceMSSql.setStoreId(store.getStoreId());
		storeInventoryPriceMSSql.setBatchId(batchId);
		storeInventoryPriceMSSql.setNameCn(store.getShortNameCn());
		storeInventoryPriceMSSql.setLastRun(null);// 初始为null
		storeInventoryPriceMSSql.setCreateDate(new Date());
		storeInventoryPriceMSSql.setServerName(AppUtils.getHostNameAndAddressString());
		storeInventoryPriceMSSqlMapper.insert(storeInventoryPriceMSSql);
	}

	public void updateStoreInventoryPriceMSSql(StoreInventoryPriceMSSql storeInventoryPriceMSSql) {
		storeInventoryPriceMSSql.setUpdateDate(new Date());
		storeInventoryPriceMSSqlMapper.update(storeInventoryPriceMSSql);
	}

	public void doSyncOneStore(Store store, String batchId, List<ScanPriceLog> scanPriceLogList)
			throws ApplicationException {
		Integer storeId = store.getStoreId();
		LOGGER.info("doSyncOneStore 门店号：{}------", storeId);

		// 备份:保存变更明细Log数据(备份去重前的scan_price_log_trigger表)
		saveScanPriceLogMSSql(batchId, scanPriceLogList, storeId);

		long begin;
		long end;
		begin = System.currentTimeMillis();

		List<ScanPriceLog> sortDistinctResultList = sortSplitDistinct(scanPriceLogList);

		end = System.currentTimeMillis();
		LOGGER.info(
				"排序（倒序DESC）、拆分、去重后的扫描结果：门店号：{} ,smm006_chg_proc_log,smm012_chg_proc_log表大小：{} 条记录,performance:used time:{} s,distinct items：{}",
				storeId, sortDistinctResultList.size(), (end - begin) / 1000,
				JSONObject.toJSONString(sortDistinctResultList));

		List<Long> itemNumsDelete = new ArrayList<>();
		List<Long> itemNumsUpsert = new ArrayList<>();
		for (ScanPriceLog spl : sortDistinctResultList) {
			// logChangeTypeCd
			String logChangeTypeCd = spl.getLogChangeTypeCd().toString();
			// itemNum
			Long itemNum = spl.getItemNum();

			if ("D".equals(logChangeTypeCd)) {
				itemNumsDelete.add(itemNum);
			} else if ("U".equals(logChangeTypeCd) || "I".equals(logChangeTypeCd)) {
				itemNumsUpsert.add(itemNum);
			}
		}

		if (!ipIncrementDeleteSwitch) {
			if (!itemNumsDelete.isEmpty()) {
				LOGGER.info("deleteInventoryPricesByItem. storeId:{}", storeId);
				// delete InventoryPrice(delete)
				deleteInventoryPricesByItem(store, itemNumsDelete);
			}
		} else {
			LOGGER.info("setIpIncrementDeleteAndSendToMq. storeId:{}", storeId);
			// 需要软删除的ipItemNum推送到mq处理
			ipProducerService.setIpIncrementDeleteAndSendToMq(itemNumsDelete, storeId, batchId);
		}

		if (!itemNumsUpsert.isEmpty()) {
			// save InventoryPrice
			saveInventoryPrices(store, itemNumsUpsert, batchId);
		}

	}

	/**
	 * saveScanPriceLogMSSql
	 * 
	 * @param batchId
	 *            批次号
	 * @param scanPriceLogList
	 *            更新log
	 * @param storeId
	 *            店号
	 * @throws ApplicationException
	 */
	private void saveScanPriceLogMSSql(String batchId, List<ScanPriceLog> scanPriceLogList, Integer storeId)
			throws ApplicationException {

		for (ScanPriceLog spl : scanPriceLogList) {
			ScanPriceLogMSSql scanPriceLogMSSql = new ScanPriceLogMSSql();
			BeanUtils.copyProperties(spl, scanPriceLogMSSql);
			scanPriceLogMSSql.setBatchId(batchId);
			scanPriceLogMSSqlMapper.insertScanPriceLogMSSql(scanPriceLogMSSql);
		}

		LOGGER.info("store id [{}], batch Id [{}], scanPriceLogList size [{}], saveScanPriceLogMSSql success.", storeId,
				batchId, scanPriceLogList.size());
	}

	public List<ScanPriceLog> sortSplitDistinct(List<ScanPriceLog> scanPriceLogList) throws ApplicationException {
		List<ScanPriceLog> resultList = new ArrayList<>();

		doSort(scanPriceLogList);
		resultList.addAll(doDistinct(doSplitScanPriceListBySource(scanPriceLogList, true)));
		resultList.addAll(doDistinct(doSplitScanPriceListBySource(scanPriceLogList, false)));

		return resultList;
	}

	/**
	 * 排序（倒序DESC）:根据LogChangeSeqNbr排序
	 * 
	 */
	private void doSort(List<ScanPriceLog> scanPriceLogList) {
		// 排序（倒序DESC）
		scanPriceLogList.sort((o1, o2) -> {
			Long seq1 = o1.getLogChangeSeqNbr();
			Long seq2 = o2.getLogChangeSeqNbr();
			if (seq1 > seq2) {
				return -1;
			} else if (seq1 < seq2) {
				return 1;
			} else {
				return 0;
			}
		});
	}

	/**
	 * 拆分：根据trigger_source_type拆分list
	 * 
	 */
	private List<ScanPriceLog> doSplitScanPriceListBySource(List<ScanPriceLog> scanPriceLogList, boolean is006) {
		// 定义返回结果
		List<ScanPriceLog> resultList = new ArrayList<>();
		String eq = is006 ? "006" : "012";
		scanPriceLogList.forEach(action -> {
			if (action.getTriggerSourceType().equals(eq)) {
				resultList.add(action);
			}
		});
		return resultList;
	}

	/**
	 * 去重:scanPriceLogList
	 * 
	 */
	/*
	 * private List<ScanPriceLog> doDistinct(List<ScanPriceLog>
	 * scanPriceLogList) { Set<Long> set = new HashSet<>(); // 定义返回结果
	 * List<ScanPriceLog> resultList = new ArrayList<>();
	 * scanPriceLogList.forEach(action -> { if
	 * (!set.contains(action.getItemNum())) { set.add(action.getItemNum());
	 * resultList.add(action); } }); return resultList; }
	 */
	private List<ScanPriceLog> doDistinct(List<ScanPriceLog> scanPriceLogList) {
		Set<String> set = new HashSet<>();
		// 定义返回结果
		List<ScanPriceLog> resultList = new ArrayList<>();

		scanPriceLogList.forEach(action -> {
			// 根据ItemNum和LogChangeTypeCd关键字进行去重
			String keyword = action.getItemNum().toString().concat(action.getLogChangeTypeCd().toString());
			if (!set.contains(keyword)) {
				set.add(keyword);
				resultList.add(action);
			}
		});
		return resultList;
	}

	/**
	 * deleteInventoryPricesByItem
	 * 
	 * @param store
	 *            门店
	 * @param itemNumsDelete
	 *            商品号
	 * @throws ApplicationException
	 */
	public void deleteInventoryPricesByItem(Store store, List<Long> itemNumsDelete) throws ApplicationException {
		long begin = System.currentTimeMillis();
		Integer storeId = store.getStoreId();

		List<InventoryPrice> inventoryPriceList = new ArrayList<>();
		List<Long> deleteIpList = new ArrayList<>();
		List<Long> ignoreDeleteIpList = new ArrayList<>();
		Date date = new Date();
		List<Long> paramList = new ArrayList<>();
		for (Long itemNum : itemNumsDelete) {
			paramList.clear();
			paramList.add(itemNum);
			List<InventoryPrice> priceList = inventoryPriceRepository.findByStoreIdAndItemNumberIn(storeId, paramList);
			if (!priceList.isEmpty()) {
				priceList.forEach(price -> {
					price.setStockStatus(InventoryPrice.STOCK_STATUS_NOT_SELL);// 2:不售卖（下架）;(改为软删除)
					price.setUpdatedTime(date);// 设置更新时间
					price.setInventoryUpdatedTime(date);// 设置更新时间-Trigger触发的软删除
					inventoryPriceList.add(price);
				});
				deleteIpList.add(itemNum);
			} else {
				ignoreDeleteIpList.add(itemNum);
			}
		}

		if (!inventoryPriceList.isEmpty()) {
			// 删除InventoryPrice(改为软删除)
			inventoryPriceRepository.save(inventoryPriceList);// 2:不在该商店售卖
			// inventoryPriceRepository.delete(inventoryPriceList);
		}

		LOGGER.info(
				"deleteInventoryPricesByItem(删除主次商品)结果：门店号：{}, soft delete大小：{} 条记录, itemNum明细信息：{}; "
						+ "未delete结果(原因：itemNum在Mongo InventoryPrice中不存在)： 未delete大小：{} 条记录, itemNum明细信息：{}"
						+ "performance:used time:{} s",
				storeId, deleteIpList.size(), JSONArray.toJSONString(deleteIpList), ignoreDeleteIpList.size(),
				JSONArray.toJSONString(ignoreDeleteIpList), (System.currentTimeMillis() - begin) / 1000);
	}

	/**
	 * saveInventoryPrices
	 * 
	 * @param store
	 *            门店
	 * @param itemNumsUpsert
	 *            商品号
	 * @throws ApplicationException
	 */
	public Map<Long, InventoryPrice> saveInventoryPrices(Store store, List<Long> itemNumsUpsert, String batchId)
			throws ApplicationException {
		long begin = System.currentTimeMillis();
		Integer storeId = store.getStoreId();

		Map<Long, InventoryPrice> map = saveInventoryPriceAndGp(store, itemNumsUpsert, batchId, false);

		LOGGER.info("Mongo InventoryPrice upsert结果：门店号：{}, upsert大小：{} 条记录, performance:used time:{} s", storeId,
				itemNumsUpsert.size(), (System.currentTimeMillis() - begin) / 1000);

		return map;
	}

	public Map<Long, InventoryPrice> saveInventoryPriceAndGp(Store store, List<Long> itemNums, String batchId,
			boolean inInvokeShellFlag) throws ApplicationException {

		boolean outInvokeShellFalg = false;
		if (inInvokeShellFlag) {
			outInvokeShellFalg = true;
		}
		LOGGER.info("---saveInventoryPriceAndGp, store:{}, inInvokeShellFlag:{}, outInvokeShellFalg:{}",
				store.getStoreId(), inInvokeShellFlag, outInvokeShellFalg);

		LOGGER.info("---saveInventoryPriceAndGp begin. store:{}", store.getStoreId());
		Map<Long, InventoryPrice> map = new HashMap<>();
		HashMap<Integer, GPOffer> gpNameMap = new HashMap<>();
		// 通过informix InventoryPriceSource 构建InventoryPrice、ProductDetail
		List<Long> unitItemList = new ArrayList<>();// 单只商品列表
		List<Long> invalidTaxRateItemNumList = new ArrayList<>();// 不存在税率的商品列表
		List<ProductDetail> products = storePriceService.inventoryPrice(map, store, itemNums, unitItemList,
				invalidTaxRateItemNumList);
		LOGGER.info("---products from SMART.size: {}", products.size());
		productDetailService.saveProduct(products);

		// 通过itemNums获取 GpItem映射关系
		List<GpItemMapping> mappingList = storePriceService.getItemGpMappingInBatchsFun(store, itemNums);
		LOGGER.info("---item gp from SMART.size: {}", mappingList.size());
		if (!mappingList.isEmpty()) {
			// 通过 gpid set获取smart GP
			List<GlobalPromotion> list = storePriceService.getGlobalPromotionList(getGpIdSet(mappingList));
			LOGGER.info("---gp from SMART.size: {}", list.size());

			// 通过smart GP构建mongo GPOffer
			Map<Integer, GPOffer> offers = storePriceService.globalPromotion(store, list, gpNameMap);
			Set<Integer> invalidSet = storePriceService.getInvalidGp(offers);
			storePriceService.gpAndIpMapping(map, store, mappingList, gpNameMap, invalidSet);
			storePriceService.clearInventoryPriceInvalidOffer(invalidSet, map);
			storePriceService.saveGpoffer(offers);
		}
		// 设置ews flag
		storePriceService.ewsFlag(map);
		LOGGER.info("---ews Flag set to inventoryPrice done");

		// 设置was price
		storePriceService.wasPrice(store, map, itemNums);
		LOGGER.info("---was Price set to inventoryPrice done");

		storePriceService.itemModular(map, itemNums);
		LOGGER.info("---itemModular set to inventoryPrice done");

		// VAT
		Map<Long, List<BigDecimal>> vatPriceMap = new HashMap<>();
		List<String> noVatItemList = new ArrayList<>();
		if (outInvokeShellFalg) {
			// by shell
			vatPriceMap = resolveVatPriceService.getVaildVatPrice(itemNums, store, noVatItemList);

		} else {
			// new
			vatPriceMap = resolveVatPriceService.getVaildVatPrice(map, store, itemNums == null);
		}
		LOGGER.info("---getVaildVatPrice from SMART done");

		relationItemService.executeRelationItem(map);
		LOGGER.info("---relationItem set to inventoryPrice done");

		weightItemService.executeWeightItem(map);
		LOGGER.info("---weightItem set to inventoryPrice done");

		storePriceService.saveInventoryPriceList(store, map, vatPriceMap, noVatItemList);
		LOGGER.info("---Inventory Price to MONGO done. MONGO.size: {}, storeId:{}", map.size(), store.getStoreId());

		storePriceService.saveUnitItemList(store, unitItemList);
		LOGGER.info("---saveUnitItemList to MONGO done. unitItemList.size: {}", unitItemList.size());

		// 将不存在税率的商品通过mq推送到消费者处理
		// ipProducerService.setIpNonTaxAndSendToMq(invalidTaxRateItemNumList,
		// store.getStoreId(), batchId);
		LOGGER.info("--not send to mq(将不存在税率的商品不再通过mq处理). 门店 : {}, 大小: {}, invalidTaxRateItemNumList: {}。 ",
				store.getStoreId(), invalidTaxRateItemNumList.size(), invalidTaxRateItemNumList.toString());

		// 同步到Elabel系统
		sendToElabel(map, store, batchId);
		return map;
	}

	private Set<Integer> getGpIdSet(List<GpItemMapping> mappingList) {
		Set<Integer> gpIdSet = new HashSet<>();
		for (GpItemMapping mapping : mappingList) {
			gpIdSet.add(mapping.getGpOfferId());
		}
		return gpIdSet;
	}

	public void sendToElabel(Map<Long, InventoryPrice> map, Store store, String batchId) throws ApplicationException {

		StoreElabelConfig storeElabelConfig = storeElabelConfigRepository.findByStoreCode(store.getStoreId());
		LOGGER.info("---findByStoreCode from MONGO done. storeId: {}, sendToElabelFlag: {}, storeElabelConfig: {}",
				store.getStoreId(), sendToElabelFlag, storeElabelConfig == null
						? "this store has not Elabel Config in mongo." : JSONObject.toJSONString(storeElabelConfig));

		// 变价是否同步到Elabel系统
		if (sendToElabelFlag && storeElabelConfig != null) {

			long begin = System.currentTimeMillis();
			List<InventoryPrice> list = new ArrayList<>();
			int i = 0;
			int j = 0;
			for (Map.Entry<Long, InventoryPrice> entry : map.entrySet()) {
				InventoryPrice ip = entry.getValue();
				// LOGGER.info("---Item Number {}, dept is {}",
				// ip.getItemNumber(), ip.getAcctDept());

				if (storeElabelConfig.getDeptList().contains(ip.getAcctDept())) {
					list.add(ip);
				}
				if (list.size() == size) {
					doSendToElabel(list, batchId + ++i, storeElabelConfig);
					list.clear();
					j = j + size;
				}
			}
			if (!list.isEmpty()) {
				doSendToElabel(list, batchId + ++i, storeElabelConfig);
				j = j + list.size();
				list.clear();
			}

			LOGGER.info("------ send to elabel 结束。信息：门店号：{} ,父批次号：{} ,总商品数：{} ,performance:used time:{} s",
					store.getStoreId(), batchId, j, (System.currentTimeMillis() - begin) / 1000);
		}
	}

	private void doSendToElabel(List<InventoryPrice> list, String batchIdS, StoreElabelConfig storeElabelConfig)
			throws ApplicationException {
		LOGGER.info("------ doSendToElabel 信息, 批次号：{}", batchIdS);
		BatchAndItemLine batchAndItemLine = setBatchAndItemLine(list, batchIdS, storeElabelConfig);
		String requestJson = JSON.toJSONString(batchAndItemLine);
		String token = storeElabelConfig.getToken();
		if (token != null) {
			try {
				String sign = getSign(batchAndItemLine.getItems(), token);
				batchAndItemLine.setSign(sign);
				requestJson = JSON.toJSONString(batchAndItemLine);
			} catch (Exception e) {
				LOGGER.error("------   getSignBySHA512 error. e: {}", e.toString());
				return;
			}
		}
		if (writeLog && storeElabelConfig.getWriteLogFlag()) {
			LOGGER.info("------ doSendToElabel 信息, 商品信息：{}", requestJson);
		}

		sendToElabelService.worker(requestJson, storeElabelConfig.getUrl());
	}

	/**
	 * 计算签名
	 * 
	 */
	private String getSign(List<ItemLine> items, String token) throws ApplicationException {
		String sign = null;
		String jsonInventoryItems = JSON.toJSONString(items, new PropertyFilter() {
			@Override
			public boolean apply(Object object, String name, Object value) {
				return (name.equalsIgnoreCase("itemName") || name.equalsIgnoreCase("itemShortName")) ? false : true;
			}
		});

		try {

			sign = SignUtils.getSignBySHA512(jsonInventoryItems, token);
		} catch (Exception e) {
			throw new ApplicationException("---getSignBySHA512 occur exp.", e);
		}

		return sign;
	}

	private BatchAndItemLine setBatchAndItemLine(List<InventoryPrice> list, String batchId,
			StoreElabelConfig storeElabelConfig) throws ApplicationException {
		BatchAndItemLine batchAndItemLine = new BatchAndItemLine();
		batchAndItemLine.setStoreCode(storeElabelConfig.getStoreCode().toString());
		batchAndItemLine.setCustomerStoreCode(storeElabelConfig.getCustomerCode());
		batchAndItemLine.setBatchNo(batchId);
		batchAndItemLine.setBatchSize(String.valueOf(list.size()));

		List<ItemLine> itemLines = new ArrayList<>();
		for (InventoryPrice ip : list) {
			ItemLine itemLine = new ItemLine();
			if (elabelIsTest) {
				// test
				itemLine.setSku(ip.getUpc().toString());
				itemLine.setEan(ip.getItemNumber().toString());
			} else {
				// 生产
				itemLine.setSku(ip.getItemNumber().toString());
				itemLine.setEan(ip.getUpc().toString());
			}
			itemLine.setCustomerStoreCode(storeElabelConfig.getCustomerCode());
			itemLine.setItemName(ip.getDescOnline());
			itemLine.setItemShortName(ip.getPosDesc());
			itemLine.setPrice1(ip.getWeightType() ? ip.getWeightPrice().toString() : ip.getPriceWithTax().toString());
			itemLine.setPrice2(ip.getWasPrice().toString());
			itemLine.setItemStatus(ip.getCode() == null ? "" : ip.getCode().trim());// 去掉空格
			itemLine.setUnit(ip.getWeightType() ? "500g" : ip.getUnit());
			itemLine.setPlaceOfOrigin(ip.getManufacturer() == null ? "" : ip.getManufacturer().trim());// 去掉空格
			itemLine.setSpecification(ip.getSpecification() == null ? "" : ip.getSpecification().trim());// 去掉空格
			itemLine.setGrade(ip.getLevel());
			itemLine.setPackSize(ip.getPackQty());
			itemLine.setPromoDateFrom(ip.getPriceStartDate());
			itemLine.setPromoDateTo(ip.getPriceEndDate());
			itemLine.setShelfCode(ip.getHorizFacingsNbr());
			itemLine.setDisplayLocation(ip.getModularCode());
			itemLine.setLevel1CategoryCode(ip.getAcctDept());
			itemLine.setPrice2Description(ip.getPromTab());
			itemLine.setPricingStaff(ip.getPriceManager());
			itemLine.setDescription(ip.getTexture());

			Date tmpDate = ip.getPriceUpdatedTime() == null ? ip.getUpdatedTime() : ip.getPriceUpdatedTime();
			itemLine.setRsrvTxt5(new SimpleDateFormat("MM/dd/yy").format(tmpDate));// 标签打印时间:09/06/17

			String rsrvTxt4 = "40".concat(String.format("%09d", ip.getItemNumber()));
			itemLine.setRsrvTxt4(rsrvTxt4);// 沃尔玛ITEM条码('40'||LPAD(itm.item_num,9,0)
											// AS item_barcode)

			// String qrCode = ip.getUpc().toString() + "0";
			String qrCode = rsrvTxt4 + "0";
			itemLine.setQrCode(elabelQrCodeUrl.concat(qrCode));

			List<IPOffers> gpOffersList = ip.getGpOffers();

			if (!gpOffersList.isEmpty()) {
				for (int i = 0; i < gpOffersList.size(); i++) {
					String description = gpOffersList.get(i).getPromotionDescCn().replace("{", "").replace("}", "");
					switch (3 + i) {
					case 3:
						itemLine.setPrice3Description(description);
						break;
					case 4:
						itemLine.setPrice4Description(description);
						break;
					case 5:
						itemLine.setPrice5Description(description);
						break;
					default:
						break;
					}
				}
			}
			itemLines.add(itemLine);
		}

		batchAndItemLine.setItems(itemLines);

		return batchAndItemLine;

	}

	public Map<Long, InventoryPrice> saveTaxChange(Store store, List<Long> itemNums, String batchId)
			throws ApplicationException {
		Integer storeId = store.getStoreId();
		LOGGER.info("---saveTaxChange begin. storeId:{}", storeId);
		Map<Long, InventoryPrice> map = new HashMap<>();
		// 通过informix InventoryPriceSource 构建InventoryPrice
		storePriceService.inventoryPrice_TaxChange(map, store, itemNums);// itemNums为null
		LOGGER.info("---inventoryPrice TaxChange from SMART(include new ip), SMART.size: {}, storeId:{}", map.size(),
				storeId);

		LOGGER.info("---tax change saveTaxChange, isRefreshWasPrice {}", isRefreshWasPrice);
		if (isRefreshWasPrice) {
			// 设置was price
			storePriceService.wasPrice(store, map, itemNums);
			LOGGER.info("---tax change, was Price set to inventoryPrice done");
		}

		storePriceService.saveInventoryPriceList_TaxChange(map, store);
		return map;
	}
}
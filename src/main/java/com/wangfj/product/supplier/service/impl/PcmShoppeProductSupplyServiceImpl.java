package com.wangfj.product.supplier.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wangfj.product.maindata.domain.entity.PcmErpProductSupply;
import com.wangfj.product.maindata.persistence.PcmErpProductSupplyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wangfj.core.constants.ComErrorCodeConstants;
import com.wangfj.core.framework.exception.BleException;
import com.wangfj.core.utils.StringUtils;
import com.wangfj.product.maindata.domain.entity.PcmErpProduct;
import com.wangfj.product.maindata.domain.entity.PcmShoppeProduct;
import com.wangfj.product.maindata.persistence.PcmErpProductMapper;
import com.wangfj.product.maindata.persistence.PcmShoppeProductMapper;
import com.wangfj.product.supplier.domain.entity.PcmShoppeProductSupply;
import com.wangfj.product.supplier.domain.entity.PcmSupplyInfo;
import com.wangfj.product.supplier.domain.vo.PcmShoppeProSupplyUploadDto;
import com.wangfj.product.supplier.persistence.PcmShoppeProductSupplyMapper;
import com.wangfj.product.supplier.persistence.PcmSupplyInfoMapper;
import com.wangfj.product.supplier.service.intf.IPcmShoppeProductSupplyService;
import com.wangfj.util.Constants;

/**
 * 专柜商品对应多个供应商
 *
 * @Class Name PcmShoppeProductSupplyServiceImpl
 * @Author wuxiong
 * @Create In 2015年7月13日
 */
@Service
public class PcmShoppeProductSupplyServiceImpl implements IPcmShoppeProductSupplyService {

    private static final Logger logger = LoggerFactory.getLogger(PcmShoppeProductSupplyServiceImpl.class);

    @Autowired
    private PcmShoppeProductSupplyMapper shoppeProductSupplyMapper;
    @Autowired
    private PcmSupplyInfoMapper supplyInfoMapper;
    @Autowired
    private PcmShoppeProductMapper shoppeProductMapper;
    @Autowired
    private PcmErpProductMapper erpProductMapper;
    @Autowired
    private PcmErpProductSupplyMapper erpProductSupplyMapper;

    /**
     * 判重
     *
     * @param shoppeProductSupply
     * @return Boolean
     * @Methods Name isExist
     * @Create In 2015-9-17 By wangxuan
     */
    @Override
    public Boolean isExist(PcmShoppeProductSupply shoppeProductSupply) {
        logger.info("start isExist(),param:" + shoppeProductSupply.toString());

        Boolean flag = false;// 标记是否存在，默认不存在

        Map<String, Object> paramMap = new HashMap<String, Object>();

        Long supplySid = shoppeProductSupply.getSupplySid();// 供应商sid
        Long shoppeProductSid = shoppeProductSupply.getShoppeProductSid();// 专柜商品sid
        String productSid = shoppeProductSupply.getProductSid();// 大码sid
        if (supplySid != null) {
            paramMap.put("supplySid", supplySid);
        }
        if (shoppeProductSid != null) {
            paramMap.put("shoppeProductSid", shoppeProductSid);
        }
        if (StringUtils.isNotEmpty(productSid)) {
            paramMap.put("productSid", productSid);
        }
        paramMap.put("status", Constants.PUBLIC_0);// 有效标记，未删除的

        List<PcmShoppeProductSupply> list = shoppeProductSupplyMapper.getListByParam(paramMap);
        if (list != null && !list.isEmpty()) {
            flag = true;// 修改为存在
        }
        logger.info("end isExist(),return:" + flag);
        return flag;

    }

    /**
     * 一品多供应商关系上传
     *
     * @param dto
     * @return Map<String, Object>
     * @Methods Name uploadShoppeProSupply
     * @Create In 2015-8-28 By wangxuan
     */
    @Override
    @Transactional
    public Map<String, Object> uploadShoppeProSupply(PcmShoppeProSupplyUploadDto dto) {
        logger.info("start uploadShoppeProSupply(),param:" + dto.toString());

        Map<String, Object> resultMap = new HashMap<String, Object>();
        Integer flag = Constants.PUBLIC_0;

        String storeCode = dto.getStoreCode();// 门店编码
        String supplierCode = dto.getSupplierCode();// 供应商编码
        String supplierProductCode = dto.getSupplierProductCode();// 专柜商品编码
        String productCode = dto.getProductCode();// 大码编码
        String ACTION_CODE = dto.getACTION_CODE();// 本条记录对应的操作 (A添加；U更新；D删除)

        if (!StringUtils.isNotEmpty(storeCode)) {
            throw new BleException(
                    ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_STORECODE_IS_NULL.getErrorCode(),
                    ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_STORECODE_IS_NULL.getMemo());
        }
        if (!StringUtils.isNotEmpty(supplierCode)) {
            throw new BleException(
                    ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_SUPPLIERCODE_IS_NULL.getErrorCode(),
                    ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_SUPPLIERCODE_IS_NULL.getMemo());
        }
        if (!StringUtils.isNotEmpty(ACTION_CODE)) {
            throw new BleException(
                    ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_ACTIONCODE_IS_NULL.getErrorCode(),
                    ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_ACTIONCODE_IS_NULL.getMemo());
        }

        boolean productTypeFlag = true;
        if (StringUtils.isNotEmpty(supplierProductCode)) {//判断商品类型-大码商品、专柜商品
            productTypeFlag = true;//专柜商品
        } else {
            productTypeFlag = false;//大码商品
        }

        PcmShoppeProductSupply shoppeProductSupply = null;
        PcmErpProductSupply erpProductSupply = null;
        if (productTypeFlag) {
            shoppeProductSupply = new PcmShoppeProductSupply();//专柜商品与供应商的关系实体
        } else {
            erpProductSupply = new PcmErpProductSupply();//大码商品与供应商的关系实体
        }

        // 封装供应商sid
        if (StringUtils.isNotEmpty(storeCode) && StringUtils.isNotEmpty(supplierCode)) {
            Map<String, Object> paramSupply = new HashMap<String, Object>();
            paramSupply.put("shopSid", storeCode);
            paramSupply.put("supplyCode", supplierCode);
            List<PcmSupplyInfo> supplyInfoList = supplyInfoMapper.selectSupplyByShopCodeAndSupplyCode(paramSupply);
            if (supplyInfoList != null && supplyInfoList.size() == 1) {
                if (productTypeFlag) {//判断商品类型-大码商品、专柜商品
                    shoppeProductSupply.setSupplySid(supplyInfoList.get(0).getSid());
                } else {
                    erpProductSupply.setSupplySid(supplierCode);
                }
            } else {
                throw new BleException(
                        ComErrorCodeConstants.ErrorCode.SUPPLYINFO_NOT_EXISTENCE.getErrorCode(),
                        ComErrorCodeConstants.ErrorCode.SUPPLYINFO_NOT_EXISTENCE.getMemo());
            }
        }
        Map<String, Object> shoppeProductMap = null;//下发专柜商品参数
        Integer productType = null;//大码商品经营方式
        if (productTypeFlag) {//判断商品类型-大码商品、专柜商品
            // 封装专柜商品sid
            Map<String, Object> paramShoppePro = new HashMap<String, Object>();
            paramShoppePro.put("shoppeProSid", supplierProductCode);
            List<PcmShoppeProduct> shoppeProductList = shoppeProductMapper.selectShoppeProductByShoppeProCode(paramShoppePro);
            if (shoppeProductList != null && shoppeProductList.size() == 1) {
                Long sid = shoppeProductList.get(0).getSid();
                shoppeProductSupply.setShoppeProductSid(sid);
                //下发专柜商品的sid
                if (ACTION_CODE.trim().toUpperCase().equals(Constants.D)) {
                    shoppeProductMap = new HashMap<String, Object>();
                    shoppeProductMap.put("sid", sid);
                    shoppeProductMap.put("type", 1);
                }
            } else {
                throw new BleException(
                        ComErrorCodeConstants.ErrorCode.SUPPLYINFO_NOT_EXISTENCE.getErrorCode(), "专柜商品不存在！");
            }
        } else {
            // 封装大码
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("storeCode", storeCode);
            paramMap.put("productCode", productCode);
            List<PcmErpProduct> list = erpProductMapper.selectListByParam(paramMap);
            if (list != null && list.size() == 1) {
                productType = list.get(0).getProductType();
                erpProductSupply.setShopSid(storeCode);
                erpProductSupply.setErpProductSid(productCode);
            } else {
                throw new BleException(
                        ComErrorCodeConstants.ErrorCode.SUPPLYINFO_NOT_EXISTENCE.getErrorCode(), "大码商品不存在！");
            }
        }

        if (StringUtils.isNotEmpty(ACTION_CODE)) {
            //判断商品类型-大码商品、专柜商品
            if (productTypeFlag) {
                //专柜商品
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("supplySid", shoppeProductSupply.getSupplySid());
                paramMap.put("shoppeProductSid", shoppeProductSupply.getShoppeProductSid());
                List<PcmShoppeProductSupply> list = shoppeProductSupplyMapper.selectListByParam(paramMap);
                if (ACTION_CODE.trim().toUpperCase().equals(Constants.A)) {
                    if (list.isEmpty()) {
                        shoppeProductSupply.setStatus(Constants.PUBLIC_0);// 有效标记
                        flag = shoppeProductSupplyMapper.insertSelective(shoppeProductSupply);
                    } else if (list.size() == 1) {
                        PcmShoppeProductSupply pcmShoppeProductSupply = list.get(0);
                        pcmShoppeProductSupply.setStatus(Constants.PUBLIC_0);
                        flag = shoppeProductSupplyMapper.updateByPrimaryKeySelective(pcmShoppeProductSupply);
                    } else {
                        throw new BleException(
                                ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_RELATION_EXISTENCE.getErrorCode(),
                                "存在两条专柜商品与供应商关系的重复数据！");
                    }
                } else if (ACTION_CODE.trim().toUpperCase().equals(Constants.D)) {
                    if (list.size() == 1) {
                        PcmShoppeProductSupply pcmShoppeProductSupply = list.get(0);
                        pcmShoppeProductSupply.setStatus(Constants.PUBLIC_1);
                        flag = shoppeProductSupplyMapper.updateByPrimaryKeySelective(pcmShoppeProductSupply);
                    }
                }
            } else {
                //大码
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("supplySid", erpProductSupply.getSupplySid());
                paramMap.put("shopSid", erpProductSupply.getShopSid());
                paramMap.put("erpProductSid", erpProductSupply.getErpProductSid());
                List<PcmErpProductSupply> list = erpProductSupplyMapper.selectListByParam(paramMap);
                if (ACTION_CODE.trim().toUpperCase().equals(Constants.A)) {
                    erpProductSupply.setStatus(Constants.PUBLIC_0);
                    if (productType == Constants.PCMERPPRODUCT_PRODUCT_TYPE_Z3) {
                        //联营大码只能有一个供应商
                        paramMap.clear();
                        paramMap.put("shopSid", erpProductSupply.getShopSid());
                        paramMap.put("erpProductSid", erpProductSupply.getErpProductSid());
                        List<PcmErpProductSupply> listU = erpProductSupplyMapper.selectListByParam(paramMap);
                        if (listU.isEmpty()) {
                            flag = erpProductSupplyMapper.insertSelective(erpProductSupply);
                        } else if (listU.size() == 1) {
                            PcmErpProductSupply pcmErpProductSupply = listU.get(0);
                            pcmErpProductSupply.setSupplySid(erpProductSupply.getSupplySid());
                            pcmErpProductSupply.setStatus(Constants.PUBLIC_0);
                           flag = erpProductSupplyMapper.updateByPrimaryKeySelective(pcmErpProductSupply);
                        } else {
                            throw new BleException(
                                    ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_RELATION_EXISTENCE.getErrorCode(),
                                    "联营大码商品有两条供应商关系数据！");
                        }
                    } else {
                        if (list.isEmpty()) {
                            flag = erpProductSupplyMapper.insertSelective(erpProductSupply);
                        } else if (list.size() == 1) {//如果数据已经存在，只修改状态即可。
                            PcmErpProductSupply pcmErpProductSupply = list.get(0);
                            pcmErpProductSupply.setStatus(Constants.PUBLIC_0);
                            flag = erpProductSupplyMapper.updateByPrimaryKeySelective(pcmErpProductSupply);
                        } else {
                            throw new BleException(
                                    ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_RELATION_EXISTENCE.getErrorCode(),
                                    "存在两条大码商品与供应商关系的重复数据！");
                        }
                    }
                } else if (ACTION_CODE.trim().toUpperCase().equals(Constants.D)) {
                    if (list.size() == 1) {
                        PcmErpProductSupply pcmErpProductSupply = list.get(0);
                        pcmErpProductSupply.setStatus(Constants.PUBLIC_1);
                        flag = erpProductSupplyMapper.updateByPrimaryKeySelective(pcmErpProductSupply);
                    }
                }
//                else if (ACTION_CODE.trim().toUpperCase().equals(Constants.U)) {//联营换供应商-联营只有一个供应商
//                    paramMap.clear();
//                    paramMap.put("shopSid", erpProductSupply.getShopSid());
//                    paramMap.put("erpProductSid", erpProductSupply.getErpProductSid());
//                    List<PcmErpProductSupply> listU = erpProductSupplyMapper.selectListByParam(paramMap);
//                    if (listU.size() == 1) {
//                        PcmErpProductSupply pcmErpProductSupply = listU.get(0);
//                        pcmErpProductSupply.setSupplySid(supplierCode);
//                        pcmErpProductSupply.setStatus(Constants.PUBLIC_0);
//                        flag = erpProductSupplyMapper.updateByPrimaryKeySelective(pcmErpProductSupply);
//                    } else if (listU.isEmpty()) {
//                        throw new BleException(
//                                ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_RELATION_EXISTENCE.getErrorCode(),
//                                "大码商品与供应商关系不存在！");
//                    } else if (listU.size() > 1) {
//                        throw new BleException(
//                                ComErrorCodeConstants.ErrorCode.PCMSHOPPEPRODUCTSUPPLY_RELATION_EXISTENCE.getErrorCode(),
//                                "存在两条大码商品与供应商关系的重复数据-U操作，联营大码修改供应商！");
//                    }
//                }
            }
        }

        resultMap.put("result", flag);
        resultMap.put("shoppeProductMap", shoppeProductMap);
        logger.info("end uploadShoppeProSupply(),return:" + resultMap);
        return resultMap;
    }
}

package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper groupMapper;
    @Autowired
    private SpecParamMapper paramMapper;

    public List<SpecGroup> querySpecGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> groupList = groupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(groupList)) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return groupList;

    }

    public List<SpecParam> querySpecParamsList(Long gid, Long cid, Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> specParams = paramMapper.select(param);
        if (CollectionUtils.isEmpty(specParams)) {
            throw new LyException(ExceptionEnum.SPEC_PARAMS_NOT_FOUND);
        }
        return specParams;
    }


    public List<SpecGroup> queryAllByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> specGroups = querySpecGroupByCid(cid);
        // 查询当前分类内参数
        List<SpecParam> specParams = querySpecParamsList(null, cid, null);
        // 填充params到group
        // 先把组id变成map的key，值变成组下的参数
        Map<Long, List<SpecParam>> paramMap = new HashMap<>();
        for (SpecParam param : specParams) {
            if (!paramMap.containsKey(param.getGroupId())){
                // 组id再map中不存在
                paramMap.put(param.getGroupId(), new ArrayList<>());
            }
            paramMap.get(param.getGroupId()).add(param);
        }
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(paramMap.get(specGroup.getId()));
        }
        return specGroups;
    }

}
